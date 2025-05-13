/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.repositories.impl;

/**
 *
 * @author baominh14022004gmail.com
 */


import com.nqbm.pojo.Product;
import com.nqbm.repositories.ProductRepository;
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
public class ProductRepositoryImpl implements ProductRepository {
    @Autowired
    private SessionFactory sessionFactory;
    
    @Override
    public List<Product> getProducts(Map<String, String> params) {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Product> query = builder.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);
        query.select(root);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Tìm kiếm theo tên
        if (params != null && params.containsKey("name")) {
            String name = params.get("name");
            if (name != null && !name.isEmpty()) {
                predicates.add(builder.like(root.get("name"), "%" + name + "%"));
            }
        }
        
        // Tìm kiếm theo supplier
        if (params != null && params.containsKey("supplierId")) {
            String supplierId = params.get("supplierId");
            if (supplierId != null && !supplierId.isEmpty()) {
                predicates.add(builder.equal(root.get("supplier").get("id"), Long.parseLong(supplierId)));
            }
        }
        
        // Lọc theo trạng thái active
        if (params != null && params.containsKey("active")) {
            String active = params.get("active");
            if (active != null && !active.isEmpty()) {
                predicates.add(builder.equal(root.get("active"), "true".equals(active)));
            }
        }
        
        // Lọc theo tồn kho
        if (params != null && params.containsKey("inStock")) {
            String inStock = params.get("inStock");
            if (inStock != null && !inStock.isEmpty()) {
                if ("true".equals(inStock)) {
                    predicates.add(builder.greaterThan(root.get("unitInStock"), 0));
                } else {
                    predicates.add(builder.equal(root.get("unitInStock"), 0));
                }
            }
        }
        
        // Sắp xếp
        if (params != null && params.containsKey("sort")) {
            String sort = params.get("sort");
            if ("name_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("name")));
            } else if ("name_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("name")));
            } else if ("price_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("unitPrice")));
            } else if ("price_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("unitPrice")));
            } else if ("stock_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("unitInStock")));
            } else if ("stock_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("unitInStock")));
            } else if ("date_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("createdDate")));
            } else if ("date_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("createdDate")));
            }
        } else {
            query.orderBy(builder.desc(root.get("id")));
        }
        
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(Predicate[]::new));
        }
        
        // Phân trang
        Query<Product> q = session.createQuery(query);
        
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
    public Product getProductById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            return session.get(Product.class, id);
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    @Override
    public boolean addProduct(Product product) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            session.persist(product);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateProduct(Product product) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            session.merge(product);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deleteProduct(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            Product product = session.get(Product.class, id);
            if (product != null) {
                session.remove(product);
                return true;
            }
            return false;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public Long countProducts() {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Product> root = query.from(Product.class);
        
        query.select(builder.count(root));
        
        return session.createQuery(query).getSingleResult();
    }
    
    @Override
    public List<Product> getProductsBySupplier(Long supplierId) {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Product> query = builder.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);
        
        query.select(root);
        
        Predicate supplierPredicate = builder.equal(root.get("supplier").get("id"), supplierId);
        Predicate activePredicate = builder.equal(root.get("active"), true);
        
        query.where(builder.and(supplierPredicate, activePredicate));
        query.orderBy(builder.asc(root.get("name")));
        
        return session.createQuery(query).getResultList();
    }
}
