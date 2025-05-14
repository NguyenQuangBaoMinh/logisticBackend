/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.repositories;

import com.nqbm.pojo.Inventory;
import com.nqbm.pojo.Product;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ADMIN
 */
public interface InventoryRepository {
    List<Inventory> getAllInventory();
    Integer getTotalProduct(Integer inventoryId);
    List<Product> getProductFromInventory(int inventoryId);
    void updateTotalProduct(int inventoryId, int newTotal);
    void updateDate(int inventoryId, Date lastUpdate);
}
