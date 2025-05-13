/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services.impl;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Order;
import com.nqbm.pojo.OrderDetail;
import com.nqbm.pojo.Product;
import com.nqbm.pojo.User;
import com.nqbm.repositories.OrderRepository;
import com.nqbm.repositories.ProductRepository;
import com.nqbm.repositories.UserRepository;
import com.nqbm.services.OrderService;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public List<Order> getOrders(Map<String, String> params) {
        return this.orderRepository.getOrders(params);
    }
    
    @Override
    public Order getOrderById(Long id) {
        return this.orderRepository.getOrderById(id);
    }
    
    @Override
    public Order getOrderByOrderNumber(String orderNumber) {
        return this.orderRepository.getOrderByOrderNumber(orderNumber);
    }
    
    @Override
    public boolean addOrder(Order order) {
        // Tạo số đơn hàng nếu chưa có
        if (order.getOrderNumber() == null || order.getOrderNumber().isEmpty()) {
            order.setOrderNumber(generateOrderNumber(order.getOrderType()));
        }
        
        // Tính toán tổng tiền
        order.calculateTotal();
        
        return this.orderRepository.addOrder(order);
    }
    
    @Override
    public boolean updateOrder(Order order) {
        order.calculateTotal();
        return this.orderRepository.updateOrder(order);
    }
    
    @Override
    public boolean deleteOrder(Long id) {
        return this.orderRepository.deleteOrder(id);
    }
    
    @Override
    public Long countOrders() {
        return this.orderRepository.countOrders();
    }
    
    @Override
    public Long countOrdersByStatus(Order.OrderStatus status) {
        return this.orderRepository.countOrdersByStatus(status);
    }
    
    @Override
    public boolean approveOrder(Long orderId, Long approvedById) {
        Order order = this.orderRepository.getOrderById(orderId);
        if (order == null || order.getStatus() != Order.OrderStatus.PENDING) {
            return false;
        }
        
        User approvedBy = this.userRepository.getUserById(approvedById);
        if (approvedBy == null) {
            return false;
        }
        
        // Cập nhật trạng thái đơn hàng
        order.setStatus(Order.OrderStatus.APPROVED);
        order.setApprovedBy(approvedBy);
        order.setApprovedDate(new Date());
        
        // Cập nhật tồn kho
        updateInventory(order);
        
        return this.orderRepository.updateOrder(order);
    }
    
    @Override
    public boolean shipOrder(Long orderId) {
        Order order = this.orderRepository.getOrderById(orderId);
        if (order == null || order.getStatus() != Order.OrderStatus.APPROVED) {
            return false;
        }
        
        order.setStatus(Order.OrderStatus.SHIPPING);
        order.setShippedDate(new Date());
        
        return this.orderRepository.updateOrder(order);
    }
    
    @Override
    public boolean completeOrder(Long orderId) {
        Order order = this.orderRepository.getOrderById(orderId);
        if (order == null || order.getStatus() != Order.OrderStatus.SHIPPING) {
            return false;
        }
        
        order.setStatus(Order.OrderStatus.COMPLETED);
        order.setCompletedDate(new Date());
        
        return this.orderRepository.updateOrder(order);
    }
    
    @Override
    public boolean cancelOrder(Long orderId) {
        Order order = this.orderRepository.getOrderById(orderId);
        if (order == null || order.getStatus() == Order.OrderStatus.COMPLETED) {
            return false;
        }
        
        // Nếu đơn hàng đã được duyệt, cần hoàn trả lại tồn kho
        if (order.getStatus() == Order.OrderStatus.APPROVED || order.getStatus() == Order.OrderStatus.SHIPPING) {
            revertInventory(order);
        }
        
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledDate(new Date());
        
        return this.orderRepository.updateOrder(order);
    }
    
    @Override
    public String generateOrderNumber(Order.OrderType orderType) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String dateStr = dateFormat.format(new Date());
        
        String prefix = orderType == Order.OrderType.INBOUND ? "IN" : "OUT";
        
        // Lấy số đơn hàng hiện tại và tăng lên 1
        Long count = this.orderRepository.countOrders() + 1;
        
        return prefix + "-" + dateStr + "-" + String.format("%04d", count);
    }
    
    // Phương thức cập nhật tồn kho khi duyệt đơn hàng
    private void updateInventory(Order order) {
        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            return;
        }
        
        for (OrderDetail detail : order.getOrderDetails()) {
            Product product = detail.getProduct();
            if (product != null) {
                int currentStock = product.getUnitInStock() != null ? product.getUnitInStock() : 0;
                int quantity = detail.getQuantity() != null ? detail.getQuantity() : 0;
                
                if (order.getOrderType() == Order.OrderType.INBOUND) {
                    // Nhập kho: tăng số lượng
                    product.setUnitInStock(currentStock + quantity);
                } else {
                    // Xuất kho: giảm số lượng
                    product.setUnitInStock(Math.max(0, currentStock - quantity));
                }
                
                this.productRepository.updateProduct(product);
            }
        }
    }
    
    // Phương thức hoàn trả tồn kho khi hủy đơn hàng đã duyệt
    private void revertInventory(Order order) {
        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            return;
        }
        
        for (OrderDetail detail : order.getOrderDetails()) {
            Product product = detail.getProduct();
            if (product != null) {
                int currentStock = product.getUnitInStock() != null ? product.getUnitInStock() : 0;
                int quantity = detail.getQuantity() != null ? detail.getQuantity() : 0;
                
                if (order.getOrderType() == Order.OrderType.INBOUND) {
                    // Nhập kho bị hủy: giảm số lượng
                    product.setUnitInStock(Math.max(0, currentStock - quantity));
                } else {
                    // Xuất kho bị hủy: tăng số lượng
                    product.setUnitInStock(currentStock + quantity);
                }
                
                this.productRepository.updateProduct(product);
            }
        }
    }
}
