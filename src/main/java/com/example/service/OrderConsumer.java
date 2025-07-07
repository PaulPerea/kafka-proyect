package com.example.service;

import com.example.model.Order;
import com.example.model.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class OrderConsumer {

    private static final Logger LOG = Logger.getLogger(OrderConsumer.class);

    @Inject
    OrderService orderService;

    @Inject
    ObjectMapper objectMapper;

    @Incoming("orders-in")
    public CompletionStage<Void> processOrder(String orderJson) {
        LOG.infof("Procesando orden recibida: %s", orderJson);

        return CompletableFuture.runAsync(() -> {
            try {
                // ✅ Deserializar JSON String a Order
                Order order = objectMapper.readValue(orderJson, Order.class);
                LOG.infof("Procesando orden recibida: %s", order);

                // Simular procesamiento de la orden
                Thread.sleep(1000);

                // Procesar según la lógica de negocio
                if (order.getQuantity() > 0 && order.getPrice() != null) {
                    order.setStatus(OrderStatus.CONFIRMED);
                    orderService.saveOrder(order);
                    LOG.infof("Orden confirmada: %s", order.getId());
                } else {
                    order.setStatus(OrderStatus.CANCELLED);
                    LOG.warnf("Orden cancelada por datos inválidos: %s", order.getId());
                }

            } catch (Exception e) {
                LOG.errorf("Error procesando orden del JSON %s: %s", orderJson, e.getMessage());
                e.printStackTrace(); // ✅ Para debug
            }
        });
    }

    @Incoming("notifications-in")
    public void handleNotification(String notification) {
        LOG.infof("Notificación recibida: %s", notification);

        // Aquí podrías enviar emails, SMS, push notifications, etc.
        if (notification.contains("Nueva orden")) {
            LOG.info("Procesando notificación de nueva orden...");
            // Lógica para notificar al equipo de ventas
        } else if (notification.contains("confirmada")) {
            LOG.info("Procesando notificación de orden confirmada...");
            // Lógica para notificar al cliente
        }
    }
}