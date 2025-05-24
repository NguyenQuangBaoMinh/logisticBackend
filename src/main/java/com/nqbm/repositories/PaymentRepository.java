/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.repositories;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Payment;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PaymentRepository {
    List<Payment> getPayments(Map<String, String> params);
    Payment getPaymentById(Long id);
    Payment getPaymentByCode(String paymentCode);
    boolean addPayment(Payment payment);
    boolean updatePayment(Payment payment);
    boolean deletePayment(Long id);
    Long countPayments();
    List<Payment> getPaymentsBySupplier(Long supplierId);
    List<Payment> getPaymentsByOrder(Long orderId);
    List<Payment> getOverduePayments();
    BigDecimal getTotalPayableAmount();
    BigDecimal getTotalReceivableAmount();
}
