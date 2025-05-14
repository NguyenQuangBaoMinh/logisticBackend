/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.controllers;

import com.nqbm.pojo.Inventory;
import com.nqbm.pojo.Product;
import com.nqbm.services.InventoryService;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author ADMIN
 */
@RestController
@RequestMapping("/api/inventory")
public class ApiInventoryController {

    @Autowired
    private InventoryService invenService;

    @GetMapping("/{inventoryId}/total-product")
    public ResponseEntity<Integer> getTotalProduct(@PathVariable Integer inventoryId) {
        Integer total = invenService.getTotalProduct(inventoryId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/{inventoryId}/products")
    public ResponseEntity<List<Product>> getProducts(@PathVariable int inventoryId) {
        List<Product> products = invenService.getProductFromInventory(inventoryId);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{inventoryId}/total-product")
    public ResponseEntity<String> updateTotalProduct(
            @PathVariable int inventoryId,
            @RequestParam int newTotal) {
        invenService.updateTotalProduct(inventoryId, newTotal);
        return ResponseEntity.ok("Total product updated successfully.");
    }

    @PutMapping("/{inventoryId}/last-update")
    public ResponseEntity<String> updateLastUpdate(
            @PathVariable int inventoryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date lastUpdate) {
        invenService.updateDate(inventoryId, lastUpdate);
        return ResponseEntity.ok("Last update date changed successfully.");
    }

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventories() {
        List<Inventory> inventories = invenService.getAllInventory();
        return ResponseEntity.ok(inventories);
    }
}
