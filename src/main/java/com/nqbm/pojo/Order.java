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
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
public class Order implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_number", length = 20, unique = true)
    private String orderNumber;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "order_date")
    private Date orderDate;
    
    @Column(name = "customer_name")
    private String customerName;
    
    @Column(name = "customer_phone")
    private String customerPhone;
    
    @Column(name = "customer_email")
    private String customerEmail;
    
    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;
    
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
    
    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "approved_date")
    private Date approvedDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "shipped_date")
    private Date shippedDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "completed_date")
    private Date completedDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "cancelled_date")
    private Date cancelledDate;
    
    @Column(name = "order_type")
    @Enumerated(EnumType.STRING)
    private OrderType orderType = OrderType.OUTBOUND;
    
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    
    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderDetail> orderDetails = new HashSet<>();
    
    // Constructors
    public Order() {
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Date getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(Date approvedDate) {
        this.approvedDate = approvedDate;
    }

    public Date getShippedDate() {
        return shippedDate;
    }

    public void setShippedDate(Date shippedDate) {
        this.shippedDate = shippedDate;
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }

    public Date getCancelledDate() {
        return cancelledDate;
    }

    public void setCancelledDate(Date cancelledDate) {
        this.cancelledDate = cancelledDate;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public Set<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(Set<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }
    
    // Helper method để thêm chi tiết đơn hàng
    public void addOrderDetail(OrderDetail orderDetail) {
        orderDetails.add(orderDetail);
        orderDetail.setOrder(this);
    }
    
    // Helper method để xóa chi tiết đơn hàng
    public void removeOrderDetail(OrderDetail orderDetail) {
        orderDetails.remove(orderDetail);
        orderDetail.setOrder(null);
    }
    
    // Enums
    public enum OrderStatus {
        PENDING("Đang xử lý"),
        APPROVED("Đã duyệt"),
        SHIPPING("Đang giao hàng"),
        COMPLETED("Hoàn thành"),
        CANCELLED("Đã hủy");
        
        private final String displayValue;
        
        OrderStatus(String displayValue) {
            this.displayValue = displayValue;
        }
        
        public String getDisplayValue() {
            return displayValue;
        }
    }
    
    public enum OrderType {
        INBOUND("Nhập kho"),
        OUTBOUND("Xuất kho");
        
        private final String displayValue;
        
        OrderType(String displayValue) {
            this.displayValue = displayValue;
        }
        
        public String getDisplayValue() {
            return displayValue;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        this.orderDate = new Date();
        
        // Tạo mã đơn hàng tự động nếu chưa có
        if (this.orderNumber == null || this.orderNumber.isEmpty()) {
            String prefix = this.orderType == OrderType.INBOUND ? "IN" : "OUT";
            this.orderNumber = prefix + "-" + String.format("%04d", this.id != null ? this.id : 0) +
                    "-" + String.format("%tY%<tm%<td", new Date());
        }
    }
    
    // Method để tính tổng tiền
    public void calculateTotal() {
        this.totalAmount = BigDecimal.ZERO;
        for (OrderDetail detail : this.orderDetails) {
            if (detail.getSubtotal() != null) {
                this.totalAmount = this.totalAmount.add(detail.getSubtotal());
            }
        }
    }
}
