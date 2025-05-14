/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services.impl;

import com.nqbm.pojo.Inventory;
import com.nqbm.pojo.Product;
import com.nqbm.repositories.InventoryRepository;
import com.nqbm.services.InventoryService;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author ADMIN
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryRepository invenRepo;

    @Override
    public Integer getTotalProduct(Integer inventoryId) {
        return this.invenRepo.getTotalProduct(inventoryId);
    }

    @Override
    public List<Product> getProductFromInventory(int inventoryId) {
        return this.invenRepo.getProductFromInventory(inventoryId);
    }

    @Override
    public void updateTotalProduct(int inventoryId, int newTotal) {
        this.invenRepo.updateTotalProduct(inventoryId, newTotal);
    }

    @Override
    public void updateDate(int inventoryId, Date lastUpdate) {
        this.invenRepo.updateDate(inventoryId, lastUpdate);
    }

    @Override
    public List<Inventory> getAllInventory() {
        return this.invenRepo.getAllInventory();
    }
    

}
