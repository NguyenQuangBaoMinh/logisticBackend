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
@Table(name = "inventory")
public class Inventory implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "quantity_on_hand", nullable = false)
    private Integer quantityOnHand = 0;
    
    @Column(name = "quantity_reserved", nullable = false)
    private Integer quantityReserved = 0;
    
    @Column(name = "quantity_available", nullable = false)
    private Integer quantityAvailable = 0;
    
    @Column(name = "reorder_point")
    private Integer reorderPoint = 10;
    
    @Column(name = "maximum_stock")
    private Integer maximumStock = 1000;
    
    @Column(name = "minimum_stock")
    private Integer minimumStock = 5;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "last_stock_date")
    private Date lastStockDate;
    
    @Column(name = "average_cost", precision = 15, scale = 2)
    private BigDecimal averageCost = BigDecimal.ZERO;
    
    @Column(name = "total_value", precision = 15, scale = 2)
    private BigDecimal totalValue = BigDecimal.ZERO;
    
    @Column(name = "location")
    private String location;
    
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
    
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;
    
    // Constructors
    public Inventory() {
    }
    
    public Inventory(Product product, Integer quantityOnHand) {
        this.product = product;
        this.quantityOnHand = quantityOnHand;
        this.quantityAvailable = quantityOnHand;
        updateTotalValue();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        updateTotalValue();
    }

    public Integer getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(Integer quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
        updateQuantityAvailable();
        updateTotalValue();
    }

    public Integer getQuantityReserved() {
        return quantityReserved;
    }

    public void setQuantityReserved(Integer quantityReserved) {
        this.quantityReserved = quantityReserved;
        updateQuantityAvailable();
    }

    public Integer getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(Integer quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public Integer getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(Integer reorderPoint) {
        this.reorderPoint = reorderPoint;
    }

    public Integer getMaximumStock() {
        return maximumStock;
    }

    public void setMaximumStock(Integer maximumStock) {
        this.maximumStock = maximumStock;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }

    public Date getLastStockDate() {
        return lastStockDate;
    }

    public void setLastStockDate(Date lastStockDate) {
        this.lastStockDate = lastStockDate;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
        updateTotalValue();
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    // Business Logic Methods
    
    /**
     * Cập nhật số lượng có sẵn dựa trên số lượng tồn kho và số lượng đã đặt
     */
    private void updateQuantityAvailable() {
        this.quantityAvailable = this.quantityOnHand - this.quantityReserved;
        if (this.quantityAvailable < 0) {
            this.quantityAvailable = 0;
        }
    }
    
    /**
     * Cập nhật tổng giá trị tồn kho
     */
    private void updateTotalValue() {
        if (this.averageCost != null && this.quantityOnHand != null) {
            this.totalValue = this.averageCost.multiply(new BigDecimal(this.quantityOnHand));
        }
    }
    
    /**
     * Tăng số lượng tồn kho (nhập kho)
     */
    public void increaseStock(Integer quantity, BigDecimal cost) {
        if (quantity > 0) {
            // Cập nhật giá trị trung bình
            if (this.quantityOnHand > 0 && this.averageCost != null) {
                BigDecimal totalValue = this.averageCost.multiply(new BigDecimal(this.quantityOnHand));
                BigDecimal newTotalValue = cost.multiply(new BigDecimal(quantity));
                BigDecimal combinedValue = totalValue.add(newTotalValue);
                int combinedQuantity = this.quantityOnHand + quantity;
                this.averageCost = combinedValue.divide(new BigDecimal(combinedQuantity), 2, BigDecimal.ROUND_HALF_UP);
            } else {
                this.averageCost = cost;
            }
            
            this.quantityOnHand += quantity;
            this.lastStockDate = new Date();
            updateQuantityAvailable();
            updateTotalValue();
        }
    }
    
    /**
     * Giảm số lượng tồn kho (xuất kho)
     */
    public boolean decreaseStock(Integer quantity) {
        if (quantity > 0 && this.quantityAvailable >= quantity) {
            this.quantityOnHand -= quantity;
            this.lastStockDate = new Date();
            updateQuantityAvailable();
            updateTotalValue();
            return true;
        }
        return false;
    }
    
    /**
     * Đặt trước hàng hóa
     */
    public boolean reserveStock(Integer quantity) {
        if (quantity > 0 && this.quantityAvailable >= quantity) {
            this.quantityReserved += quantity;
            updateQuantityAvailable();
            return true;
        }
        return false;
    }
    
    /**
     * Hủy đặt trước hàng hóa
     */
    public void unreserveStock(Integer quantity) {
        if (quantity > 0 && this.quantityReserved >= quantity) {
            this.quantityReserved -= quantity;
            updateQuantityAvailable();
        }
    }
    
    /**
     * Kiểm tra có cần đặt hàng lại không
     */
    public boolean needsReorder() {
        return this.quantityAvailable <= this.reorderPoint;
    }
    
    /**
     * Kiểm tra có vượt quá tồn kho tối đa không
     */
    public boolean isOverstock() {
        return this.quantityOnHand > this.maximumStock;
    }
    
    /**
     * Kiểm tra có dưới tồn kho tối thiểu không
     */
    public boolean isUnderstock() {
        return this.quantityOnHand < this.minimumStock;
    }
    
    @PrePersist
    protected void onCreate() {
        this.createdDate = new Date();
        this.updatedDate = new Date();
        updateQuantityAvailable();
        updateTotalValue();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = new Date();
        updateQuantityAvailable();
        updateTotalValue();
    }
}
