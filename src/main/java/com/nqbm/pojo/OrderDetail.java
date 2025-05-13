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

@Entity
@Table(name = "order_details")
public class OrderDetail implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    private Integer quantity = 1;
    
    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal subtotal;
    
    @Column(length = 255)
    private String note;
    
    // Constructors
    public OrderDetail() {
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        
        // Tự động cài đặt giá đơn vị từ sản phẩm nếu chưa có
        if (this.unitPrice == null && product != null) {
            this.unitPrice = product.getUnitPrice();
        }
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateSubtotal();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateSubtotal();
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
        calculateSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
    
    // Phương thức tự động tính thành tiền
    public void calculateSubtotal() {
        if (this.unitPrice != null && this.quantity != null) {
            BigDecimal total = this.unitPrice.multiply(new BigDecimal(this.quantity));
            
            if (this.discount != null && this.discount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountAmount = total.multiply(this.discount.divide(new BigDecimal(100)));
                total = total.subtract(discountAmount);
            }
            
            this.subtotal = total;
        }
    }
    
    @PrePersist
    @PreUpdate
    protected void onSave() {
        calculateSubtotal();
    }
}
