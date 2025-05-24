/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.controllers;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Shipping;
import com.nqbm.services.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/shipping")
@CrossOrigin
public class ApiShippingController {
    
    @Autowired
    private ShippingService shippingService;
    
    /**
     * Lấy danh sách shipping với bộ lọc và phân trang
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getShippings(@RequestParam Map<String, String> params) {
        try {
            List<Shipping> shippings = shippingService.getShippings(params);
            Long totalCount = shippingService.countShippings();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("shippings", shippings);
            response.put("totalCount", totalCount);
            response.put("message", "Lấy danh sách shipping thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy danh sách shipping: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Lấy shipping theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getShippingById(@PathVariable Long id) {
        try {
            Shipping shipping = shippingService.getShippingById(id);
            
            Map<String, Object> response = new HashMap<>();
            if (shipping != null) {
                response.put("success", true);
                response.put("shipping", shipping);
                response.put("message", "Lấy thông tin shipping thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy shipping với ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy thông tin shipping: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Lấy shipping theo tracking number
     */
    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<Map<String, Object>> getShippingByTrackingNumber(@PathVariable String trackingNumber) {
        try {
            Shipping shipping = shippingService.getShippingByTrackingNumber(trackingNumber);
            
            Map<String, Object> response = new HashMap<>();
            if (shipping != null) {
                response.put("success", true);
                response.put("shipping", shipping);
                response.put("message", "Theo dõi shipping thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy shipping với tracking number: " + trackingNumber);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi theo dõi shipping: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Lấy shipping theo order ID
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Map<String, Object>> getShippingByOrderId(@PathVariable Long orderId) {
        try {
            Shipping shipping = shippingService.getShippingByOrderId(orderId);
            
            Map<String, Object> response = new HashMap<>();
            if (shipping != null) {
                response.put("success", true);
                response.put("shipping", shipping);
                response.put("message", "Lấy thông tin shipping cho đơn hàng thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy shipping cho đơn hàng ID: " + orderId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy thông tin shipping: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Tạo mới shipping
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createShipping(@RequestBody Shipping shipping) {
        try {
            boolean success = shippingService.addShipping(shipping);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Tạo shipping thành công");
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                response.put("success", false);
                response.put("message", "Tạo shipping thất bại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi tạo shipping: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Cập nhật shipping
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateShipping(@PathVariable Long id, @RequestBody Shipping shipping) {
        try {
            shipping.setId(id);
            boolean success = shippingService.updateShipping(shipping);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Cập nhật shipping thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Cập nhật shipping thất bại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi cập nhật shipping: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Xóa shipping (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteShipping(@PathVariable Long id) {
        try {
            boolean success = shippingService.deleteShipping(id);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Xóa shipping thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Xóa shipping thất bại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi xóa shipping: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // Shipping Status Management
    
    /**
     * Cập nhật trạng thái shipping
     */
    @PostMapping("/{id}/update-status")
    public ResponseEntity<Map<String, Object>> updateShippingStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String notes) {
        try {
            boolean success = shippingService.updateShippingStatus(id, status, location, notes);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Cập nhật trạng thái shipping thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Cập nhật trạng thái thất bại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Lên lịch lấy hàng
     */
    @PostMapping("/{id}/schedule-pickup")
    public ResponseEntity<Map<String, Object>> schedulePickup(
            @PathVariable Long id,
            @RequestParam String pickupDate) {
        try {
            boolean success = shippingService.schedulePickup(id, pickupDate);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Lên lịch lấy hàng thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Lên lịch lấy hàng thất bại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lên lịch lấy hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Xác nhận lấy hàng
     */
    @PostMapping("/{id}/confirm-pickup")
    public ResponseEntity<Map<String, Object>> confirmPickup(
            @PathVariable Long id,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String notes) {
        try {
            boolean success = shippingService.confirmPickup(id, location, notes);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Xác nhận lấy hàng thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Xác nhận lấy hàng thất bại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi xác nhận lấy hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Cập nhật lịch giao hàng
     */
    @PostMapping("/{id}/update-delivery-schedule")
    public ResponseEntity<Map<String, Object>> updateDeliverySchedule(
            @PathVariable Long id,
            @RequestParam String deliveryDate) {
        try {
            boolean success = shippingService.updateDeliverySchedule(id, deliveryDate);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Cập nhật lịch giao hàng thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Cập nhật lịch giao hàng thất bại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi cập nhật lịch giao hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Xác nhận giao hàng thành công
     */
    @PostMapping("/{id}/confirm-delivery")
    public ResponseEntity<Map<String, Object>> confirmDelivery(
            @PathVariable Long id,
            @RequestParam String confirmationType,
            @RequestParam(required = false) String notes) {
        try {
            boolean success = shippingService.confirmDelivery(id, confirmationType, notes);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Xác nhận giao hàng thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Xác nhận giao hàng thất bại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi xác nhận giao hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Hủy shipping
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelShipping(
            @PathVariable Long id,
            @RequestParam String reason) {
        try {
            boolean success = shippingService.cancelShipping(id, reason);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Hủy shipping thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Hủy shipping thất bại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi hủy shipping: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // Analytics and Reports
    
    /**
     * Lấy thống kê tổng quan shipping
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getShippingStatistics() {
        try {
            Map<String, Object> statistics = shippingService.getShippingStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            response.put("message", "Lấy thống kê shipping thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy thống kê shipping: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Lấy danh sách shipping quá hạn
     */
    @GetMapping("/overdue")
    public ResponseEntity<Map<String, Object>> getOverdueShippings() {
        try {
            List<Shipping> overdueShippings = shippingService.getOverdueShippings();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("shippings", overdueShippings);
            response.put("count", overdueShippings.size());
            response.put("message", "Lấy danh sách shipping quá hạn thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy danh sách shipping quá hạn: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Lấy danh sách shipping theo status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getShippingsByStatus(@PathVariable String status) {
        try {
            List<Shipping> shippings = shippingService.getShippingsByStatus(status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("shippings", shippings);
            response.put("count", shippings.size());
            response.put("message", "Lấy danh sách shipping theo status thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy danh sách shipping theo status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Lấy báo cáo hiệu suất đối tác vận chuyển
     */
    @GetMapping("/partner-performance")
    public ResponseEntity<Map<String, Object>> getPartnerPerformance() {
        try {
            List<Map<String, Object>> performance = shippingService.getPartnerPerformance();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("partnerPerformance", performance);
            response.put("message", "Lấy báo cáo hiệu suất đối tác thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy báo cáo hiệu suất đối tác: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Tính toán chi phí vận chuyển
     */
    @PostMapping("/calculate-cost")
    public ResponseEntity<Map<String, Object>> calculateShippingCost(
            @RequestParam String serviceType,
            @RequestParam Double distance,
            @RequestParam Double weight) {
        try {
            Double cost = shippingService.calculateShippingCost(serviceType, distance, weight);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cost", cost);
            response.put("serviceType", serviceType);
            response.put("distance", distance);
            response.put("weight", weight);
            response.put("message", "Tính toán chi phí vận chuyển thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi tính toán chi phí vận chuyển: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Kiểm tra có thể hủy shipping không
     */
    @GetMapping("/{id}/can-cancel")
    public ResponseEntity<Map<String, Object>> canCancelShipping(@PathVariable Long id) {
        try {
            boolean canCancel = shippingService.canCancelShipping(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("canCancel", canCancel);
            response.put("message", "Kiểm tra trạng thái hủy shipping thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi kiểm tra trạng thái hủy shipping: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
