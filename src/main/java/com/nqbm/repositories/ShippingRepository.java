/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.repositories;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Shipping;
import java.util.List;
import java.util.Map;

public interface ShippingRepository {
    List<Shipping> getShippings(Map<String, String> params);
    Shipping getShippingById(Long id);
    Shipping getShippingByTrackingNumber(String trackingNumber);
    Shipping getShippingByOrderId(Long orderId);
    boolean addShipping(Shipping shipping);
    boolean updateShipping(Shipping shipping);
    boolean deleteShipping(Long id);
    Long countShippings();
    List<Shipping> getOverdueShippings();
    List<Shipping> getShippingsByStatus(String status);
    Map<String, Object> getShippingStatistics();
    List<Map<String, Object>> getPartnerPerformance();
}
