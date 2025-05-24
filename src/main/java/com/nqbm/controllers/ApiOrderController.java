/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.controllers;

/**
 *
 * @author baominh14022004gmail.com
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import com.nqbm.pojo.Order;
import com.nqbm.pojo.OrderDetail;
import com.nqbm.pojo.Product;
import com.nqbm.pojo.Supplier;
import com.nqbm.pojo.User;
import com.nqbm.services.OrderService;
import com.nqbm.services.ProductService;
import com.nqbm.services.SupplierService;
import com.nqbm.services.UserService;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author baominh14022004gmail.com
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class ApiOrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private SupplierService supplierService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Lấy danh sách đơn hàng với phân trang và lọc - ADMIN ONLY
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getOrders(@RequestParam Map<String, String> params) {
        try {
            System.out.println("=== GET ORDERS ===");
            System.out.println("Parameters: " + params);
            
            List<Order> orders = orderService.getOrders(params);
            Long totalCount = orderService.countOrders();
            
            System.out.println("Found " + orders.size() + " orders");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("orders", orders);
            result.put("totalCount", totalCount);
            result.put("currentPage", params.get("page") != null ? Integer.parseInt(params.get("page")) : 1);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi lấy danh sách đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * USER lấy đơn hàng của chính mình
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyOrders(@RequestParam Map<String, String> params, Principal principal) {
        try {
            System.out.println("=== GET MY ORDERS ===");
            System.out.println("Parameters: " + params);
            System.out.println("User: " + (principal != null ? principal.getName() : "null"));
            
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            // Thêm filter theo username của user hiện tại
            params.put("createdByUsername", principal.getName());
            
            List<Order> orders = orderService.getOrders(params);
            // Note: Bạn cần thêm method countOrdersByUsername trong OrderService
            Long totalCount = orderService.countOrders(); // Tạm thời dùng countOrders()
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("orders", orders);
            result.put("totalCount", totalCount);
            result.put("currentPage", params.get("page") != null ? Integer.parseInt(params.get("page")) : 1);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi lấy danh sách đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy thông tin đơn hàng theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            System.out.println("=== GET ORDER BY ID ===");
            System.out.println("Order ID: " + id);
            
            Order order = orderService.getOrderById(id);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(Map.of("success", true, "order", order));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi lấy thông tin đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * USER lấy thông tin đơn hàng của mình theo ID
     */
    @GetMapping("/my/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyOrderById(@PathVariable Long id, Principal principal) {
        try {
            System.out.println("=== GET MY ORDER BY ID ===");
            System.out.println("Order ID: " + id);
            System.out.println("User: " + (principal != null ? principal.getName() : "null"));
            
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            Order order = orderService.getOrderById(id);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Kiểm tra ownership: USER chỉ được xem order của mình
            if (!order.getCreatedBy().getUsername().equals(principal.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Bạn không có quyền xem đơn hàng này"));
            }
            
            return ResponseEntity.ok(Map.of("success", true, "order", order));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi lấy thông tin đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy thông tin đơn hàng theo số đơn hàng
     */
    @GetMapping("/by-number/{orderNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getOrderByOrderNumber(@PathVariable String orderNumber) {
        try {
            System.out.println("=== GET ORDER BY NUMBER ===");
            System.out.println("Order Number: " + orderNumber);
            
            Order order = orderService.getOrderByOrderNumber(orderNumber);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(Map.of("success", true, "order", order));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi lấy thông tin đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Tạo đơn hàng mới
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> orderData, Principal principal) {
        try {
            System.out.println("=== CREATE ORDER ===");
            System.out.println("Order Data: " + orderData);
            System.out.println("Principal: " + (principal != null ? principal.getName() : "null"));
            
            // Tạo object Order từ dữ liệu request
            Order order = new Order();
            
            // Set thông tin cơ bản
            order.setCustomerName((String) orderData.get("customerName"));
            order.setCustomerPhone((String) orderData.get("customerPhone"));
            order.setCustomerEmail((String) orderData.get("customerEmail"));
            order.setShippingAddress((String) orderData.get("shippingAddress"));
            order.setNote((String) orderData.get("note"));
            
            // Set order type
            String orderTypeStr = (String) orderData.get("orderType");
            Order.OrderType orderType = orderTypeStr != null ? 
                Order.OrderType.valueOf(orderTypeStr.toUpperCase()) : Order.OrderType.OUTBOUND;
            order.setOrderType(orderType);
            
            // Set supplier nếu có
            if (orderData.get("supplierId") != null) {
                Long supplierId = Long.valueOf(orderData.get("supplierId").toString());
                Supplier supplier = supplierService.getSupplierById(supplierId);
                if (supplier != null) {
                    order.setSupplier(supplier);
                }
            }
            
            // Set người tạo
            if (principal != null) {
                User currentUser = userService.getUserByUsername(principal.getName());
                if (currentUser != null) {
                    order.setCreatedBy(currentUser);
                }
            }
            
            // Xử lý order details
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orderDetailsData = (List<Map<String, Object>>) orderData.get("orderDetails");
            
            if (orderDetailsData != null && !orderDetailsData.isEmpty()) {
                for (Map<String, Object> detailData : orderDetailsData) {
                    OrderDetail detail = new OrderDetail();
                    
                    // Set product
                    Long productId = Long.valueOf(detailData.get("productId").toString());
                    Product product = productService.getProductById(productId);
                    if (product != null) {
                        detail.setProduct(product);
                        detail.setUnitPrice(product.getUnitPrice()); // Set giá từ sản phẩm
                    }
                    
                    // Set quantity
                    detail.setQuantity(Integer.valueOf(detailData.get("quantity").toString()));
                    
                    // Set discount nếu có
                    if (detailData.get("discount") != null) {
                        detail.setDiscount(new java.math.BigDecimal(detailData.get("discount").toString()));
                    }
                    
                    // Set note nếu có
                    if (detailData.get("note") != null) {
                        detail.setNote((String) detailData.get("note"));
                    }
                    
                    order.addOrderDetail(detail);
                }
            }
            
            // Tạo đơn hàng
            boolean created = orderService.addOrder(order);
            
            if (created) {
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "Tạo đơn hàng thành công", "order", order));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Không thể tạo đơn hàng"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi tạo đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Cập nhật đơn hàng - ADMIN ONLY
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody Map<String, Object> orderData) {
        try {
            System.out.println("=== UPDATE ORDER ===");
            System.out.println("Order ID: " + id);
            System.out.println("Update Data: " + orderData);
            
            Order existingOrder = orderService.getOrderById(id);
            if (existingOrder == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Chỉ cho phép cập nhật đơn hàng ở trạng thái PENDING
            if (existingOrder.getStatus() != Order.OrderStatus.PENDING) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Chỉ có thể cập nhật đơn hàng đang chờ xử lý"));
            }
            
            // Cập nhật thông tin cơ bản
            if (orderData.get("customerName") != null) {
                existingOrder.setCustomerName((String) orderData.get("customerName"));
            }
            if (orderData.get("customerPhone") != null) {
                existingOrder.setCustomerPhone((String) orderData.get("customerPhone"));
            }
            if (orderData.get("customerEmail") != null) {
                existingOrder.setCustomerEmail((String) orderData.get("customerEmail"));
            }
            if (orderData.get("shippingAddress") != null) {
                existingOrder.setShippingAddress((String) orderData.get("shippingAddress"));
            }
            if (orderData.get("note") != null) {
                existingOrder.setNote((String) orderData.get("note"));
            }
            
            // Cập nhật supplier nếu có
            if (orderData.get("supplierId") != null) {
                Long supplierId = Long.valueOf(orderData.get("supplierId").toString());
                Supplier supplier = supplierService.getSupplierById(supplierId);
                if (supplier != null) {
                    existingOrder.setSupplier(supplier);
                }
            }
            
            // Cập nhật order details nếu có
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orderDetailsData = (List<Map<String, Object>>) orderData.get("orderDetails");
            
            if (orderDetailsData != null) {
                // Xóa order details cũ
                existingOrder.getOrderDetails().clear();
                
                // Thêm order details mới
                for (Map<String, Object> detailData : orderDetailsData) {
                    OrderDetail detail = new OrderDetail();
                    
                    // Set product
                    Long productId = Long.valueOf(detailData.get("productId").toString());
                    Product product = productService.getProductById(productId);
                    if (product != null) {
                        detail.setProduct(product);
                        detail.setUnitPrice(product.getUnitPrice());
                    }
                    
                    // Set quantity
                    detail.setQuantity(Integer.valueOf(detailData.get("quantity").toString()));
                    
                    // Set discount nếu có
                    if (detailData.get("discount") != null) {
                        detail.setDiscount(new java.math.BigDecimal(detailData.get("discount").toString()));
                    }
                    
                    // Set note nếu có
                    if (detailData.get("note") != null) {
                        detail.setNote((String) detailData.get("note"));
                    }
                    
                    existingOrder.addOrderDetail(detail);
                }
            }
            
            // Cập nhật đơn hàng
            boolean updated = orderService.updateOrder(existingOrder);
            
            if (updated) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật đơn hàng thành công", "order", existingOrder));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Không thể cập nhật đơn hàng"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi cập nhật đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * USER cập nhật đơn hàng của chính mình
     */
    @PutMapping("/my/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateMyOrder(@PathVariable Long id, @RequestBody Map<String, Object> orderData, Principal principal) {
        try {
            System.out.println("=== UPDATE MY ORDER ===");
            System.out.println("Order ID: " + id);
            System.out.println("Update Data: " + orderData);
            System.out.println("User: " + (principal != null ? principal.getName() : "null"));
            
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            
            Order existingOrder = orderService.getOrderById(id);
            if (existingOrder == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Kiểm tra ownership: USER chỉ được sửa order của mình
            if (!existingOrder.getCreatedBy().getUsername().equals(principal.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Bạn không có quyền sửa đơn hàng này"));
            }
            
            // Chỉ cho phép cập nhật đơn hàng ở trạng thái PENDING
            if (existingOrder.getStatus() != Order.OrderStatus.PENDING) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Chỉ có thể cập nhật đơn hàng đang chờ xử lý"));
            }
            
            // Cập nhật thông tin cơ bản
            if (orderData.get("customerName") != null) {
                existingOrder.setCustomerName((String) orderData.get("customerName"));
            }
            if (orderData.get("customerPhone") != null) {
                existingOrder.setCustomerPhone((String) orderData.get("customerPhone"));
            }
            if (orderData.get("customerEmail") != null) {
                existingOrder.setCustomerEmail((String) orderData.get("customerEmail"));
            }
            if (orderData.get("shippingAddress") != null) {
                existingOrder.setShippingAddress((String) orderData.get("shippingAddress"));
            }
            if (orderData.get("note") != null) {
                existingOrder.setNote((String) orderData.get("note"));
            }
            
            // Cập nhật supplier nếu có
            if (orderData.get("supplierId") != null) {
                Long supplierId = Long.valueOf(orderData.get("supplierId").toString());
                Supplier supplier = supplierService.getSupplierById(supplierId);
                if (supplier != null) {
                    existingOrder.setSupplier(supplier);
                }
            }
            
            // Cập nhật order details nếu có
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orderDetailsData = (List<Map<String, Object>>) orderData.get("orderDetails");
            
            if (orderDetailsData != null) {
                // Xóa order details cũ
                existingOrder.getOrderDetails().clear();
                
                // Thêm order details mới
                for (Map<String, Object> detailData : orderDetailsData) {
                    OrderDetail detail = new OrderDetail();
                    
                    // Set product
                    Long productId = Long.valueOf(detailData.get("productId").toString());
                    Product product = productService.getProductById(productId);
                    if (product != null) {
                        detail.setProduct(product);
                        detail.setUnitPrice(product.getUnitPrice());
                    }
                    
                    // Set quantity
                    detail.setQuantity(Integer.valueOf(detailData.get("quantity").toString()));
                    
                    // Set discount nếu có
                    if (detailData.get("discount") != null) {
                        detail.setDiscount(new java.math.BigDecimal(detailData.get("discount").toString()));
                    }
                    
                    // Set note nếu có
                    if (detailData.get("note") != null) {
                        detail.setNote((String) detailData.get("note"));
                    }
                    
                    existingOrder.addOrderDetail(detail);
                }
            }
            
            // Cập nhật đơn hàng
            boolean updated = orderService.updateOrder(existingOrder);
            
            if (updated) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật đơn hàng thành công", "order", existingOrder));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Không thể cập nhật đơn hàng"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi cập nhật đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Xóa đơn hàng
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            System.out.println("=== DELETE ORDER ===");
            System.out.println("Order ID: " + id);
            
            Order existingOrder = orderService.getOrderById(id);
            if (existingOrder == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Chỉ cho phép xóa đơn hàng ở trạng thái PENDING
            if (existingOrder.getStatus() != Order.OrderStatus.PENDING) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Chỉ có thể xóa đơn hàng đang chờ xử lý"));
            }
            
            boolean deleted = orderService.deleteOrder(id);
            
            if (deleted) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Xóa đơn hàng thành công"));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Không thể xóa đơn hàng"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi xóa đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Duyệt đơn hàng
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveOrder(@PathVariable Long id, Principal principal) {
        try {
            System.out.println("=== APPROVE ORDER ===");
            System.out.println("Order ID: " + id);
            System.out.println("Approved by: " + (principal != null ? principal.getName() : "null"));
            
            if (principal == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Không xác định được người duyệt"));
            }
            
            User currentUser = userService.getUserByUsername(principal.getName());
            if (currentUser == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Không tìm thấy người duyệt"));
            }
            
            boolean approved = orderService.approveOrder(id, currentUser.getId());
            
            if (approved) {
                Order order = orderService.getOrderById(id);
                return ResponseEntity.ok(Map.of("success", true, "message", "Duyệt đơn hàng thành công", "order", order));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Không thể duyệt đơn hàng"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi duyệt đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Chuyển trạng thái đang giao hàng
     */
    @PutMapping("/{id}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> shipOrder(@PathVariable Long id) {
        try {
            System.out.println("=== SHIP ORDER ===");
            System.out.println("Order ID: " + id);
            
            boolean shipped = orderService.shipOrder(id);
            
            if (shipped) {
                Order order = orderService.getOrderById(id);
                return ResponseEntity.ok(Map.of("success", true, "message", "Đơn hàng đang được giao", "order", order));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Không thể chuyển trạng thái đơn hàng"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Hoàn thành đơn hàng
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> completeOrder(@PathVariable Long id) {
        try {
            System.out.println("=== COMPLETE ORDER ===");
            System.out.println("Order ID: " + id);
            
            boolean completed = orderService.completeOrder(id);
            
            if (completed) {
                Order order = orderService.getOrderById(id);
                return ResponseEntity.ok(Map.of("success", true, "message", "Hoàn thành đơn hàng", "order", order));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Không thể hoàn thành đơn hàng"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi hoàn thành đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Hủy đơn hàng
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            System.out.println("=== CANCEL ORDER ===");
            System.out.println("Order ID: " + id);
            
            boolean cancelled = orderService.cancelOrder(id);
            
            if (cancelled) {
                Order order = orderService.getOrderById(id);
                return ResponseEntity.ok(Map.of("success", true, "message", "Hủy đơn hàng thành công", "order", order));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Không thể hủy đơn hàng"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi hủy đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy thống kê đơn hàng theo trạng thái
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getOrderStatistics() {
        try {
            System.out.println("=== GET ORDER STATISTICS ===");
            
            Map<String, Object> statistics = new HashMap<>();
            
            // Đếm tổng số đơn hàng
            Long totalOrders = orderService.countOrders();
            statistics.put("totalOrders", totalOrders);
            
            // Đếm theo trạng thái
            for (Order.OrderStatus status : Order.OrderStatus.values()) {
                Long count = orderService.countOrdersByStatus(status);
                statistics.put(status.name().toLowerCase(), count);
            }
            
            return ResponseEntity.ok(Map.of("success", true, "statistics", statistics));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi lấy thống kê: " + e.getMessage()));
        }
    }
    
    /**
     * Tìm kiếm đơn hàng
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> searchOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderType,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        try {
            System.out.println("=== SEARCH ORDERS ===");
            System.out.println("Keyword: " + keyword);
            System.out.println("Status: " + status);
            System.out.println("OrderType: " + orderType);
            
            Map<String, String> params = new HashMap<>();
            params.put("page", String.valueOf(page));
            params.put("size", String.valueOf(size));
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                params.put("keyword", keyword);
            }
            if (status != null && !status.trim().isEmpty()) {
                params.put("status", status);
            }
            if (orderType != null && !orderType.trim().isEmpty()) {
                params.put("orderType", orderType);
            }
            
            List<Order> orders = orderService.getOrders(params);
            Long totalCount = orderService.countOrders();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("orders", orders);
            result.put("totalCount", totalCount);
            result.put("currentPage", page);
            result.put("totalPages", (totalCount + size - 1) / size);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi tìm kiếm đơn hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách order details của một đơn hàng
     */
    @GetMapping("/{id}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long id) {
        try {
            System.out.println("=== GET ORDER DETAILS ===");
            System.out.println("Order ID: " + id);
            
            Order order = orderService.getOrderById(id);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "orderDetails", order.getOrderDetails(),
                "order", order
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi lấy chi tiết đơn hàng: " + e.getMessage()));
        }
    }
}