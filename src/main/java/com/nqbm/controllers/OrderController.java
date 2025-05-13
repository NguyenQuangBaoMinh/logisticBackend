/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.controllers;

/**
 *
 * @author baominh14022004gmail.com
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private SupplierService supplierService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String orders(Model model, 
                        @RequestParam(required = false) Map<String, String> params) {
        List<Order> orders = this.orderService.getOrders(params);
        Long count = this.orderService.countOrders();
        
        // Tính số trang
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) count / pageSize);
        
        // Lấy trang hiện tại
        int currentPage = 1;
        if (params != null && params.containsKey("page")) {
            try {
                currentPage = Integer.parseInt(params.get("page"));
            } catch (NumberFormatException ex) {
                // Giữ trang 1 nếu có lỗi
            }
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("count", count);
        
        // Thêm các tham số tìm kiếm để giữ trong URL
        if (params != null) {
            model.addAttribute("orderNumber", params.getOrDefault("orderNumber", ""));
            model.addAttribute("customer", params.getOrDefault("customer", ""));
            model.addAttribute("status", params.getOrDefault("status", ""));
            model.addAttribute("orderType", params.getOrDefault("orderType", ""));
            model.addAttribute("supplierId", params.getOrDefault("supplierId", ""));
            model.addAttribute("sort", params.getOrDefault("sort", ""));
        }
        
        // Thêm danh sách nhà cung cấp cho dropdown
        List<Supplier> suppliers = this.supplierService.getSuppliers(new HashMap<>());
        model.addAttribute("suppliers", suppliers);
        
        // Thêm danh sách các trạng thái
        model.addAttribute("orderStatuses", Order.OrderStatus.values());
        model.addAttribute("orderTypes", Order.OrderType.values());
        
        // Thêm thống kê
        Map<Order.OrderStatus, Long> statusCounts = new HashMap<>();
        for (Order.OrderStatus status : Order.OrderStatus.values()) {
            statusCounts.put(status, this.orderService.countOrdersByStatus(status));
        }
        model.addAttribute("statusCounts", statusCounts);
        
        return "orders";
    }
    
    @GetMapping("/create")
    public String createOrderForm(Model model, 
                                 @RequestParam(required = false) String type,
                                 @RequestParam(required = false) Long supplierId) {
        // Xác định loại đơn hàng (mặc định là OUTBOUND - xuất kho)
        Order.OrderType orderType = Order.OrderType.OUTBOUND;
        if ("inbound".equalsIgnoreCase(type)) {
            orderType = Order.OrderType.INBOUND;
        }
        
        // Tạo đơn hàng mới
        Order order = new Order();
        order.setOrderType(orderType);
        
        // Nếu là đơn nhập kho và có supplier_id, thiết lập nhà cung cấp
        if (orderType == Order.OrderType.INBOUND && supplierId != null) {
            Supplier supplier = this.supplierService.getSupplierById(supplierId);
            if (supplier != null) {
                order.setSupplier(supplier);
                
                // Tự động điền thông tin từ nhà cung cấp
                order.setCustomerName(supplier.getName());
                order.setCustomerPhone(supplier.getPhoneNumber());
                order.setCustomerEmail(supplier.getEmail());
                order.setShippingAddress(supplier.getAddress());
            }
        }
        
        model.addAttribute("order", order);
        
        // Thêm danh sách nhà cung cấp
        List<Supplier> suppliers = this.supplierService.getSuppliers(new HashMap<>());
        model.addAttribute("suppliers", suppliers);
        
        return "order-form";
    }
    
    @GetMapping("/edit/{id}")
    public String editOrderForm(@PathVariable("id") Long id, Model model) {
        Order order = this.orderService.getOrderById(id);
        
        if (order == null) {
            return "redirect:/orders";
        }
        
        model.addAttribute("order", order);
        
        // Thêm danh sách nhà cung cấp
        List<Supplier> suppliers = this.supplierService.getSuppliers(new HashMap<>());
        model.addAttribute("suppliers", suppliers);
        
        // Lấy danh sách sản phẩm của nhà cung cấp nếu là đơn nhập kho
        if (order.getOrderType() == Order.OrderType.INBOUND && order.getSupplier() != null) {
            List<Product> supplierProducts = this.productService.getProductsBySupplier(order.getSupplier().getId());
            model.addAttribute("supplierProducts", supplierProducts);
        }
        
        return "order-form";
    }
    
    @PostMapping("/save")
    public String saveOrder(@ModelAttribute("order") Order order,
                           @RequestParam(value = "productIds[]", required = false) List<Long> productIds,
                           @RequestParam(value = "quantities[]", required = false) List<Integer> quantities,
                           @RequestParam(value = "unitPrices[]", required = false) List<String> unitPrices,
                           RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin người dùng hiện tại
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.getUserByUsername(auth.getName());
            
            // Thiết lập người tạo đơn hàng
            if (order.getId() == null) {
                order.setCreatedBy(currentUser);
            } else {
                // Nếu cập nhật, lấy thông tin đơn hàng cũ để giữ lại createdBy
                Order existingOrder = orderService.getOrderById(order.getId());
                if (existingOrder != null) {
                    order.setCreatedBy(existingOrder.getCreatedBy());
                    order.setOrderDate(existingOrder.getOrderDate());
                    
                    // Giữ lại các thông tin khác tùy theo nghiệp vụ
                    if (existingOrder.getStatus() != order.getStatus()) {
                        // Xử lý các trường hợp thay đổi trạng thái
                    }
                }
            }
            
            // Xử lý chi tiết đơn hàng
            if (productIds != null && quantities != null && !productIds.isEmpty()) {
                // Xóa chi tiết đơn hàng cũ (nếu cập nhật)
                if (order.getId() != null) {
                    order.getOrderDetails().clear();
                } else {
                    order.setOrderDetails(new HashSet<>());
                }
                
                // Thêm chi tiết đơn hàng mới
                for (int i = 0; i < productIds.size(); i++) {
                    if (productIds.get(i) != null && quantities.get(i) != null && quantities.get(i) > 0) {
                        Product product = productService.getProductById(productIds.get(i));
                        if (product != null) {
                            OrderDetail detail = new OrderDetail();
                            detail.setProduct(product);
                            detail.setQuantity(quantities.get(i));
                            
                            // Nếu có giá đơn vị, sử dụng nó
                            if (unitPrices != null && i < unitPrices.size() && !unitPrices.get(i).isEmpty()) {
                                try {
                                    double unitPrice = Double.parseDouble(unitPrices.get(i).replace(",", ""));
                                    detail.setUnitPrice(java.math.BigDecimal.valueOf(unitPrice));
                                } catch (NumberFormatException e) {
                                    detail.setUnitPrice(product.getUnitPrice());
                                }
                            } else {
                                detail.setUnitPrice(product.getUnitPrice());
                            }
                            
                            // Tự động tính thành tiền
                            detail.calculateSubtotal();
                            
                            // Thêm vào danh sách
                            OrderDetail savedDetail = detail;
                            order.addOrderDetail(savedDetail);
                        }
                    }
                }
            }
            
            // Lưu đơn hàng
            boolean success;
            if (order.getId() == null) {
                success = orderService.addOrder(order);
                if (success) {
                    redirectAttributes.addFlashAttribute("successMessage", "Tạo đơn hàng thành công");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Tạo đơn hàng thất bại");
                }
            } else {
                success = orderService.updateOrder(order);
                if (success) {
                    redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đơn hàng thành công");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật đơn hàng thất bại");
                }
            }
            
            return "redirect:/orders";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xử lý đơn hàng: " + e.getMessage());
            return "redirect:/orders";
        }
    }
    
    @GetMapping("/view/{id}")
    public String viewOrder(@PathVariable("id") Long id, Model model) {
        Order order = this.orderService.getOrderById(id);
        
        if (order == null) {
            return "redirect:/orders";
        }
        
        model.addAttribute("order", order);
        
        return "order-details";
    }
    
    @GetMapping("/approve/{id}")
    public String approveOrder(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        // Lấy thông tin người dùng hiện tại
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByUsername(auth.getName());
        
        boolean success = orderService.approveOrder(id, currentUser.getId());
        
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Duyệt đơn hàng thành công");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Duyệt đơn hàng thất bại");
        }
        
        return "redirect:/orders/view/" + id;
    }
    
    @GetMapping("/ship/{id}")
    public String shipOrder(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        boolean success = orderService.shipOrder(id);
        
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái giao hàng thành công");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật trạng thái giao hàng thất bại");
        }
        
        return "redirect:/orders/view/" + id;
    }
    
    @GetMapping("/complete/{id}")
    public String completeOrder(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        boolean success = orderService.completeOrder(id);
        
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Hoàn thành đơn hàng thành công");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Hoàn thành đơn hàng thất bại");
        }
        
        return "redirect:/orders/view/" + id;
    }
    
    @GetMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        boolean success = orderService.cancelOrder(id);
        
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Hủy đơn hàng thành công");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Hủy đơn hàng thất bại");
        }
        
        return "redirect:/orders/view/" + id;
    }
    
    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        boolean success = orderService.deleteOrder(id);
        
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Xóa đơn hàng thành công");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Xóa đơn hàng thất bại");
        }
        
        return "redirect:/orders";
    }
}
