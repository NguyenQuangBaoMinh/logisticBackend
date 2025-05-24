/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.controllers;

/**
 *
 * @author baominh14022004gmail.com
 */
// src/main/java/com/nqbm/controllers/ApiMoMoPaymentController.java
import com.nqbm.pojo.Payment;
import com.nqbm.services.MoMoPaymentService;
import com.nqbm.services.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments/momo")
@CrossOrigin(origins = "*")
@Transactional
public class ApiMoMoPaymentController {

    @Autowired
    private MoMoPaymentService moMoPaymentService;

    @Autowired
    private PaymentService paymentService;

    /**
     * 1. Tạo MoMo payment URL
     */
    @PostMapping("/create/{paymentId}")
    public ResponseEntity<Map<String, Object>> createMoMoPayment(@PathVariable Long paymentId) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println(" Creating MoMo payment for Payment ID: " + paymentId);

            // Kiểm tra payment tồn tại
            Payment payment = paymentService.getPaymentById(paymentId);
            if (payment == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thanh toán với ID: " + paymentId);
                return ResponseEntity.status(404).body(response);
            }

            // Kiểm tra trạng thái payment
            if (!payment.getStatus().equals(Payment.PaymentStatus.PENDING)) {
                response.put("success", false);
                response.put("message", "Thanh toán đã được xử lý, không thể thanh toán lại");
                response.put("currentStatus", payment.getStatus().toString());
                return ResponseEntity.badRequest().body(response);
            }

            // Kiểm tra số tiền hợp lệ
            if (payment.getAmount() == null || payment.getAmount().longValue() <= 0) {
                response.put("success", false);
                response.put("message", "Số tiền thanh toán không hợp lệ");
                return ResponseEntity.badRequest().body(response);
            }

            // Tạo MoMo payment
            Map<String, Object> moMoResponse = moMoPaymentService.createMoMoPayment(payment);

            // Xử lý kết quả
            String resultCode = String.valueOf(moMoResponse.get("resultCode"));
            if ("0".equals(resultCode)) {
                // Thành công
                response.put("success", true);
                response.put("payUrl", moMoResponse.get("payUrl"));
                response.put("orderId", moMoResponse.get("orderId"));
                response.put("deeplink", moMoResponse.get("deeplink"));
                response.put("qrCodeUrl", moMoResponse.get("qrCodeUrl"));
                response.put("message", "Tạo link thanh toán MoMo thành công");
                response.put("paymentInfo", createPaymentInfo(payment));

                System.out.println("MoMo payment URL created successfully");
            } else {
                // Thất bại
                response.put("success", false);
                response.put("resultCode", resultCode);
                response.put("message", "Lỗi MoMo: " + moMoResponse.get("message"));

                System.err.println(" MoMo payment creation failed: " + moMoResponse.get("message"));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println(" Exception in createMoMoPayment: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 2. Kiểm tra trạng thái thanh toán MoMo
     */
    @GetMapping("/status/{paymentId}")
    public ResponseEntity<Map<String, Object>> checkPaymentStatus(@PathVariable Long paymentId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Payment payment = paymentService.getPaymentById(paymentId);
            if (payment == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thanh toán");
                return ResponseEntity.status(404).body(response);
            }

            response.put("success", true);
            response.put("paymentId", payment.getId());
            response.put("paymentCode", payment.getPaymentCode());
            response.put("status", payment.getStatus().toString());
            response.put("amount", payment.getAmount());
            response.put("paymentDate", payment.getPaymentDate());
            response.put("message", "Lấy trạng thái thanh toán thành công");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi kiểm tra trạng thái: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 3. MoMo callback - User redirect về sau khi thanh toán
     */
    @GetMapping("/callback")
    public ModelAndView handleCallback(HttpServletRequest request) {
        try {
            System.out.println(" MoMo callback received");

            // Lấy tất cả parameters
            Map<String, String> params = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values.length > 0) {
                    params.put(key, values[0]);
                }
            });

            System.out.println("Callback params: " + params);

            String resultCode = params.get("resultCode");
            String orderId = params.get("orderId");
            String message = params.get("message");

            // Tạo redirect URL dựa trên kết quả
            String redirectUrl;
            if ("0".equals(resultCode)) {
                // Thành công
                redirectUrl = "http://localhost:3000/payments?status=success&orderId=" + orderId + "&message=Thanh toán thành công";
                System.out.println("✅ Payment successful for order: " + orderId);
            } else {
                // Thất bại
                String errorMessage = message != null ? message : "Thanh toán thất bại";
                redirectUrl = "http://localhost:3000/payments?status=failed&orderId=" + orderId + "&message=" + errorMessage;
                System.out.println("Payment failed for order: " + orderId + ", reason: " + errorMessage);
            }

            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("redirect:" + redirectUrl);
            return modelAndView;

        } catch (Exception e) {
            System.err.println(" Error in callback: " + e.getMessage());
            ModelAndView errorView = new ModelAndView();
            errorView.setViewName("redirect:http://localhost:3000/payments?status=error&message=Lỗi xử lý callback");
            return errorView;
        }
    }

    /**
     * 4. MoMo IPN - Server notification từ MoMo
     */
    @PostMapping("/notify")
    public ResponseEntity<Map<String, String>> handleNotify(@RequestBody Map<String, String> params) {
        Map<String, String> response = new HashMap<>();

        try {
            System.out.println("🔔 MoMo IPN notification received");
            System.out.println("IPN params: " + params);

            // Verify signature
            if (!moMoPaymentService.verifySignature(params)) {
                System.err.println(" Invalid signature in IPN");
                response.put("resultCode", "-1");
                response.put("message", "Invalid signature");
                return ResponseEntity.badRequest().body(response);
            }

            String resultCode = params.get("resultCode");
            String orderId = params.get("orderId");
            String transId = params.get("transId");

            // Extract payment ID từ orderId (format: SCM_paymentId_timestamp)
            if (orderId != null && orderId.startsWith("SCM_")) {
                try {
                    String[] parts = orderId.split("_");
                    if (parts.length >= 2) {
                        Long paymentId = Long.parseLong(parts[1]);
                        Payment payment = paymentService.getPaymentById(paymentId);

                        if (payment != null) {
                            updatePaymentStatus(payment, resultCode, orderId, transId);
                            System.out.println("Payment status updated for ID: " + paymentId);
                        } else {
                            System.err.println(" Payment not found for ID: " + paymentId);
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println(" Invalid payment ID in orderId: " + orderId);
                }
            }

            // Phản hồi thành công cho MoMo
            response.put("resultCode", "0");
            response.put("message", "OK");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println(" Error in IPN notify: " + e.getMessage());
            e.printStackTrace();

            response.put("resultCode", "-1");
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 5. Test MoMo connection
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testMoMoConnection() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Tạo một payment test để kiểm tra connection
            Payment testPayment = new Payment();
            testPayment.setId(999L);
            testPayment.setAmount(new java.math.BigDecimal("10000"));
            testPayment.setDescription("Test MoMo connection");

            String orderId = moMoPaymentService.generateOrderId(testPayment);

            response.put("success", true);
            response.put("message", "MoMo service is ready");
            response.put("testOrderId", orderId);
            response.put("timestamp", new Date());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "MoMo service error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/fake-success/{paymentId}")
    public ModelAndView fakeSuccess(@PathVariable Long paymentId) {
        try {
            // Fake
            Payment payment = paymentService.getPaymentById(paymentId);
            if (payment != null) {
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setPaymentDate(new Date());
                paymentService.updatePayment(payment);
            }

            return new ModelAndView("redirect:http://localhost:3000/payments?status=success&orderId=SCM_" + paymentId);
        } catch (Exception e) {
            return new ModelAndView("redirect:http://localhost:3000/payments?status=error");
        }
    }
    
    // === PRIVATE HELPER METHODS ===

    private Map<String, Object> createPaymentInfo(Payment payment) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", payment.getId());
        info.put("paymentCode", payment.getPaymentCode());
        info.put("amount", payment.getAmount());
        info.put("currency", payment.getCurrency());
        info.put("description", payment.getDescription());

        if (payment.getSupplier() != null) {
            info.put("supplierName", payment.getSupplier().getName());
        }

        return info;
    }

    private void updatePaymentStatus(Payment payment, String resultCode, String orderId, String transId) {
        try {
            if ("0".equals(resultCode)) {
                // Thanh toán thành công
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setPaymentDate(new Date());
                payment.setReferenceNumber(orderId);

                // Có thể lưu thêm transaction ID từ MoMo
                if (transId != null) {
                    String description = payment.getDescription() + " | MoMo TransID: " + transId;
                    payment.setDescription(description);
                }

                System.out.println("Payment completed: " + payment.getPaymentCode());

            } else {
                // Thanh toán thất bại
                payment.setStatus(Payment.PaymentStatus.FAILED);
                System.out.println("Payment failed: " + payment.getPaymentCode());
            }

            paymentService.updatePayment(payment);

        } catch (Exception e) {
            System.err.println(" Error updating payment status: " + e.getMessage());
        }
    }
}
