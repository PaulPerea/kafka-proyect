package com.example.service;

import com.example.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class OrderProducer {

    private static final Logger LOG = Logger.getLogger(OrderProducer.class);

    @Inject
    @Channel("orders-out")
    Emitter<String> orderEmitter;

    @Inject
    @Channel("notifications-out")
    Emitter<String> notificationEmitter;

    @Inject
    ObjectMapper objectMapper;

    public CompletionStage<Void> sendOrder(Order order) {
        LOG.infof("Enviando orden: %s", order);

        try {
            // ✅ Serializar Order a JSON String
            String orderJson = objectMapper.writeValueAsString(order);

            return orderEmitter.send(orderJson)
                    .thenRun(() -> {
                        LOG.infof("Orden enviada exitosamente: %s", order.getId());
                        // Enviar notificación
                        String notification = String.format("Nueva orden creada: %s por cliente %s",
                                order.getId(), order.getCustomerId());
                        notificationEmitter.send(notification);
                    })
                    .exceptionally(throwable -> {
                        LOG.errorf("Error enviando orden %s: %s", order.getId(), throwable.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            LOG.errorf("Error serializando orden %s: %s", order.getId(), e.getMessage());
            return CompletableFuture.failedStage(e);
        }
    }

    public void sendNotification(String message) {
        LOG.infof("Enviando notificación: %s", message);
        notificationEmitter.send(message);
    }
}