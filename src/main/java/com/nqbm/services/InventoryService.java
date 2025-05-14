/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services;

import com.nqbm.pojo.Inventory;
import com.nqbm.pojo.Product;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ADMIN
 */
public interface InventoryService {

    Integer getTotalProduct(Integer inventoryId);

    List<Product> getProductFromInventory(int inventoryId);

    void updateTotalProduct(int inventoryId, int newTotal);

    void updateDate(int inventoryId, Date lastUpdate);
    
    List<Inventory> getAllInventory();
}
