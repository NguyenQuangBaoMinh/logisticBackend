/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.pojo;

/**
 *
 * @author baominh14022004gmail.com
 */

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "shipping")
public class Shipping implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tracking_number", unique = true, nullable = false)
    private String trackingNumber;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    @Column(name = "partner_name")
    private String partnerName;
    
    @Column(name = "service_type")
    private String serviceType;
    
    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;
    
    @Column(name = "recipient_name")
    private String recipientName;
    
    @Column(name = "recipient_phone")
    private String recipientPhone;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "scheduled_delivery_date")
    private Date scheduledDeliveryDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "actual_delivery_date")
    private Date actualDeliveryDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ShippingStatus status = ShippingStatus.PENDING;
    
    @Column(name = "shipping_cost", precision = 12, scale = 2)
    private BigDecimal shippingCost;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "is_active")
    private boolean active = true;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    private Date createdDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_date")
    private Date updatedDate;
    
    // Enums
    public enum ShippingStatus {
        PENDING("Chờ xử lý"),
        PICKED_UP("Đã lấy hàng"),
        IN_TRANSIT("Đang vận chuyển"),
        DELIVERED("Đã giao"),
        CANCELLED("Đã hủy");
        
        private final String displayValue;
        
        ShippingStatus(String displayValue) {
            this.displayValue = displayValue;
        }
        
        public String getDisplayValue() {
            return displayValue;
        }
    }
    
    // Constructors
    public Shipping() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public Date getScheduledDeliveryDate() {
        return scheduledDeliveryDate;
    }

    public void setScheduledDeliveryDate(Date scheduledDeliveryDate) {
        this.scheduledDeliveryDate = scheduledDeliveryDate;
    }

    public Date getActualDeliveryDate() {
        return actualDeliveryDate;
    }

    public void setActualDeliveryDate(Date actualDeliveryDate) {
        this.actualDeliveryDate = actualDeliveryDate;
    }

    public ShippingStatus getStatus() {
        return status;
    }

    public void setStatus(ShippingStatus status) {
        this.status = status;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
    
    // Business Methods
    
    /**
     * Tự động tạo tracking number nếu chưa có
     */
    public void generateTrackingNumber() {
        if (this.trackingNumber == null || this.trackingNumber.isEmpty()) {
            String prefix = "ST";
            if ("EXPRESS".equals(this.serviceType)) {
                prefix = "EX";
            }
            this.trackingNumber = prefix + "-" + System.currentTimeMillis();
        }
    }
    
    /**
     * Kiểm tra có quá hạn giao hàng không
     */
    public boolean isOverdue() {
        if (this.scheduledDeliveryDate == null) return false;
        if (this.status == ShippingStatus.DELIVERED || this.status == ShippingStatus.CANCELLED) return false;
        return new Date().after(this.scheduledDeliveryDate);
    }
    
    /**
     * Kiểm tra có đúng hạn không
     */
    public boolean isOnTime() {
        if (this.actualDeliveryDate == null || this.scheduledDeliveryDate == null) return false;
        return this.actualDeliveryDate.before(this.scheduledDeliveryDate) || 
               this.actualDeliveryDate.equals(this.scheduledDeliveryDate);
    }
    
    @PrePersist
    protected void onCreate() {
        this.createdDate = new Date();
        this.updatedDate = new Date();
        generateTrackingNumber();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = new Date();
    }
}