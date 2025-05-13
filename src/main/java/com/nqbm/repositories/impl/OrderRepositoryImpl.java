/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.repositories.impl;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Order;
import com.nqbm.repositories.OrderRepository;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
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
public class OrderRepositoryImpl implements OrderRepository {
    @Autowired
    private SessionFactory sessionFactory;
    
    @Override
    public List<Order> getOrders(Map<String, String> params) {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Order> query = builder.createQuery(Order.class);
        Root<Order> root = query.from(Order.class);
        query.select(root);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Tìm kiếm theo mã đơn hàng
        if (params != null && params.containsKey("orderNumber")) {
            String orderNumber = params.get("orderNumber");
            if (orderNumber != null && !orderNumber.isEmpty()) {
                predicates.add(builder.like(root.get("orderNumber"), "%" + orderNumber + "%"));
            }
        }
        
        // Tìm kiếm theo khách hàng
        if (params != null && params.containsKey("customer")) {
            String customer = params.get("customer");
            if (customer != null && !customer.isEmpty()) {
                predicates.add(builder.like(root.get("customerName"), "%" + customer + "%"));
            }
        }
        
        // Lọc theo trạng thái
        if (params != null && params.containsKey("status")) {
            String status = params.get("status");
            if (status != null && !status.isEmpty()) {
                predicates.add(builder.equal(root.get("status"), Order.OrderStatus.valueOf(status)));
            }
        }
        
        // Lọc theo loại đơn hàng
        if (params != null && params.containsKey("orderType")) {
            String orderType = params.get("orderType");
            if (orderType != null && !orderType.isEmpty()) {
                predicates.add(builder.equal(root.get("orderType"), Order.OrderType.valueOf(orderType)));
            }
        }
        
        // Lọc theo nhà cung cấp
        if (params != null && params.containsKey("supplierId")) {
            String supplierId = params.get("supplierId");
            if (supplierId != null && !supplierId.isEmpty()) {
                predicates.add(builder.equal(root.get("supplier").get("id"), Long.parseLong(supplierId)));
            }
        }
        
        // Sắp xếp
        if (params != null && params.containsKey("sort")) {
            String sort = params.get("sort");
            if ("date_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("orderDate")));
            } else if ("date_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("orderDate")));
            } else if ("amount_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("totalAmount")));
            } else if ("amount_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("totalAmount")));
            }
        } else {
            query.orderBy(builder.desc(root.get("orderDate")));
        }
        
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(Predicate[]::new));
        }
        
        // Phân trang
        Query<Order> q = session.createQuery(query);
        
        if (params != null) {
            String page = params.get("page");
            if (page != null && !page.isEmpty()) {
                int pageNumber = Integer.parseInt(page);
                int pageSize = 10; // Số mục mỗi trang
                
                q.setFirstResult((pageNumber - 1) * pageSize);
                q.setMaxResults(pageSize);
            }
        }
        
        return q.getResultList();
    }
    
    @Override
    public Order getOrderById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            return session.get(Order.class, id);
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    @Override
    public Order getOrderByOrderNumber(String orderNumber) {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Order> query = builder.createQuery(Order.class);
        Root<Order> root = query.from(Order.class);
        query.select(root);
        
        query.where(builder.equal(root.get("orderNumber"), orderNumber));
        
        try {
            return session.createQuery(query).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    @Override
    public boolean addOrder(Order order) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            session.persist(order);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateOrder(Order order) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            session.merge(order);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deleteOrder(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            Order order = session.get(Order.class, id);
            if (order != null) {
                session.remove(order);
                return true;
            }
            return false;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public Long countOrders() {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Order> root = query.from(Order.class);
        
        query.select(builder.count(root));
        
        return session.createQuery(query).getSingleResult();
    }
    
    @Override
    public Long countOrdersByStatus(Order.OrderStatus status) {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Order> root = query.from(Order.class);
        
        query.select(builder.count(root));
        query.where(builder.equal(root.get("status"), status));
        
        return session.createQuery(query).getSingleResult();
    }
}