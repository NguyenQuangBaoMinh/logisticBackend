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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author ADMIN
 */
@Controller
public class InventoryController {

    @Autowired
    private InventoryService invenService;

    @GetMapping
    public String listInventory(Model model) {
        List<Inventory> inventories = invenService.getAllInventory();
        model.addAttribute("inventories", inventories);
        return "inventory/list";
    }

    @GetMapping("/{inventoryId}/products")
    public String listProducts(@PathVariable int inventoryId, Model model) {
        List<Product> products = invenService.getProductFromInventory(inventoryId);
        model.addAttribute("products", products);
        model.addAttribute("inventoryId", inventoryId);
        return "inventory/products"; 
    }

    @GetMapping("/{inventoryId}/edit-total")
    public String editTotalProductForm(@PathVariable int inventoryId, Model model) {
        model.addAttribute("inventoryId", inventoryId);
        model.addAttribute("currentTotal", invenService.getTotalProduct(inventoryId));
        return "inventory/edit-total";
    }

    @PostMapping("/{inventoryId}/edit-total")
    public String updateTotalProduct(@PathVariable int inventoryId,
            @RequestParam int newTotal) {
        invenService.updateTotalProduct(inventoryId, newTotal);
        return "redirect:/inventory";
    }

    @GetMapping("/{inventoryId}/edit-date")
    public String editDateForm(@PathVariable int inventoryId, Model model) {
        model.addAttribute("inventoryId", inventoryId);
        return "inventory/edit-date";
    }

    @PostMapping("/{inventoryId}/edit-date")
    public String updateDate(@PathVariable int inventoryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date lastUpdate) {
        invenService.updateDate(inventoryId, lastUpdate);
        return "redirect:/inventory";
    }

}
