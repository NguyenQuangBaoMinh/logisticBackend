/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.controllers;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Inventory;
import com.nqbm.services.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin
public class ApiInventoryController {
    
    @Autowired
    private InventoryService inventoryService;
    
    /**
     * Lấy danh sách inventory với bộ lọc và phân trang
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getInventories(@RequestParam Map<String, String> params) {
        try {
            List<Inventory> inventories = inventoryService.getInventories(params);
            Long totalCount = inventoryService.countInventories();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("inventories", inventories);
            response.put("totalCount", totalCount);
            response.put("message", "Lấy danh sách inventory thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy danh sách inventory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Lấy inventory theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getInventoryById(@PathVariable Long id) {
        try {
            Inventory inventory = inventoryService.getInventoryById(id);
            
            Map<String, Object> response = new HashMap<>();
            if (inventory != null) {
                response.put("success", true);
                response.put("inventory", inventory);
                response.put("message", "Lấy thông tin inventory thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy inventory với ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy thông tin inventory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Lấy inventory theo product ID
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> getInventoryByProductId(@PathVariable Long productId) {
        try {
            Inventory inventory = inventoryService.getInventoryByProductId(productId);
            
            Map<String, Object> response = new HashMap<>();
            if (inventory != null) {
                response.put("success", true);
                response.put("inventory", inventory);
                response.put("message", "Lấy thông tin inventory thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy inventory cho product ID: " + productId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy thông tin inventory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Tạo mới inventory
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createInventory(@RequestBody Inventory inventory) {
        try {
            boolean success = inventoryService.addInventory(inventory);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Tạo inventory thành công");
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                response.put("success", false);
                response.put("message", "Tạo inventory thất bại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi tạo inventory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Cập nhật inventory
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateInventory(@PathVariable Long id, @RequestBody Inventory inventory) {
        try {
            inventory.setId(id);
            boolean success = inventoryService.updateInventory(inventory);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Cập nhật inventory thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Cập nhật inventory thất bại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi cập nhật inventory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Xóa inventory (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteInventory(@PathVariable Long id) {
        try {
            boolean success = inventoryService.deleteInventory(id);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Xóa inventory thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Xóa inventory thất bại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi xóa inventory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Nhập kho - tăng số lượng tồn kho
     */
    @PostMapping("/{id}/increase-stock")
    public ResponseEntity<Map<String, Object>> increaseStock(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String reason) {
        try {
            boolean success = inventoryService.increaseStock(id, quantity, reason);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Nhập kho thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Nhập kho thất bại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi nhập kho: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Xuất kho - giảm số lượng tồn kho
     */
    @PostMapping("/{id}/decrease-stock")
    public ResponseEntity<Map<String, Object>> decreaseStock(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String reason) {
        try {
            boolean success = inventoryService.decreaseStock(id, quantity, reason);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Xuất kho thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Xuất kho thất bại - không đủ hàng trong kho");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi xuất kho: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Điều chỉnh tồn kho
     */
    @PostMapping("/{id}/adjust-stock")
    public ResponseEntity<Map<String, Object>> adjustStock(
            @PathVariable Long id,
            @RequestParam Integer newQuantity,
            @RequestParam(required = false) String reason) {
        try {
            boolean success = inventoryService.adjustStock(id, newQuantity, reason);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Điều chỉnh tồn kho thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Điều chỉnh tồn kho thất bại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi điều chỉnh tồn kho: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Lấy thống kê tổng quan inventory
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getInventoryStatistics() {
        try {
            Map<String, Object> statistics = inventoryService.getInventoryStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            response.put("message", "Lấy thống kê inventory thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy thống kê inventory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Lấy danh sách inventory cần đặt hàng lại
     */
    @GetMapping("/low-stock")
    public ResponseEntity<Map<String, Object>> getLowStockItems() {
        try {
            List<Inventory> lowStockItems = inventoryService.getLowStockItems();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("inventories", lowStockItems);
            response.put("count", lowStockItems.size());
            response.put("message", "Lấy danh sách inventory cần đặt hàng lại thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy danh sách low stock: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Lấy danh sách inventory hết hàng
     */
    @GetMapping("/out-of-stock")
    public ResponseEntity<Map<String, Object>> getOutOfStockItems() {
        try {
            List<Inventory> outOfStockItems = inventoryService.getOutOfStockItems();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("inventories", outOfStockItems);
            response.put("count", outOfStockItems.size());
            response.put("message", "Lấy danh sách inventory hết hàng thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy danh sách out of stock: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
