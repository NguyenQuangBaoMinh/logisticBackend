/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.controllers;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Price;
import com.nqbm.services.PriceService;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prices")
@CrossOrigin(origins = "*")
@Transactional
public class ApiPriceController {
    
    @Autowired
    private PriceService priceService;
    
    /**
     * Get all prices with filters
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPrices(@RequestParam Map<String, String> params) {
        List<Price> prices = priceService.getPrices(params);
        Long totalPrices = priceService.countPrices();
        
        Map<String, Object> response = new HashMap<>();
        response.put("prices", prices);
        response.put("totalCount", totalPrices);
        response.put("currentPage", params.get("page") != null ? Integer.parseInt(params.get("page")) : 1);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get price by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Price> getPriceById(@PathVariable Long id) {
        Price price = priceService.getPriceById(id);
        return price != null ? ResponseEntity.ok(price) : ResponseEntity.notFound().build();
    }
    
    /**
     * Get prices by supplier
     */
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<Price>> getPricesBySupplier(@PathVariable Long supplierId) {
        List<Price> prices = priceService.getPricesBySupplier(supplierId);
        return ResponseEntity.ok(prices);
    }
    
    /**
     * Create new price
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPrice(@RequestBody Price price) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Basic validation
            if (price.getSupplier() == null || price.getProduct() == null || 
                price.getUnitPrice() == null || price.getUnitPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                response.put("success", false);
                response.put("message", "Dữ liệu không hợp lệ");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean created = priceService.addPrice(price);
            response.put("success", created);
            response.put("message", created ? "Thêm đơn giá thành công" : "Có lỗi xảy ra");
            if (created) response.put("price", price);
            
            return created ? ResponseEntity.status(HttpStatus.CREATED).body(response) : 
                           ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Update price
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePrice(@PathVariable Long id, @RequestBody Price price) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (priceService.getPriceById(id) == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy đơn giá");
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            price.setId(id);
            boolean updated = priceService.updatePrice(price);
            response.put("success", updated);
            response.put("message", updated ? "Cập nhật thành công" : "Có lỗi xảy ra");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Delete price
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePrice(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean deleted = priceService.deletePrice(id);
            response.put("success", deleted);
            response.put("message", deleted ? "Xóa thành công" : "Không tìm thấy hoặc có lỗi");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
