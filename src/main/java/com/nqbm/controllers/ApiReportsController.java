/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.controllers;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.services.ReportsService;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ApiReportsController {

    @Autowired
    private ReportsService reportsService;

    /**
     * 1. Dashboard tổng quan
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> dashboard = reportsService.getDashboardData();
            response.put("success", true);
            response.put("data", dashboard);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi lấy dữ liệu dashboard: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 2. Báo cáo tồn kho chi tiết
     */
    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Object>> getInventoryReport() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> report = reportsService.getInventoryReport();
            response.put("success", true);
            response.put("report", report);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi lấy báo cáo tồn kho: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 3. Báo cáo hiệu suất nhà cung cấp
     */
    @GetMapping("/supplier-performance")
    public ResponseEntity<Map<String, Object>> getSupplierPerformanceReport() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> report = reportsService.getSupplierPerformanceReport();
            response.put("success", true);
            response.put("report", report);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi lấy báo cáo hiệu suất nhà cung cấp: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
