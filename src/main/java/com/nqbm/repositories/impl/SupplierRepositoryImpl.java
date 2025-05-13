/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.repositories.impl;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Supplier;
import com.nqbm.repositories.SupplierRepository;
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
public class SupplierRepositoryImpl implements SupplierRepository {
    @Autowired
    private SessionFactory sessionFactory;
    
    @Override
    public List<Supplier> getSuppliers(Map<String, String> params) {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Supplier> query = builder.createQuery(Supplier.class);
        Root<Supplier> root = query.from(Supplier.class);
        query.select(root);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Tìm kiếm theo tên
        if (params != null && params.containsKey("name")) {
            String name = params.get("name");
            if (name != null && !name.isEmpty()) {
                predicates.add(builder.like(root.get("name"), "%" + name + "%"));
            }
        }
        
        // Lọc theo trạng thái active
        if (params != null && params.containsKey("active")) {
            String active = params.get("active");
            if (active != null && !active.isEmpty()) {
                predicates.add(builder.equal(root.get("active"), "true".equals(active)));
            }
        }
        
        // Sắp xếp theo tên
        if (params != null && params.containsKey("sort")) {
            String sort = params.get("sort");
            if ("name_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("name")));
            } else if ("name_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("name")));
            } else if ("date_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("createdDate")));
            } else if ("date_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("createdDate")));
            } else if ("rating_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("rating")));
            } else if ("rating_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("rating")));
            }
        } else {
            query.orderBy(builder.desc(root.get("id")));
        }
        
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(Predicate[]::new));
        }
        
        // Phân trang
        Query<Supplier> q = session.createQuery(query);
        
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
    public Supplier getSupplierById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            return session.get(Supplier.class, id);
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    @Override
    public boolean addSupplier(Supplier supplier) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            session.persist(supplier);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateSupplier(Supplier supplier) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            session.merge(supplier);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deleteSupplier(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            Supplier supplier = session.get(Supplier.class, id);
            if (supplier != null) {
                session.remove(supplier);
                return true;
            }
            return false;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public Long countSuppliers() {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Supplier> root = query.from(Supplier.class);
        
        query.select(builder.count(root));
        
        return session.createQuery(query).getSingleResult();
    }
}
