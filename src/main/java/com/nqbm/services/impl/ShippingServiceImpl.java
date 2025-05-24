/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services.impl;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Shipping;
import com.nqbm.repositories.ShippingRepository;
import com.nqbm.services.ShippingService;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ShippingServiceImpl implements ShippingService {
    @Autowired
    private ShippingRepository shippingRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<Shipping> getShippings(Map<String, String> params) {
        return this.shippingRepository.getShippings(params);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Shipping getShippingById(Long id) {
        return this.shippingRepository.getShippingById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Shipping getShippingByTrackingNumber(String trackingNumber) {
        return this.shippingRepository.getShippingByTrackingNumber(trackingNumber);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Shipping getShippingByOrderId(Long orderId) {
        return this.shippingRepository.getShippingByOrderId(orderId);
    }
    
    @Override
    public boolean addShipping(Shipping shipping) {
        try {
            // Generate tracking number if not provided
            if (shipping.getTrackingNumber() == null || shipping.getTrackingNumber().isEmpty()) {
                shipping.generateTrackingNumber();
            }
            
            // Set default status if not provided
            if (shipping.getStatus() == null) {
                shipping.setStatus(Shipping.ShippingStatus.PENDING);
            }
            
            return this.shippingRepository.addShipping(shipping);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updateShipping(Shipping shipping) {
        try {
            if (shipping.getId() == null) {
                return false;
            }
            
            Shipping existingShipping = this.shippingRepository.getShippingById(shipping.getId());
            if (existingShipping == null) {
                return false;
            }
            
            // Update basic fields
            existingShipping.setPartnerName(shipping.getPartnerName());
            existingShipping.setServiceType(shipping.getServiceType());
            existingShipping.setDeliveryAddress(shipping.getDeliveryAddress());
            existingShipping.setRecipientName(shipping.getRecipientName());
            existingShipping.setRecipientPhone(shipping.getRecipientPhone());
            existingShipping.setScheduledDeliveryDate(shipping.getScheduledDeliveryDate());
            existingShipping.setActualDeliveryDate(shipping.getActualDeliveryDate());
            existingShipping.setStatus(shipping.getStatus());
            existingShipping.setShippingCost(shipping.getShippingCost());
            existingShipping.setNotes(shipping.getNotes());
            existingShipping.setActive(shipping.isActive());
            
            return this.shippingRepository.updateShipping(existingShipping);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteShipping(Long id) {
        return this.shippingRepository.deleteShipping(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countShippings() {
        return this.shippingRepository.countShippings();
    }
    
    // Status management methods
    
    @Override
    public boolean updateShippingStatus(Long shippingId, String status, String location, String notes) {
        try {
            Shipping shipping = this.shippingRepository.getShippingById(shippingId);
            if (shipping == null) {
                return false;
            }
            
            Shipping.ShippingStatus newStatus = Shipping.ShippingStatus.valueOf(status);
            shipping.setStatus(newStatus);
            
            // Update notes if provided
            if (notes != null && !notes.trim().isEmpty()) {
                String existingNotes = shipping.getNotes() != null ? shipping.getNotes() : "";
                shipping.setNotes(existingNotes + "\n[" + new Date() + "] " + notes);
            }
            
            // Set actual delivery date if status is DELIVERED
            if (newStatus == Shipping.ShippingStatus.DELIVERED && shipping.getActualDeliveryDate() == null) {
                shipping.setActualDeliveryDate(new Date());
            }
            
            return this.shippingRepository.updateShipping(shipping);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean schedulePickup(Long shippingId, String pickupDate) {
        try {
            Shipping shipping = this.shippingRepository.getShippingById(shippingId);
            if (shipping == null) {
                return false;
            }
            
            // For simplified entity, we just update status
            shipping.setStatus(Shipping.ShippingStatus.PICKED_UP);
            shipping.setNotes((shipping.getNotes() != null ? shipping.getNotes() : "") + 
                            "\n[" + new Date() + "] Scheduled pickup: " + pickupDate);
            
            return this.shippingRepository.updateShipping(shipping);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean confirmPickup(Long shippingId, String location, String notes) {
        try {
            Shipping shipping = this.shippingRepository.getShippingById(shippingId);
            if (shipping == null) {
                return false;
            }
            
            shipping.setStatus(Shipping.ShippingStatus.PICKED_UP);
            String noteText = "[" + new Date() + "] Pickup confirmed";
            if (location != null) noteText += " at " + location;
            if (notes != null) noteText += ". " + notes;
            
            shipping.setNotes((shipping.getNotes() != null ? shipping.getNotes() : "") + "\n" + noteText);
            
            return this.shippingRepository.updateShipping(shipping);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updateDeliverySchedule(Long shippingId, String deliveryDate) {
        try {
            Shipping shipping = this.shippingRepository.getShippingById(shippingId);
            if (shipping == null) {
                return false;
            }
            
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date scheduledDate = formatter.parse(deliveryDate);
            
            shipping.setScheduledDeliveryDate(scheduledDate);
            shipping.setNotes((shipping.getNotes() != null ? shipping.getNotes() : "") + 
                            "\n[" + new Date() + "] Delivery rescheduled to: " + deliveryDate);
            
            return this.shippingRepository.updateShipping(shipping);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean confirmDelivery(Long shippingId, String confirmationType, String notes) {
        try {
            Shipping shipping = this.shippingRepository.getShippingById(shippingId);
            if (shipping == null) {
                return false;
            }
            
            shipping.setActualDeliveryDate(new Date());
            shipping.setStatus(Shipping.ShippingStatus.DELIVERED);
            
            String noteText = "[" + new Date() + "] Delivery confirmed";
            if (confirmationType != null) noteText += " (" + confirmationType + ")";
            if (notes != null) noteText += ". " + notes;
            
            shipping.setNotes((shipping.getNotes() != null ? shipping.getNotes() : "") + "\n" + noteText);
            
            return this.shippingRepository.updateShipping(shipping);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Analytics and reports methods
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getShippingStatistics() {
        return this.shippingRepository.getShippingStatistics();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Shipping> getOverdueShippings() {
        return this.shippingRepository.getOverdueShippings();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Shipping> getShippingsByStatus(String status) {
        return this.shippingRepository.getShippingsByStatus(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPartnerPerformance() {
        return this.shippingRepository.getPartnerPerformance();
    }
    
    // Business logic methods
    
    @Override
    @Transactional(readOnly = true)
    public boolean canCancelShipping(Long shippingId) {
        Shipping shipping = this.shippingRepository.getShippingById(shippingId);
        if (shipping == null) {
            return false;
        }
        
        // Can cancel if not yet delivered or already cancelled
        return shipping.getStatus() != Shipping.ShippingStatus.DELIVERED 
            && shipping.getStatus() != Shipping.ShippingStatus.CANCELLED;
    }
    
    @Override
    public boolean cancelShipping(Long shippingId, String reason) {
        try {
            if (!canCancelShipping(shippingId)) {
                return false;
            }
            
            Shipping shipping = this.shippingRepository.getShippingById(shippingId);
            shipping.setStatus(Shipping.ShippingStatus.CANCELLED);
            
            String noteText = "[" + new Date() + "] Shipping cancelled";
            if (reason != null) noteText += ". Reason: " + reason;
            
            shipping.setNotes((shipping.getNotes() != null ? shipping.getNotes() : "") + "\n" + noteText);
            
            return this.shippingRepository.updateShipping(shipping);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public Double calculateShippingCost(String serviceType, Double distance, Double weight) {
        try {
            double baseCost = 0.0;
            double ratePerKm = 0.0;
            
            // Different rates based on service type
            switch (serviceType.toUpperCase()) {
                case "EXPRESS":
                    baseCost = 50000; // 50k VND base cost
                    ratePerKm = 2000; // 2k VND per km
                    break;
                case "STANDARD":
                    baseCost = 30000; // 30k VND base cost
                    ratePerKm = 1500; // 1.5k VND per km
                    break;
                default:
                    baseCost = 30000;
                    ratePerKm = 1500;
            }
            
            double totalCost = baseCost + (distance * ratePerKm);
            
            // Add weight factor (simplified since we removed weight field)
            if (weight != null && weight > 0) {
                totalCost += weight * 1000; // 1k VND per kg
            }
            
            return totalCost;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}