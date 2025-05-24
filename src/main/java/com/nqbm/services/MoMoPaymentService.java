/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Payment;
import java.util.Map;

public interface MoMoPaymentService {
    Map<String, Object> createMoMoPayment(Payment payment);
    boolean verifySignature(Map<String, String> params);
    String generateOrderId(Payment payment);
}
