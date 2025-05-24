/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services.impl;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Inventory;
import com.nqbm.repositories.InventoryRepository;
import com.nqbm.services.InventoryService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {
    @Autowired
    private InventoryRepository inventoryRepository;
    
    @Override
    public List<Inventory> getInventories(Map<String, String> params) {
        return this.inventoryRepository.getInventories(params);
    }
    
    @Override
    public Inventory getInventoryById(Long id) {
        return this.inventoryRepository.getInventoryById(id);
    }
    
    @Override
    public Inventory getInventoryByProductId(Long productId) {
        return this.inventoryRepository.getInventoryByProductId(productId);
    }
    
    @Override
    public boolean addInventory(Inventory inventory) {
        return this.inventoryRepository.addInventory(inventory);
    }
    
    @Override
    public boolean updateInventory(Inventory inventory) {
        return this.inventoryRepository.updateInventory(inventory);
    }
    
    @Override
    public boolean deleteInventory(Long id) {
        return this.inventoryRepository.deleteInventory(id);
    }
    
    @Override
    public Long countInventories() {
        return this.inventoryRepository.countInventories();
    }
    
    @Override
    public List<Inventory> getLowStockItems() {
        return this.inventoryRepository.getLowStockItems();
    }
    
    @Override
    public List<Inventory> getOutOfStockItems() {
        return this.inventoryRepository.getOutOfStockItems();
    }
    
    @Override
    public Map<String, Object> getInventoryStatistics() {
        return this.inventoryRepository.getInventoryStatistics();
    }
    
    @Override
    public boolean increaseStock(Long inventoryId, Integer quantity, String reason) {
        return this.inventoryRepository.increaseStock(inventoryId, quantity, reason);
    }
    
    @Override
    public boolean decreaseStock(Long inventoryId, Integer quantity, String reason) {
        return this.inventoryRepository.decreaseStock(inventoryId, quantity, reason);
    }
    
    @Override
    public boolean adjustStock(Long inventoryId, Integer newQuantity, String reason) {
        return this.inventoryRepository.adjustStock(inventoryId, newQuantity, reason);
    }
}