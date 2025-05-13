/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.controllers;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Supplier;
import com.nqbm.services.SupplierService;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API Controller for Supplier management
 */
@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin
public class ApiSupplierController {
    
    @Autowired
    private SupplierService supplierService;
    
    /**
     * Get all suppliers with optional filters
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSuppliers(
            @RequestParam Map<String, String> params) {
        
        List<Supplier> suppliers = supplierService.getSuppliers(params);
        Long totalSuppliers = supplierService.countSuppliers();
        
        Map<String, Object> response = new HashMap<>();
        response.put("suppliers", suppliers);
        response.put("totalCount", totalSuppliers);
        response.put("currentPage", params.get("page") != null ? Integer.parseInt(params.get("page")) : 1);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get active suppliers only
     */
    @GetMapping("/active")
    public ResponseEntity<List<Supplier>> getActiveSuppliers() {
        Map<String, String> params = new HashMap<>();
        params.put("active", "true");
        
        List<Supplier> suppliers = supplierService.getSuppliers(params);
        return ResponseEntity.ok(suppliers);
    }
    
    /**
     * Search suppliers by name
     */
    @GetMapping("/search")
    public ResponseEntity<List<Supplier>> searchSuppliers(
            @RequestParam String name,
            @RequestParam(required = false) String active,
            @RequestParam(required = false) String sort) {
        
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        
        if (active != null) {
            params.put("active", active);
        }
        
        if (sort != null) {
            params.put("sort", sort);
        }
        
        List<Supplier> suppliers = supplierService.getSuppliers(params);
        return ResponseEntity.ok(suppliers);
    }
    
    /**
     * Get supplier by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        Supplier supplier = supplierService.getSupplierById(id);
        
        if (supplier == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(supplier);
    }
    
    /**
     * Create new supplier
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSupplier(@RequestBody Supplier supplier) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validation
            if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Tên nhà cung cấp không được để trống");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean created = supplierService.addSupplier(supplier);
            
            if (created) {
                response.put("success", true);
                response.put("message", "Thêm nhà cung cấp thành công");
                response.put("supplier", supplier);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                response.put("success", false);
                response.put("message", "Có lỗi xảy ra khi thêm nhà cung cấp");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Update supplier
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSupplier(
            @PathVariable Long id, 
            @RequestBody Supplier supplier) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if supplier exists
            Supplier existingSupplier = supplierService.getSupplierById(id);
            if (existingSupplier == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy nhà cung cấp với ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Validation
            if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Tên nhà cung cấp không được để trống");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Set ID to ensure update
            supplier.setId(id);
            
            boolean updated = supplierService.updateSupplier(supplier);
            
            if (updated) {
                response.put("success", true);
                response.put("message", "Cập nhật nhà cung cấp thành công");
                response.put("supplier", supplier);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Có lỗi xảy ra khi cập nhật nhà cung cấp");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Delete supplier
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSupplier(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if supplier exists
            Supplier existingSupplier = supplierService.getSupplierById(id);
            if (existingSupplier == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy nhà cung cấp với ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            boolean deleted = supplierService.deleteSupplier(id);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "Xóa nhà cung cấp thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Có lỗi xảy ra khi xóa nhà cung cấp");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Toggle supplier active status
     */
    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<Map<String, Object>> toggleSupplierStatus(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Supplier supplier = supplierService.getSupplierById(id);
            if (supplier == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy nhà cung cấp với ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Toggle active status
            supplier.setActive(!supplier.isActive());
            
            boolean updated = supplierService.updateSupplier(supplier);
            
            if (updated) {
                response.put("success", true);
                response.put("message", "Cập nhật trạng thái thành công");
                response.put("active", supplier.isActive());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Có lỗi xảy ra khi cập nhật trạng thái");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Update supplier rating
     */
    @PutMapping("/{id}/rating")
    public ResponseEntity<Map<String, Object>> updateSupplierRating(
            @PathVariable Long id, 
            @RequestBody Map<String, Integer> ratingData) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Supplier supplier = supplierService.getSupplierById(id);
            if (supplier == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy nhà cung cấp với ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Integer rating = ratingData.get("rating");
            if (rating == null || rating < 1 || rating > 5) {
                response.put("success", false);
                response.put("message", "Đánh giá phải từ 1 đến 5");
                return ResponseEntity.badRequest().body(response);
            }
            
            supplier.setRating(rating);
            boolean updated = supplierService.updateSupplier(supplier);
            
            if (updated) {
                response.put("success", true);
                response.put("message", "Cập nhật đánh giá thành công");
                response.put("rating", rating);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Có lỗi xảy ra khi cập nhật đánh giá");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get suppliers count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getSuppliersCount() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long totalCount = supplierService.countSuppliers();
            
            // Count active suppliers
            Map<String, String> activeParams = new HashMap<>();
            activeParams.put("active", "true");
            List<Supplier> activeSuppliers = supplierService.getSuppliers(activeParams);
            
            response.put("total", totalCount);
            response.put("active", activeSuppliers.size());
            response.put("inactive", totalCount - activeSuppliers.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}