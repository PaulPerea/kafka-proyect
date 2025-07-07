package com.example.service;

import com.example.model.Order;
import com.example.model.OrderStatus;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;

@ApplicationScoped
public class OrderService {

    private static final Logger LOG = Logger.getLogger(OrderService.class);

    // Simulamos una base de datos en memoria
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public void saveOrder(Order order) {
        orders.put(order.getId(), order);
        LOG.infof("Orden guardada: %s", order.getId());
    }

    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }

    public Collection<Order> getAllOrders() {
        return orders.values();
    }

    public void updateOrderStatus(String orderId, OrderStatus status) {
        Order order = orders.get(orderId);
        if (order != null) {
            order.setStatus(status);
            LOG.infof("Estado de orden %s actualizado a: %s", orderId, status);
        }
    }

    public long getOrderCount() {
        return orders.size();
    }

    public Collection<Order> getOrdersByStatus(OrderStatus status) {
        return orders.values().stream()
                .filter(order -> order.getStatus() == status)
                .toList();
    }
}