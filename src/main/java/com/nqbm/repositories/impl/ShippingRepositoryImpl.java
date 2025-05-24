/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.repositories.impl;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Shipping;
import com.nqbm.repositories.ShippingRepository;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
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
public class ShippingRepositoryImpl implements ShippingRepository {
    @Autowired
    private SessionFactory sessionFactory;
    
    @Override
    public List<Shipping> getShippings(Map<String, String> params) {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Shipping> query = builder.createQuery(Shipping.class);
        Root<Shipping> root = query.from(Shipping.class);
        query.select(root);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Chỉ lấy shipping đang active
        predicates.add(builder.equal(root.get("active"), true));
        
        // Tìm kiếm theo tracking number hoặc recipient name
        if (params != null && params.containsKey("keyword")) {
            String keyword = params.get("keyword");
            if (keyword != null && !keyword.isEmpty()) {
                Predicate trackingNumber = builder.like(
                    builder.lower(root.get("trackingNumber")), 
                    "%" + keyword.toLowerCase() + "%"
                );
                Predicate recipientName = builder.like(
                    builder.lower(root.get("recipientName")), 
                    "%" + keyword.toLowerCase() + "%"
                );
                Predicate partnerName = builder.like(
                    builder.lower(root.get("partnerName")), 
                    "%" + keyword.toLowerCase() + "%"
                );
                predicates.add(builder.or(trackingNumber, recipientName, partnerName));
            }
        }
        
        // Lọc theo status
        if (params != null && params.containsKey("status")) {
            String status = params.get("status");
            if (status != null && !status.isEmpty()) {
                predicates.add(builder.equal(root.get("status"), Shipping.ShippingStatus.valueOf(status)));
            }
        }
        
        // Lọc theo partner
        if (params != null && params.containsKey("partner")) {
            String partner = params.get("partner");
            if (partner != null && !partner.isEmpty()) {
                predicates.add(builder.like(
                    builder.lower(root.get("partnerName")), 
                    "%" + partner.toLowerCase() + "%"
                ));
            }
        }
        
        // Lọc theo service type
        if (params != null && params.containsKey("serviceType")) {
            String serviceType = params.get("serviceType");
            if (serviceType != null && !serviceType.isEmpty()) {
                predicates.add(builder.equal(root.get("serviceType"), serviceType));
            }
        }
        
        // Lọc theo overdue
        if (params != null && params.containsKey("overdue")) {
            String overdue = params.get("overdue");
            if ("true".equals(overdue)) {
                predicates.add(builder.lessThan(root.get("scheduledDeliveryDate"), new java.util.Date()));
                predicates.add(builder.notEqual(root.get("status"), Shipping.ShippingStatus.DELIVERED));
                predicates.add(builder.notEqual(root.get("status"), Shipping.ShippingStatus.CANCELLED));
            }
        }
        
        // Sắp xếp
        if (params != null && params.containsKey("sort")) {
            String sort = params.get("sort");
            if ("tracking_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("trackingNumber")));
            } else if ("tracking_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("trackingNumber")));
            } else if ("delivery_date_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("scheduledDeliveryDate")));
            } else if ("delivery_date_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("scheduledDeliveryDate")));
            } else if ("status_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("status")));
            } else if ("status_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("status")));
            } else if ("partner_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("partnerName")));
            } else if ("partner_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("partnerName")));
            } else if ("created_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("createdDate")));
            } else if ("created_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("createdDate")));
            }
        } else {
            query.orderBy(builder.desc(root.get("createdDate")));
        }
        
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(Predicate[]::new));
        }
        
        // Phân trang
        Query<Shipping> q = session.createQuery(query);
        
        if (params != null) {
            String page = params.get("page");
            String size = params.get("size");
            if (page != null && !page.isEmpty()) {
                int pageNumber = Integer.parseInt(page);
                int pageSize = size != null && !size.isEmpty() ? Integer.parseInt(size) : 10;
                
                q.setFirstResult(pageNumber * pageSize);
                q.setMaxResults(pageSize);
            }
        }
        
        return q.getResultList();
    }
    
    @Override
    public Shipping getShippingById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            return session.get(Shipping.class, id);
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    @Override
    public Shipping getShippingByTrackingNumber(String trackingNumber) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Shipping> query = builder.createQuery(Shipping.class);
            Root<Shipping> root = query.from(Shipping.class);
            
            query.select(root);
            query.where(
                builder.and(
                    builder.equal(root.get("trackingNumber"), trackingNumber),
                    builder.equal(root.get("active"), true)
                )
            );
            
            return session.createQuery(query).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    @Override
    public Shipping getShippingByOrderId(Long orderId) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Shipping> query = builder.createQuery(Shipping.class);
            Root<Shipping> root = query.from(Shipping.class);
            
            query.select(root);
            query.where(
                builder.and(
                    builder.equal(root.get("order").get("id"), orderId),
                    builder.equal(root.get("active"), true)
                )
            );
            
            return session.createQuery(query).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    @Override
    public boolean addShipping(Shipping shipping) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            session.persist(shipping);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateShipping(Shipping shipping) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            session.merge(shipping);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deleteShipping(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            Shipping shipping = session.get(Shipping.class, id);
            if (shipping != null) {
                // Soft delete - chỉ set active = false
                shipping.setActive(false);
                session.merge(shipping);
                return true;
            }
            return false;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public Long countShippings() {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Shipping> root = query.from(Shipping.class);
        
        query.select(builder.count(root));
        query.where(builder.equal(root.get("active"), true));
        
        return session.createQuery(query).getSingleResult();
    }
    
    @Override
    public List<Shipping> getOverdueShippings() {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Shipping> query = builder.createQuery(Shipping.class);
        Root<Shipping> root = query.from(Shipping.class);
        
        query.select(root);
        query.where(
            builder.and(
                builder.lessThan(root.get("scheduledDeliveryDate"), new java.util.Date()),
                builder.notEqual(root.get("status"), Shipping.ShippingStatus.DELIVERED),
                builder.notEqual(root.get("status"), Shipping.ShippingStatus.CANCELLED),
                builder.equal(root.get("active"), true)
            )
        );
        query.orderBy(builder.asc(root.get("scheduledDeliveryDate")));
        
        return session.createQuery(query).getResultList();
    }
    
    @Override
    public List<Shipping> getShippingsByStatus(String status) {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Shipping> query = builder.createQuery(Shipping.class);
        Root<Shipping> root = query.from(Shipping.class);
        
        query.select(root);
        query.where(
            builder.and(
                builder.equal(root.get("status"), Shipping.ShippingStatus.valueOf(status)),
                builder.equal(root.get("active"), true)
            )
        );
        query.orderBy(builder.desc(root.get("createdDate")));
        
        return session.createQuery(query).getResultList();
    }
    
    @Override
    public Map<String, Object> getShippingStatistics() {
        Session session = this.sessionFactory.getCurrentSession();
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Tổng số shipments
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
            Root<Shipping> countRoot = countQuery.from(Shipping.class);
            countQuery.select(builder.count(countRoot));
            countQuery.where(builder.equal(countRoot.get("active"), true));
            Long totalShipments = session.createQuery(countQuery).getSingleResult();
            stats.put("totalShipments", totalShipments);
            
            // Shipments theo status
            for (Shipping.ShippingStatus status : Shipping.ShippingStatus.values()) {
                CriteriaQuery<Long> statusQuery = builder.createQuery(Long.class);
                Root<Shipping> statusRoot = statusQuery.from(Shipping.class);
                statusQuery.select(builder.count(statusRoot));
                statusQuery.where(
                    builder.and(
                        builder.equal(statusRoot.get("status"), status),
                        builder.equal(statusRoot.get("active"), true)
                    )
                );
                Long count = session.createQuery(statusQuery).getSingleResult();
                stats.put(status.name().toLowerCase() + "Count", count);
            }
            
            // Overdue shipments
            CriteriaQuery<Long> overdueQuery = builder.createQuery(Long.class);
            Root<Shipping> overdueRoot = overdueQuery.from(Shipping.class);
            overdueQuery.select(builder.count(overdueRoot));
            overdueQuery.where(
                builder.and(
                    builder.lessThan(overdueRoot.get("scheduledDeliveryDate"), new java.util.Date()),
                    builder.notEqual(overdueRoot.get("status"), Shipping.ShippingStatus.DELIVERED),
                    builder.notEqual(overdueRoot.get("status"), Shipping.ShippingStatus.CANCELLED),
                    builder.equal(overdueRoot.get("active"), true)
                )
            );
            Long overdueCount = session.createQuery(overdueQuery).getSingleResult();
            stats.put("overdueCount", overdueCount);
            
            // Tổng chi phí vận chuyển
            CriteriaQuery<BigDecimal> costQuery = builder.createQuery(BigDecimal.class);
            Root<Shipping> costRoot = costQuery.from(Shipping.class);
            costQuery.select(builder.sum(costRoot.get("shippingCost")));
            costQuery.where(builder.equal(costRoot.get("active"), true));
            BigDecimal totalCost = session.createQuery(costQuery).getSingleResult();
            stats.put("totalShippingCost", totalCost != null ? totalCost : BigDecimal.ZERO);
            
            // Số partners khác nhau
            Query<Long> partnerQuery = session.createQuery(
                "SELECT COUNT(DISTINCT s.partnerName) FROM Shipping s WHERE s.partnerName IS NOT NULL AND s.active = true",
                Long.class
            );
            Long partnerCount = partnerQuery.getSingleResult();
            stats.put("partnerCount", partnerCount);
            
            // Average delivery time (for delivered shipments)
            Query<Double> avgDeliveryQuery = session.createQuery(
                "SELECT AVG(s.actualDeliveryDate - s.actualPickupDate) FROM Shipping s " +
                "WHERE s.actualDeliveryDate IS NOT NULL AND s.actualPickupDate IS NOT NULL AND s.active = true",
                Double.class
            );
            Double avgDeliveryTime = avgDeliveryQuery.getSingleResult();
            stats.put("averageDeliveryDays", avgDeliveryTime != null ? avgDeliveryTime / (24 * 60 * 60 * 1000) : 0);
            
        } catch (Exception e) {
            System.err.println("Error getting shipping statistics: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getPartnerPerformance() {
        Session session = this.sessionFactory.getCurrentSession();
        
        try {
            Query<Object[]> query = session.createQuery(
                "SELECT s.partnerName, " +
                "COUNT(s) as totalShipments, " +
                "SUM(CASE WHEN s.status = 'DELIVERED' THEN 1 ELSE 0 END) as deliveredCount, " +
                "AVG(s.partnerRating) as avgRating, " +
                "COUNT(CASE WHEN s.actualDeliveryDate <= s.scheduledDeliveryDate THEN 1 END) as onTimeCount, " +
                "SUM(s.shippingCost) as totalCost " +
                "FROM Shipping s " +
                "WHERE s.partnerName IS NOT NULL AND s.active = true " +
                "GROUP BY s.partnerName " +
                "ORDER BY totalShipments DESC",
                Object[].class
            );
            
            List<Object[]> results = query.getResultList();
            List<Map<String, Object>> performance = new ArrayList<>();
            
            for (Object[] row : results) {
                Map<String, Object> partner = new HashMap<>();
                partner.put("partnerName", row[0]);
                partner.put("totalShipments", row[1]);
                partner.put("deliveredCount", row[2]);
                partner.put("avgRating", row[3]);
                partner.put("onTimeCount", row[4]);
                partner.put("totalCost", row[5]);
                
                // Calculate performance metrics
                Long total = (Long) row[1];
                Long delivered = (Long) row[2];
                Long onTime = (Long) row[4];
                
                if (total > 0) {
                    partner.put("deliveryRate", (delivered.doubleValue() / total.doubleValue()) * 100);
                    partner.put("onTimeRate", (onTime.doubleValue() / total.doubleValue()) * 100);
                } else {
                    partner.put("deliveryRate", 0.0);
                    partner.put("onTimeRate", 0.0);
                }
                
                performance.add(partner);
            }
            
            return performance;
        } catch (Exception e) {
            System.err.println("Error getting partner performance: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
