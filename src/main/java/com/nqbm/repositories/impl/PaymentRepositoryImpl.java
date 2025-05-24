/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.repositories.impl;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Payment;
import com.nqbm.repositories.PaymentRepository;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PaymentRepositoryImpl implements PaymentRepository {
    @Autowired
    private SessionFactory sessionFactory;
    
    @Override
    public List<Payment> getPayments(Map<String, String> params) {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Payment> query = builder.createQuery(Payment.class);
        Root<Payment> root = query.from(Payment.class);
        query.select(root);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Tìm kiếm theo mã thanh toán
        if (params != null && params.containsKey("paymentCode")) {
            String paymentCode = params.get("paymentCode");
            if (paymentCode != null && !paymentCode.isEmpty()) {
                predicates.add(builder.like(root.get("paymentCode"), "%" + paymentCode + "%"));
            }
        }
        
        // Lọc theo nhà cung cấp
        if (params != null && params.containsKey("supplierId")) {
            String supplierId = params.get("supplierId");
            if (supplierId != null && !supplierId.isEmpty()) {
                predicates.add(builder.equal(root.get("supplier").get("id"), Long.parseLong(supplierId)));
            }
        }
        
        // Lọc theo đơn hàng
        if (params != null && params.containsKey("orderId")) {
            String orderId = params.get("orderId");
            if (orderId != null && !orderId.isEmpty()) {
                predicates.add(builder.equal(root.get("order").get("id"), Long.parseLong(orderId)));
            }
        }
        
        // Lọc theo trạng thái
        if (params != null && params.containsKey("status")) {
            String status = params.get("status");
            if (status != null && !status.isEmpty()) {
                predicates.add(builder.equal(root.get("status"), Payment.PaymentStatus.valueOf(status)));
            }
        }
        
        // Lọc theo loại thanh toán
        if (params != null && params.containsKey("paymentType")) {
            String paymentType = params.get("paymentType");
            if (paymentType != null && !paymentType.isEmpty()) {
                predicates.add(builder.equal(root.get("paymentType"), Payment.PaymentType.valueOf(paymentType)));
            }
        }
        
        // Lọc theo phương thức thanh toán
        if (params != null && params.containsKey("paymentMethod")) {
            String paymentMethod = params.get("paymentMethod");
            if (paymentMethod != null && !paymentMethod.isEmpty()) {
                predicates.add(builder.equal(root.get("paymentMethod"), Payment.PaymentMethod.valueOf(paymentMethod)));
            }
        }
        
        // Lọc theo khoảng thời gian
        if (params != null && params.containsKey("fromDate")) {
            String fromDate = params.get("fromDate");
            if (fromDate != null && !fromDate.isEmpty()) {
                try {
                    Date from = java.sql.Date.valueOf(fromDate);
                    predicates.add(builder.greaterThanOrEqualTo(root.get("paymentDate"), from));
                } catch (Exception e) {
                    System.err.println("Invalid fromDate format: " + fromDate);
                }
            }
        }
        
        if (params != null && params.containsKey("toDate")) {
            String toDate = params.get("toDate");
            if (toDate != null && !toDate.isEmpty()) {
                try {
                    Date to = java.sql.Date.valueOf(toDate);
                    predicates.add(builder.lessThanOrEqualTo(root.get("paymentDate"), to));
                } catch (Exception e) {
                    System.err.println("Invalid toDate format: " + toDate);
                }
            }
        }
        
        // Sắp xếp
        if (params != null && params.containsKey("sort")) {
            String sort = params.get("sort");
            if ("amount_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("amount")));
            } else if ("amount_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("amount")));
            } else if ("date_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("paymentDate")));
            } else if ("date_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("paymentDate")));
            } else if ("status_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("status")));
            } else if ("status_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("status")));
            }
        } else {
            query.orderBy(builder.desc(root.get("id")));
        }
        
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(Predicate[]::new));
        }
        
        // Phân trang
        Query<Payment> q = session.createQuery(query);
        
        if (params != null) {
            String page = params.get("page");
            if (page != null && !page.isEmpty()) {
                int pageNumber = Integer.parseInt(page);
                int pageSize = 10;
                
                q.setFirstResult((pageNumber - 1) * pageSize);
                q.setMaxResults(pageSize);
            }
        }
        
        return q.getResultList();
    }
    
    @Override
    public Payment getPaymentById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            return session.get(Payment.class, id);
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    @Override
    public Payment getPaymentByCode(String paymentCode) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            String hql = "FROM Payment p WHERE p.paymentCode = :paymentCode";
            Query<Payment> query = session.createQuery(hql, Payment.class);
            query.setParameter("paymentCode", paymentCode);
            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    @Override
    public boolean addPayment(Payment payment) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            session.persist(payment);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updatePayment(Payment payment) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            session.merge(payment);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deletePayment(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            Payment payment = session.get(Payment.class, id);
            if (payment != null) {
                session.remove(payment);
                return true;
            }
            return false;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public Long countPayments() {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Payment> root = query.from(Payment.class);
        
        query.select(builder.count(root));
        
        return session.createQuery(query).getSingleResult();
    }
    
    @Override
    public List<Payment> getPaymentsBySupplier(Long supplierId) {
        Session session = this.sessionFactory.getCurrentSession();
        String hql = "FROM Payment p WHERE p.supplier.id = :supplierId ORDER BY p.paymentDate DESC";
        Query<Payment> query = session.createQuery(hql, Payment.class);
        query.setParameter("supplierId", supplierId);
        return query.getResultList();
    }
    
    @Override
    public List<Payment> getPaymentsByOrder(Long orderId) {
        Session session = this.sessionFactory.getCurrentSession();
        String hql = "FROM Payment p WHERE p.order.id = :orderId ORDER BY p.paymentDate DESC";
        Query<Payment> query = session.createQuery(hql, Payment.class);
        query.setParameter("orderId", orderId);
        return query.getResultList();
    }
    
    @Override
    public List<Payment> getOverduePayments() {
        Session session = this.sessionFactory.getCurrentSession();
        String hql = "FROM Payment p WHERE p.status = :status AND p.dueDate < :now ORDER BY p.dueDate ASC";
        Query<Payment> query = session.createQuery(hql, Payment.class);
        query.setParameter("status", Payment.PaymentStatus.PENDING);
        query.setParameter("now", new Date());
        return query.getResultList();
    }
    
    @Override
    public BigDecimal getTotalPayableAmount() {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            String hql = "SELECT SUM(p.amount) FROM Payment p WHERE p.paymentType = :type AND p.status = :status";
            Query<BigDecimal> query = session.createQuery(hql, BigDecimal.class);
            query.setParameter("type", Payment.PaymentType.PAYABLE);
            query.setParameter("status", Payment.PaymentStatus.PENDING);
            BigDecimal result = query.getSingleResult();
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }
    
    @Override
    public BigDecimal getTotalReceivableAmount() {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            String hql = "SELECT SUM(p.amount) FROM Payment p WHERE p.paymentType = :type AND p.status = :status";
            Query<BigDecimal> query = session.createQuery(hql, BigDecimal.class);
            query.setParameter("type", Payment.PaymentType.RECEIVABLE);
            query.setParameter("status", Payment.PaymentStatus.PENDING);
            BigDecimal result = query.getSingleResult();
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }
}
