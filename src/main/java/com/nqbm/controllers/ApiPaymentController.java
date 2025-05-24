/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.controllers;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Payment;
import com.nqbm.services.PaymentService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@Transactional
public class ApiPaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * Get all payments with filters
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPayments(@RequestParam Map<String, String> params) {
        List<Payment> payments = paymentService.getPayments(params);
        Long totalPayments = paymentService.countPayments();
        
        Map<String, Object> response = new HashMap<>();
        response.put("payments", payments);
        response.put("totalCount", totalPayments);
        response.put("currentPage", params.get("page") != null ? Integer.parseInt(params.get("page")) : 1);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get payment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id);
        return payment != null ? ResponseEntity.ok(payment) : ResponseEntity.notFound().build();
    }
    
    /**
     * Get payments by supplier
     */
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<Payment>> getPaymentsBySupplier(@PathVariable Long supplierId) {
        List<Payment> payments = paymentService.getPaymentsBySupplier(supplierId);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Get overdue payments
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<Payment>> getOverduePayments() {
        List<Payment> payments = paymentService.getOverduePayments();
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Get payment summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getPaymentSummary() {
        Map<String, Object> response = new HashMap<>();
        
        BigDecimal totalPayable = paymentService.getTotalPayableAmount();
        BigDecimal totalReceivable = paymentService.getTotalReceivableAmount();
        
        response.put("totalPayable", totalPayable);
        response.put("totalReceivable", totalReceivable);
        response.put("netAmount", totalReceivable.subtract(totalPayable));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create new payment
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody Payment payment) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Basic validation
            if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                response.put("success", false);
                response.put("message", "Số tiền phải lớn hơn 0");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean created = paymentService.addPayment(payment);
            response.put("success", created);
            response.put("message", created ? "Tạo thanh toán thành công" : "Có lỗi xảy ra");
            if (created) response.put("payment", payment);
            
            return created ? ResponseEntity.status(HttpStatus.CREATED).body(response) : 
                           ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Update payment
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePayment(@PathVariable Long id, @RequestBody Payment payment) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (paymentService.getPaymentById(id) == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thanh toán");
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            payment.setId(id);
            boolean updated = paymentService.updatePayment(payment);
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
     * Process payment (đặc trưng)
     */
    @PutMapping("/{id}/process")
    public ResponseEntity<Map<String, Object>> processPayment(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean processed = paymentService.processPayment(id);
            response.put("success", processed);
            response.put("message", processed ? "Xử lý thanh toán thành công" : "Không thể xử lý thanh toán");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Cancel payment (đặc trưng)
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelPayment(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean cancelled = paymentService.cancelPayment(id);
            response.put("success", cancelled);
            response.put("message", cancelled ? "Hủy thanh toán thành công" : "Không thể hủy thanh toán");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
