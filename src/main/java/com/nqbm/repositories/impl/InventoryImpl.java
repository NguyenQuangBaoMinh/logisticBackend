/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.repositories.impl;

import com.nqbm.pojo.Inventory;
import com.nqbm.pojo.Product;
import com.nqbm.repositories.InventoryRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

/**
 *
 * @author ADMIN
 */
@Repository
@Transactional
public class InventoryImpl implements InventoryRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public Integer getTotalProduct(Integer inventoryId) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Integer> q = b.createQuery(Integer.class);
        Root root = q.from(Inventory.class);

        q.select(root.get("totalProduct").as(Integer.class));
        q.where(b.equal(root.get("id"), inventoryId));

        Query query = s.createQuery(q);
        return (int) query.getSingleResult();
    }

    @Override
    public List<Product> getProductFromInventory(int inventoryId) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Product> q = b.createQuery(Product.class);
        Root root = q.from(Product.class);

        q.select(root);
        q.where(b.equal(root.get("inventoryId"), inventoryId));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public void updateTotalProduct(int inventoryId, int newTotal) {
        Session s = this.factory.getObject().getCurrentSession();

        Inventory inventory = s.get(Inventory.class, inventoryId);
        if (inventory != null) {
            inventory.setTotalProduct(newTotal);
        }
    }

    @Override
    public void updateDate(int inventoryId, Date lastUpdate) {
        Session s = this.factory.getObject().getCurrentSession();

        Inventory inventory = s.get(Inventory.class, inventoryId);
        inventory.setLastUpdate(lastUpdate);
    }

    @Override
    public List<Inventory> getAllInventory() {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Inventory> q = b.createQuery(Inventory.class);
        Root root = q.from(Inventory.class);

        q.select(root);

        Query query = s.createQuery(q);
        return query.getResultList();
    }

}
