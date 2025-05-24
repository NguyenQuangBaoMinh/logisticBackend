/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services.impl;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.services.ReportsService;
import com.nqbm.services.InventoryService;
import com.nqbm.services.SupplierService;
import com.nqbm.services.ProductService;
import com.nqbm.pojo.Inventory;
import com.nqbm.pojo.Supplier;
import com.nqbm.pojo.Product;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportsServiceImpl implements ReportsService {

    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private SupplierService supplierService;
    
    @Autowired
    private ProductService productService;

    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Lấy thống kê inventory có sẵn
        Map<String, Object> inventoryStats = inventoryService.getInventoryStatistics();
        
        // Thống kê suppliers
        Long totalSuppliers = supplierService.countSuppliers();
        
        // Thống kê products
        Long totalProducts = productService.countProducts();
        
        // Tổng hợp cho dashboard
        dashboard.put("totalSuppliers", totalSuppliers);
        dashboard.put("totalProducts", totalProducts);
        dashboard.put("inventoryStats", inventoryStats);
        dashboard.put("reportDate", new Date());
        
        return dashboard;
    }

    @Override
    public Map<String, Object> getInventoryReport() {
        Map<String, Object> report = new HashMap<>();
        
        // 1. Thống kê tổng quan
        Map<String, Object> overview = inventoryService.getInventoryStatistics();
        report.put("overview", overview);
        
        // 2. Hàng hóa hết hạn (out of stock)
        List<Inventory> outOfStockItems = inventoryService.getOutOfStockItems();
        List<Map<String, Object>> outOfStockReport = outOfStockItems.stream()
            .map(this::mapInventoryToReport)
            .collect(Collectors.toList());
        report.put("outOfStock", outOfStockReport);
        
        // 3. Hàng hóa sắp hết hạn (low stock)
        List<Inventory> lowStockItems = inventoryService.getLowStockItems();
        List<Map<String, Object>> lowStockReport = lowStockItems.stream()
            .map(this::mapInventoryToReport)
            .collect(Collectors.toList());
        report.put("lowStock", lowStockReport);
        
        // 4. Báo cáo theo vị trí kho
        List<Inventory> allInventory = inventoryService.getInventories(new HashMap<>());
        Map<String, Object> locationReport = getInventoryByLocation(allInventory);
        report.put("byLocation", locationReport);
        
        report.put("reportDate", new Date());
        return report;
    }

    @Override
    public Map<String, Object> getSupplierPerformanceReport() {
        Map<String, Object> report = new HashMap<>();
        
        // Lấy danh sách suppliers
        List<Supplier> suppliers = supplierService.getSuppliers(new HashMap<>());
        
        // Báo cáo hiệu suất từng supplier
        List<Map<String, Object>> performanceList = suppliers.stream()
            .map(this::mapSupplierPerformance)
            .sorted((a, b) -> {
                Integer ratingA = (Integer) a.get("rating");
                Integer ratingB = (Integer) b.get("rating");
                if (ratingA == null) ratingA = 0;
                if (ratingB == null) ratingB = 0;
                return ratingB.compareTo(ratingA);
            })
            .collect(Collectors.toList());
        
        report.put("suppliers", performanceList);
        
        // Thống kê tổng quan
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalSuppliers", suppliers.size());
        
        long activeSuppliers = suppliers.stream().filter(Supplier::isActive).count();
        summary.put("activeSuppliers", activeSuppliers);
        
        double avgRating = suppliers.stream()
            .filter(s -> s.getRating() != null && s.getRating() > 0)
            .mapToInt(Supplier::getRating)
            .average()
            .orElse(0.0);
        summary.put("averageRating", Math.round(avgRating * 10.0) / 10.0);
        
        long excellentSuppliers = suppliers.stream()
            .filter(s -> s.getRating() != null && s.getRating() >= 4)
            .count();
        summary.put("excellentSuppliers", excellentSuppliers);
        
        report.put("summary", summary);
        report.put("reportDate", new Date());
        
        return report;
    }

    // Helper methods
    private Map<String, Object> mapInventoryToReport(Inventory inventory) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", inventory.getId());
        item.put("productName", inventory.getProduct().getName());
        item.put("productSku", inventory.getProduct().getSku());
        item.put("location", inventory.getLocation());
        item.put("quantityOnHand", inventory.getQuantityOnHand());
        item.put("reorderPoint", inventory.getReorderPoint());
        item.put("totalValue", inventory.getTotalValue());
        item.put("supplierName", inventory.getProduct().getSupplier() != null ? 
                   inventory.getProduct().getSupplier().getName() : "N/A");
        return item;
    }

    private Map<String, Object> mapSupplierPerformance(Supplier supplier) {
        Map<String, Object> performance = new HashMap<>();
        performance.put("id", supplier.getId());
        performance.put("name", supplier.getName());
        performance.put("rating", supplier.getRating());
        performance.put("contactPerson", supplier.getContactPerson());
        performance.put("email", supplier.getEmail());
        performance.put("active", supplier.isActive());
        
        // Đếm số products của supplier
        List<Product> products = productService.getProductsBySupplier(supplier.getId());
        performance.put("productCount", products.size());
        
        // Tính tổng giá trị products
        BigDecimal totalValue = products.stream()
            .filter(p -> p.getUnitPrice() != null && p.getUnitInStock() != null)
            .map(p -> p.getUnitPrice().multiply(BigDecimal.valueOf(p.getUnitInStock())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        performance.put("totalProductValue", totalValue);
        
        // Đánh giá quality dựa trên rating
        String qualityLevel = "Chưa đánh giá";
        if (supplier.getRating() != null) {
            if (supplier.getRating() >= 4) qualityLevel = "Xuất sắc";
            else if (supplier.getRating() == 3) qualityLevel = "Tốt"; 
            else qualityLevel = "Cần cải thiện";
        }
        performance.put("qualityLevel", qualityLevel);
        
        return performance;
    }

    private Map<String, Object> getInventoryByLocation(List<Inventory> inventories) {
        Map<String, List<Inventory>> locationGroups = inventories.stream()
            .filter(inv -> inv.getLocation() != null)
            .collect(Collectors.groupingBy(Inventory::getLocation));
        
        Map<String, Object> locationReport = new HashMap<>();
        
        locationGroups.forEach((location, items) -> {
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("totalItems", items.size());
            
            BigDecimal totalValue = items.stream()
                .map(inv -> inv.getTotalValue() != null ? inv.getTotalValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            locationData.put("totalValue", totalValue);
            
            long outOfStock = items.stream().filter(inv -> inv.getQuantityOnHand() == 0).count();
            long lowStock = items.stream().filter(Inventory::needsReorder).count();
            
            locationData.put("outOfStockCount", outOfStock);
            locationData.put("lowStockCount", lowStock);
            
            locationReport.put(location, locationData);
        });
        
        return locationReport;
    }
}