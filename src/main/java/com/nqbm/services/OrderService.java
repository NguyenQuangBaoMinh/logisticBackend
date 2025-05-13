/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Order;
import java.util.List;
import java.util.Map;

public interface OrderService {
    List<Order> getOrders(Map<String, String> params);
    Order getOrderById(Long id);
    Order getOrderByOrderNumber(String orderNumber);
    boolean addOrder(Order order);
    boolean updateOrder(Order order);
    boolean deleteOrder(Long id);
    Long countOrders();
    Long countOrdersByStatus(Order.OrderStatus status);
    
    // Business logic methods
    boolean approveOrder(Long orderId, Long approvedBy);
    boolean shipOrder(Long orderId);
    boolean completeOrder(Long orderId);
    boolean cancelOrder(Long orderId);
    String generateOrderNumber(Order.OrderType orderType);
}
