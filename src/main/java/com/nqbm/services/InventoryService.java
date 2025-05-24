/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Inventory;
import java.util.List;
import java.util.Map;

public interface InventoryService {
    List<Inventory> getInventories(Map<String, String> params);
    Inventory getInventoryById(Long id);
    Inventory getInventoryByProductId(Long productId);
    boolean addInventory(Inventory inventory);
    boolean updateInventory(Inventory inventory);
    boolean deleteInventory(Long id);
    Long countInventories();
    List<Inventory> getLowStockItems();
    List<Inventory> getOutOfStockItems();
    Map<String, Object> getInventoryStatistics();

    boolean increaseStock(Long inventoryId, Integer quantity, String reason);
    boolean decreaseStock(Long inventoryId, Integer quantity, String reason);
    boolean adjustStock(Long inventoryId, Integer newQuantity, String reason);
}
