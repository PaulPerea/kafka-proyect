package com.example.resource;

import com.example.model.Order;
import com.example.model.OrderStatus;
import com.example.service.OrderProducer;
import com.example.service.OrderService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    OrderProducer orderProducer;

    @Inject
    OrderService orderService;

    @POST
    public Response createOrder(CreateOrderRequest request) {
        try {
            Order order = new Order(
                    UUID.randomUUID().toString(),
                    request.customerId,
                    request.product,
                    request.quantity,
                    request.price
            );

            // Enviar orden a Kafka
            orderProducer.sendOrder(order);

            return Response.accepted(order).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error creando orden: " + e.getMessage())
                    .build();
        }
    }

    @GET
    public Collection<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GET
    @Path("/{orderId}")
    public Response getOrder(@PathParam("orderId") String orderId) {
        Order order = orderService.getOrder(orderId);
        if (order != null) {
            return Response.ok(order).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("/{orderId}/status")
    public Response updateOrderStatus(@PathParam("orderId") String orderId,
                                      @QueryParam("status") OrderStatus status) {
        Order order = orderService.getOrder(orderId);
        if (order != null) {
            orderService.updateOrderStatus(orderId, status);

            // Enviar notificación del cambio de estado
            String notification = String.format("Orden %s cambió a estado: %s", orderId, status);
            orderProducer.sendNotification(notification);

            return Response.ok(order).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/stats")
    public Response getStats() {
        return Response.ok(new OrderStats(
                orderService.getOrderCount(),
                orderService.getOrdersByStatus(OrderStatus.PENDING).size(),
                orderService.getOrdersByStatus(OrderStatus.CONFIRMED).size(),
                orderService.getOrdersByStatus(OrderStatus.SHIPPED).size()
        )).build();
    }

    // DTOs
    public static class CreateOrderRequest {
        public String customerId;
        public String product;
        public int quantity;
        public BigDecimal price;
    }

    public static class OrderStats {
        public long total;
        public long pending;
        public long confirmed;
        public long shipped;

        public OrderStats(long total, long pending, long confirmed, long shipped) {
            this.total = total;
            this.pending = pending;
            this.confirmed = confirmed;
            this.shipped = shipped;
        }
    }
}