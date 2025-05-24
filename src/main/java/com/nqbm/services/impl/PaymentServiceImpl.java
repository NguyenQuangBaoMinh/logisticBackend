/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services.impl;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Payment;
import com.nqbm.repositories.PaymentRepository;
import com.nqbm.services.PaymentService;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Override
    public List<Payment> getPayments(Map<String, String> params) {
        return this.paymentRepository.getPayments(params);
    }
    
    @Override
    public Payment getPaymentById(Long id) {
        return this.paymentRepository.getPaymentById(id);
    }
    
    @Override
    public Payment getPaymentByCode(String paymentCode) {
        return this.paymentRepository.getPaymentByCode(paymentCode);
    }
    
    @Override
    public boolean addPayment(Payment payment) {
        return this.paymentRepository.addPayment(payment);
    }
    
    @Override
    public boolean updatePayment(Payment payment) {
        return this.paymentRepository.updatePayment(payment);
    }
    
    @Override
    public boolean deletePayment(Long id) {
        return this.paymentRepository.deletePayment(id);
    }
    
    @Override
    public Long countPayments() {
        return this.paymentRepository.countPayments();
    }
    
    @Override
    public List<Payment> getPaymentsBySupplier(Long supplierId) {
        return this.paymentRepository.getPaymentsBySupplier(supplierId);
    }
    
    @Override
    public List<Payment> getPaymentsByOrder(Long orderId) {
        return this.paymentRepository.getPaymentsByOrder(orderId);
    }
    
    @Override
    public List<Payment> getOverduePayments() {
        return this.paymentRepository.getOverduePayments();
    }
    
    @Override
    public BigDecimal getTotalPayableAmount() {
        return this.paymentRepository.getTotalPayableAmount();
    }
    
    @Override
    public BigDecimal getTotalReceivableAmount() {
        return this.paymentRepository.getTotalReceivableAmount();
    }
    
    @Override
    public boolean processPayment(Long paymentId) {
        try {
            Payment payment = this.paymentRepository.getPaymentById(paymentId);
            if (payment != null && payment.getStatus() == Payment.PaymentStatus.PENDING) {
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setPaymentDate(new Date());
                return this.paymentRepository.updatePayment(payment);
            }
            return false;
        } catch (Exception ex) {
            System.err.println("Error processing payment: " + ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean cancelPayment(Long paymentId) {
        try {
            Payment payment = this.paymentRepository.getPaymentById(paymentId);
            if (payment != null && payment.getStatus() == Payment.PaymentStatus.PENDING) {
                payment.setStatus(Payment.PaymentStatus.CANCELLED);
                return this.paymentRepository.updatePayment(payment);
            }
            return false;
        } catch (Exception ex) {
            System.err.println("Error cancelling payment: " + ex.getMessage());
            return false;
        }
    }
}
