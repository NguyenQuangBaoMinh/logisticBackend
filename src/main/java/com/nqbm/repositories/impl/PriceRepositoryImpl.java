/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.repositories.impl;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Price;
import com.nqbm.repositories.PriceRepository;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
public class PriceRepositoryImpl implements PriceRepository {
    @Autowired
    private SessionFactory sessionFactory;
    
    @Override
    public List<Price> getPrices(Map<String, String> params) {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Price> query = builder.createQuery(Price.class);
        Root<Price> root = query.from(Price.class);
        query.select(root);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Tìm kiếm theo nhà cung cấp
        if (params != null && params.containsKey("supplierId")) {
            String supplierId = params.get("supplierId");
            if (supplierId != null && !supplierId.isEmpty()) {
                predicates.add(builder.equal(root.get("supplier").get("id"), Long.parseLong(supplierId)));
            }
        }
        
        // Tìm kiếm theo sản phẩm
        if (params != null && params.containsKey("productId")) {
            String productId = params.get("productId");
            if (productId != null && !productId.isEmpty()) {
                predicates.add(builder.equal(root.get("product").get("id"), Long.parseLong(productId)));
            }
        }
        
        // Lọc theo trạng thái active
        if (params != null && params.containsKey("active")) {
            String active = params.get("active");
            if (active != null && !active.isEmpty()) {
                predicates.add(builder.equal(root.get("active"), "true".equals(active)));
            }
        }
        
        // Lọc theo giá từ - đến
        if (params != null && params.containsKey("minPrice")) {
            String minPrice = params.get("minPrice");
            if (minPrice != null && !minPrice.isEmpty()) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("unitPrice"), new java.math.BigDecimal(minPrice)));
            }
        }
        
        if (params != null && params.containsKey("maxPrice")) {
            String maxPrice = params.get("maxPrice");
            if (maxPrice != null && !maxPrice.isEmpty()) {
                predicates.add(builder.lessThanOrEqualTo(root.get("unitPrice"), new java.math.BigDecimal(maxPrice)));
            }
        }
        
        // Lọc giá còn hiệu lực
        if (params != null && params.containsKey("currentOnly")) {
            String currentOnly = params.get("currentOnly");
            if ("true".equals(currentOnly)) {
                Date now = new Date();
                predicates.add(builder.lessThanOrEqualTo(root.get("effectiveDate"), now));
                predicates.add(builder.or(
                    builder.isNull(root.get("expiryDate")),
                    builder.greaterThanOrEqualTo(root.get("expiryDate"), now)
                ));
            }
        }
        
        // Sắp xếp
        if (params != null && params.containsKey("sort")) {
            String sort = params.get("sort");
            if ("price_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("unitPrice")));
            } else if ("price_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("unitPrice")));
            } else if ("date_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("createdDate")));
            } else if ("date_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("createdDate")));
            }
        } else {
            query.orderBy(builder.desc(root.get("id")));
        }
        
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(Predicate[]::new));
        }
        
        // Phân trang
        Query<Price> q = session.createQuery(query);
        
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
    public Price getPriceById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            return session.get(Price.class, id);
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    @Override
    public boolean addPrice(Price price) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            session.persist(price);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updatePrice(Price price) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            session.merge(price);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deletePrice(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            Price price = session.get(Price.class, id);
            if (price != null) {
                session.remove(price);
                return true;
            }
            return false;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public Long countPrices() {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Price> root = query.from(Price.class);
        
        query.select(builder.count(root));
        
        return session.createQuery(query).getSingleResult();
    }
    
    @Override
    public List<Price> getPricesBySupplier(Long supplierId) {
        Session session = this.sessionFactory.getCurrentSession();
        String hql = "FROM Price p WHERE p.supplier.id = :supplierId AND p.active = true ORDER BY p.createdDate DESC";
        Query<Price> query = session.createQuery(hql, Price.class);
        query.setParameter("supplierId", supplierId);
        return query.getResultList();
    }
    
    @Override
    public List<Price> getPricesByProduct(Long productId) {
        Session session = this.sessionFactory.getCurrentSession();
        String hql = "FROM Price p WHERE p.product.id = :productId AND p.active = true ORDER BY p.unitPrice ASC";
        Query<Price> query = session.createQuery(hql, Price.class);
        query.setParameter("productId", productId);
        return query.getResultList();
    }
    
    @Override
    public Price getCurrentPriceBySupplierAndProduct(Long supplierId, Long productId) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            String hql = "FROM Price p WHERE p.supplier.id = :supplierId AND p.product.id = :productId " +
                        "AND p.active = true AND p.effectiveDate <= :now " +
                        "AND (p.expiryDate IS NULL OR p.expiryDate >= :now) " +
                        "ORDER BY p.effectiveDate DESC";
            Query<Price> query = session.createQuery(hql, Price.class);
            query.setParameter("supplierId", supplierId);
            query.setParameter("productId", productId);
            query.setParameter("now", new Date());
            query.setMaxResults(1);
            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
}
