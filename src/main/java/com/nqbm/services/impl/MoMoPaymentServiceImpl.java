/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services.impl;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.configs.MoMoConfig;
import com.nqbm.pojo.Payment;
import com.nqbm.services.MoMoPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class MoMoPaymentServiceImpl implements MoMoPaymentService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Override
    public Map<String, Object> createMoMoPayment(Payment payment) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. Chuẩn bị thông tin cơ bản
            String orderId = generateOrderId(payment);
            String amount = String.valueOf(payment.getAmount().longValue());
            String orderInfo = "SCM Payment - " + payment.getDescription();
            String requestId = orderId;
            String extraData = "";
            
            // 2. Tạo signature
            String rawHash = "accessKey=" + MoMoConfig.ACCESS_KEY +
                           "&amount=" + amount +
                           "&extraData=" + extraData +
                           "&ipnUrl=" + MoMoConfig.NOTIFY_URL +
                           "&orderId=" + orderId +
                           "&orderInfo=" + orderInfo +
                           "&partnerCode=" + MoMoConfig.PARTNER_CODE +
                           "&redirectUrl=" + MoMoConfig.RETURN_URL +
                           "&requestId=" + requestId +
                           "&requestType=" + MoMoConfig.REQUEST_TYPE;
                           
            String signature = hmacSHA256(rawHash, MoMoConfig.SECRET_KEY);
            
            // 3. Tạo request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", MoMoConfig.PARTNER_CODE);
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", MoMoConfig.RETURN_URL);
            requestBody.put("ipnUrl", MoMoConfig.NOTIFY_URL);
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", MoMoConfig.REQUEST_TYPE);
            requestBody.put("signature", signature);
            requestBody.put("lang", "vi");
            
            // 4. Gửi request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                MoMoConfig.ENDPOINT, entity, Map.class);
            
            // 5. Xử lý response đơn giản
            if (response.getBody() != null) {
                result = response.getBody();
                result.put("orderId", orderId); // Thêm orderId để tracking
                
                // Log cho debug
                System.out.println("MoMo Response: " + result);
            }
            
        } catch (Exception e) {
            System.err.println("MoMo Payment Error: " + e.getMessage());
            result.put("resultCode", -1);
            result.put("message", "Lỗi tạo thanh toán: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public boolean verifySignature(Map<String, String> params) {
        try {
            String receivedSignature = params.get("signature");
            if (receivedSignature == null) return false;
            
            // Tạo lại signature từ params (loại bỏ signature)
            Map<String, String> sortedParams = new HashMap<>(params);
            sortedParams.remove("signature");
            
            StringBuilder rawHash = new StringBuilder();
            sortedParams.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    if (rawHash.length() > 0) rawHash.append("&");
                    rawHash.append(entry.getKey()).append("=").append(entry.getValue());
                });
            
            String computedSignature = hmacSHA256(rawHash.toString(), MoMoConfig.SECRET_KEY);
            
            return receivedSignature.equals(computedSignature);
            
        } catch (Exception e) {
            System.err.println("Signature verification failed: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public String generateOrderId(Payment payment) {
        return "SCM_" + payment.getId() + "_" + System.currentTimeMillis();
    }
    
    // Helper method
    private String hmacSHA256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
