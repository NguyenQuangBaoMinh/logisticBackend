/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.repositories.impl;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Inventory;
import com.nqbm.repositories.InventoryRepository;
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
public class InventoryRepositoryImpl implements InventoryRepository {
    @Autowired
    private SessionFactory sessionFactory;
    
    @Override
    public List<Inventory> getInventories(Map<String, String> params) {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Inventory> query = builder.createQuery(Inventory.class);
        Root<Inventory> root = query.from(Inventory.class);
        query.select(root);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Chỉ lấy inventory đang active
        predicates.add(builder.equal(root.get("active"), true));
        
        // Tìm kiếm theo từ khóa (tên product, SKU, location)
        if (params != null && params.containsKey("keyword")) {
            String keyword = params.get("keyword");
            if (keyword != null && !keyword.isEmpty()) {
                Predicate productName = builder.like(
                    builder.lower(root.get("product").get("name")), 
                    "%" + keyword.toLowerCase() + "%"
                );
                Predicate productSku = builder.like(
                    builder.lower(root.get("product").get("sku")), 
                    "%" + keyword.toLowerCase() + "%"
                );
                Predicate location = builder.like(
                    builder.lower(root.get("location")), 
                    "%" + keyword.toLowerCase() + "%"
                );
                predicates.add(builder.or(productName, productSku, location));
            }
        }
        
        // Lọc theo location
        if (params != null && params.containsKey("location")) {
            String location = params.get("location");
            if (location != null && !location.isEmpty()) {
                predicates.add(builder.equal(root.get("location"), location));
            }
        }
        
        // Lọc inventory có số lượng thấp
        if (params != null && params.containsKey("lowStock")) {
            String lowStock = params.get("lowStock");
            if ("true".equals(lowStock)) {
                predicates.add(builder.lessThanOrEqualTo(
                    root.get("quantityAvailable"), 
                    root.get("reorderPoint")
                ));
            }
        }
        
        // Lọc inventory hết hàng
        if (params != null && params.containsKey("outOfStock")) {
            String outOfStock = params.get("outOfStock");
            if ("true".equals(outOfStock)) {
                predicates.add(builder.equal(root.get("quantityOnHand"), 0));
            }
        }
        
        // Sắp xếp
        if (params != null && params.containsKey("sort")) {
            String sort = params.get("sort");
            if ("name_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("product").get("name")));
            } else if ("name_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("product").get("name")));
            } else if ("quantity_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("quantityOnHand")));
            } else if ("quantity_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("quantityOnHand")));
            } else if ("value_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("totalValue")));
            } else if ("value_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("totalValue")));
            } else if ("location_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("location")));
            } else if ("location_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("location")));
            } else if ("date_asc".equals(sort)) {
                query.orderBy(builder.asc(root.get("updatedDate")));
            } else if ("date_desc".equals(sort)) {
                query.orderBy(builder.desc(root.get("updatedDate")));
            }
        } else {
            query.orderBy(builder.desc(root.get("updatedDate")));
        }
        
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(Predicate[]::new));
        }
        
        // Phân trang
        Query<Inventory> q = session.createQuery(query);
        
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
    public Inventory getInventoryById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            return session.get(Inventory.class, id);
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    @Override
    public Inventory getInventoryByProductId(Long productId) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Inventory> query = builder.createQuery(Inventory.class);
            Root<Inventory> root = query.from(Inventory.class);
            
            query.select(root);
            query.where(
                builder.and(
                    builder.equal(root.get("product").get("id"), productId),
                    builder.equal(root.get("active"), true)
                )
            );
            
            return session.createQuery(query).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    @Override
    public boolean addInventory(Inventory inventory) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            session.persist(inventory);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateInventory(Inventory inventory) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            session.merge(inventory);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deleteInventory(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            Inventory inventory = session.get(Inventory.class, id);
            if (inventory != null) {
                // Soft delete - chỉ set active = false
                inventory.setActive(false);
                session.merge(inventory);
                return true;
            }
            return false;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public Long countInventories() {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Inventory> root = query.from(Inventory.class);
        
        query.select(builder.count(root));
        query.where(builder.equal(root.get("active"), true));
        
        return session.createQuery(query).getSingleResult();
    }
    
    @Override
    public List<Inventory> getLowStockItems() {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Inventory> query = builder.createQuery(Inventory.class);
        Root<Inventory> root = query.from(Inventory.class);
        
        query.select(root);
        query.where(
            builder.and(
                builder.lessThanOrEqualTo(root.get("quantityAvailable"), root.get("reorderPoint")),
                builder.equal(root.get("active"), true)
            )
        );
        query.orderBy(
            builder.asc(
                builder.diff(root.get("reorderPoint"), root.get("quantityAvailable"))
            )
        );
        
        return session.createQuery(query).getResultList();
    }
    
    @Override
    public List<Inventory> getOutOfStockItems() {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Inventory> query = builder.createQuery(Inventory.class);
        Root<Inventory> root = query.from(Inventory.class);
        
        query.select(root);
        query.where(
            builder.and(
                builder.equal(root.get("quantityOnHand"), 0),
                builder.equal(root.get("active"), true)
            )
        );
        query.orderBy(builder.desc(root.get("updatedDate")));
        
        return session.createQuery(query).getResultList();
    }
    
    @Override
    public Map<String, Object> getInventoryStatistics() {
        Session session = this.sessionFactory.getCurrentSession();
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Tổng số inventory items
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
            Root<Inventory> countRoot = countQuery.from(Inventory.class);
            countQuery.select(builder.count(countRoot));
            countQuery.where(builder.equal(countRoot.get("active"), true));
            Long totalItems = session.createQuery(countQuery).getSingleResult();
            stats.put("totalItems", totalItems);
            
            // Số items cần đặt hàng lại
            CriteriaQuery<Long> lowStockQuery = builder.createQuery(Long.class);
            Root<Inventory> lowStockRoot = lowStockQuery.from(Inventory.class);
            lowStockQuery.select(builder.count(lowStockRoot));
            lowStockQuery.where(
                builder.and(
                    builder.lessThanOrEqualTo(lowStockRoot.get("quantityAvailable"), lowStockRoot.get("reorderPoint")),
                    builder.equal(lowStockRoot.get("active"), true)
                )
            );
            Long lowStockCount = session.createQuery(lowStockQuery).getSingleResult();
            stats.put("lowStockCount", lowStockCount);
            
            // Số items hết hàng
            CriteriaQuery<Long> outOfStockQuery = builder.createQuery(Long.class);
            Root<Inventory> outOfStockRoot = outOfStockQuery.from(Inventory.class);
            outOfStockQuery.select(builder.count(outOfStockRoot));
            outOfStockQuery.where(
                builder.and(
                    builder.equal(outOfStockRoot.get("quantityOnHand"), 0),
                    builder.equal(outOfStockRoot.get("active"), true)
                )
            );
            Long outOfStockCount = session.createQuery(outOfStockQuery).getSingleResult();
            stats.put("outOfStockCount", outOfStockCount);
            
            // Số items vượt tồn kho tối đa
            CriteriaQuery<Long> overstockQuery = builder.createQuery(Long.class);
            Root<Inventory> overstockRoot = overstockQuery.from(Inventory.class);
            overstockQuery.select(builder.count(overstockRoot));
            overstockQuery.where(
                builder.and(
                    builder.greaterThan(overstockRoot.get("quantityOnHand"), overstockRoot.get("maximumStock")),
                    builder.equal(overstockRoot.get("active"), true)
                )
            );
            Long overstockCount = session.createQuery(overstockQuery).getSingleResult();
            stats.put("overstockCount", overstockCount);
            
            // Tổng giá trị tồn kho
            CriteriaQuery<BigDecimal> valueQuery = builder.createQuery(BigDecimal.class);
            Root<Inventory> valueRoot = valueQuery.from(Inventory.class);
            valueQuery.select(builder.sum(valueRoot.get("totalValue")));
            valueQuery.where(builder.equal(valueRoot.get("active"), true));
            BigDecimal totalValue = session.createQuery(valueQuery).getSingleResult();
            stats.put("totalValue", totalValue != null ? totalValue : BigDecimal.ZERO);
            
            // Số locations khác nhau
            Query<Long> locationQuery = session.createQuery(
                "SELECT COUNT(DISTINCT i.location) FROM Inventory i WHERE i.location IS NOT NULL AND i.active = true",
                Long.class
            );
            Long locationCount = locationQuery.getSingleResult();
            stats.put("locationCount", locationCount);
            
        } catch (Exception e) {
            System.err.println("Error getting inventory statistics: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    @Override
    public boolean increaseStock(Long inventoryId, Integer quantity, String reason) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            Inventory inventory = session.get(Inventory.class, inventoryId);
            if (inventory != null && quantity > 0) {
                inventory.setQuantityOnHand(inventory.getQuantityOnHand() + quantity);
                inventory.setLastStockDate(new java.util.Date());
                session.merge(inventory);
                return true;
            }
            return false;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean decreaseStock(Long inventoryId, Integer quantity, String reason) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            Inventory inventory = session.get(Inventory.class, inventoryId);
            if (inventory != null && quantity > 0 && inventory.getQuantityAvailable() >= quantity) {
                inventory.setQuantityOnHand(inventory.getQuantityOnHand() - quantity);
                inventory.setLastStockDate(new java.util.Date());
                session.merge(inventory);
                return true;
            }
            return false;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean adjustStock(Long inventoryId, Integer newQuantity, String reason) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            Inventory inventory = session.get(Inventory.class, inventoryId);
            if (inventory != null && newQuantity >= 0) {
                inventory.setQuantityOnHand(newQuantity);
                inventory.setLastStockDate(new java.util.Date());
                session.merge(inventory);
                return true;
            }
            return false;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
}