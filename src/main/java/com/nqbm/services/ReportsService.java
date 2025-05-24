/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services;

/**
 *
 * @author baominh14022004gmail.com
 */
import java.util.Map;

public interface ReportsService {
    Map<String, Object> getDashboardData();
    Map<String, Object> getInventoryReport();
    Map<String, Object> getSupplierPerformanceReport();
}
