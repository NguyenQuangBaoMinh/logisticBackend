/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.repositories;

/**
 *
 * @author baominh14022004gmail.com
 */


import com.nqbm.pojo.Order;
import java.util.List;
import java.util.Map;

public interface OrderRepository {
    List<Order> getOrders(Map<String, String> params);
    Order getOrderById(Long id);
    Order getOrderByOrderNumber(String orderNumber);
    boolean addOrder(Order order);
    boolean updateOrder(Order order);
    boolean deleteOrder(Long id);
    Long countOrders();
    Long countOrdersByStatus(Order.OrderStatus status);
}
