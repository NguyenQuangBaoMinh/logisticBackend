/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Shipping;
import java.util.List;
import java.util.Map;

public interface ShippingService {
    List<Shipping> getShippings(Map<String, String> params);
    Shipping getShippingById(Long id);
    Shipping getShippingByTrackingNumber(String trackingNumber);
    Shipping getShippingByOrderId(Long orderId);
    boolean addShipping(Shipping shipping);
    boolean updateShipping(Shipping shipping);
    boolean deleteShipping(Long id);
    Long countShippings();
    
    // Shipping status management
    boolean updateShippingStatus(Long shippingId, String status, String location, String notes);
    boolean schedulePickup(Long shippingId, String pickupDate);
    boolean confirmPickup(Long shippingId, String location, String notes);
    boolean updateDeliverySchedule(Long shippingId, String deliveryDate);
    boolean confirmDelivery(Long shippingId, String confirmationType, String notes);
    
    // Analytics and reports
    Map<String, Object> getShippingStatistics();
    List<Shipping> getOverdueShippings();
    List<Shipping> getShippingsByStatus(String status);
    List<Map<String, Object>> getPartnerPerformance();
    
    // Business logic
    boolean canCancelShipping(Long shippingId);
    boolean cancelShipping(Long shippingId, String reason);
    Double calculateShippingCost(String serviceType, Double distance, Double weight);
}
