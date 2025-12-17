package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.model.*;
import com.example.finalexam_jvnc.repository.*;
import com.example.finalexam_jvnc.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

        @Autowired
        private CartRepository cartRepository;

        @Autowired
        private CartItemRepository cartItemRepository;

        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private OrderItemRepository orderItemRepository;

        @Autowired
        private ShipmentRepository shipmentRepository;

        @Autowired
        private AccountProfileRepository accountProfileRepository;

        @Autowired
        private WalletRepository walletRepository;

        @Autowired
        private RefundRepository refundRepository;

        @Autowired
        private com.example.finalexam_jvnc.service.StockItemService stockItemService;

        // ...

        @Override
        public com.example.finalexam_jvnc.dto.OrderDTO getOrderById(Long orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found"));

                com.example.finalexam_jvnc.dto.OrderDTO orderDTO = convertToDTO(order);

                // Populate Items
                List<OrderItem> orderItems = orderItemRepository.findByOrderOrderId(orderId);
                List<com.example.finalexam_jvnc.dto.OrderItemDTO> itemDTOs = orderItems.stream()
                                .map(item -> com.example.finalexam_jvnc.dto.OrderItemDTO.builder()
                                                .orderItemId(item.getOrderItemId())
                                                .itemId(item.getItem().getItemId())
                                                .itemName(item.getItem().getItemName())
                                                .sku(item.getItem().getSku())
                                                .quantity(item.getQuantity())
                                                .unitPrice(item.getUnitPrice())
                                                .discountAmount(item.getDiscountAmount())
                                                .lineTotal(item.getLineTotal())
                                                .build())
                                .collect(java.util.stream.Collectors.toList());
                orderDTO.setItems(itemDTOs);

                // Populate Shipments
                List<Shipment> shipments = shipmentRepository.findByOrderOrderId(orderId);
                List<com.example.finalexam_jvnc.dto.ShipmentDTO> shipmentDTOs = shipments.stream()
                                .map(shipment -> com.example.finalexam_jvnc.dto.ShipmentDTO.builder()
                                                .shipmentId(shipment.getShipmentId())
                                                .orderId(shipment.getOrder().getOrderId())
                                                .orderNumber(shipment.getOrder().getOrderNumber())
                                                .trackingNumber(shipment.getTrackingNumber())
                                                .carrier(shipment.getCarrier())
                                                .status(shipment.getStatus())
                                                .shippedAt(shipment.getShippedAt())
                                                .deliveredAt(shipment.getDeliveredAt())
                                                .build())
                                .collect(java.util.stream.Collectors.toList());
                orderDTO.setShipments(shipmentDTOs);

                return orderDTO;
        }

        @Override
        public Order createOrderFromCart(String username, String paymentMethod) {
                // 1. Validate Cart
                Cart cart = cartRepository.findByCustomer_UsernameAndStatus(username, "CART")
                                .orElseThrow(() -> new RuntimeException("No active cart found"));

                List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());
                if (cartItems.isEmpty()) {
                        throw new RuntimeException("Cart is empty");
                }

                Account customer = cart.getCustomer();

                // Retrieve shipping address from profile
                AccountProfile profile = accountProfileRepository.findByAccount_Username(username).orElse(null);
                String shippingAddress = (profile != null && profile.getAddressLine() != null)
                                ? profile.getAddressLine()
                                : "Unknown Address";

                // 2. Calculate Totals
                double subtotal = cartItems.stream()
                                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                                .sum();

                // Calculate discount
                double discountTotal = 0.0;
                if (cart.getPromotion() != null) {
                        com.example.finalexam_jvnc.model.Promotion promo = cart.getPromotion();
                        if ("PERCENT".equals(promo.getDiscountType())) {
                                discountTotal = subtotal * (promo.getDiscountValue() / 100);
                        } else { // AMOUNT
                                discountTotal = promo.getDiscountValue();
                        }
                        if (discountTotal > subtotal)
                                discountTotal = subtotal;
                }

                // Check and Reserve Stock
                for (CartItem item : cartItems) {
                        stockItemService.reserveStock(item.getItem().getItemId(), item.getQuantity());
                }

                // Simple fee logic (can be expanded)
                double shippingFee = 0.0;
                double taxAmount = 0.0;
                double grandTotal = subtotal + shippingFee + taxAmount - discountTotal;

                // 3. Process Payment if WALLET
                if ("WALLET".equals(paymentMethod)) {
                        Wallet wallet = walletRepository.findByAccount_Username(username)
                                        .orElseThrow(() -> new RuntimeException("Wallet not found"));
                        if (wallet.getBalance() < grandTotal) {
                                throw new RuntimeException("Insufficient wallet balance");
                        }
                        wallet.setBalance(wallet.getBalance() - grandTotal);
                        walletRepository.save(wallet);
                }

                // 4. Create Order
                Order order = Order.builder()
                                .customer(customer)
                                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                                .status("PENDING_CONFIRMATION")
                                .shippingAddress(shippingAddress)
                                .billingAddress(shippingAddress)
                                .paymentMethod(paymentMethod) // Set payment method
                                .subtotal(subtotal)
                                .discountTotal(discountTotal)
                                .taxAmount(taxAmount)
                                .shippingFee(shippingFee)
                                .grandTotal(grandTotal)
                                .amountDue("WALLET".equals(paymentMethod) ? 0.0 : grandTotal) // If wallet, amount due
                                                                                              // is 0
                                .createdAt(LocalDateTime.now())
                                .build();

                Order savedOrder = orderRepository.save(order);

                // 4. Create OrderItems
                for (CartItem cartItem : cartItems) {
                        OrderItem orderItem = OrderItem.builder()
                                        .order(savedOrder)
                                        .item(cartItem.getItem())
                                        .quantity(cartItem.getQuantity())
                                        .unitPrice(cartItem.getUnitPrice())
                                        .discountAmount(cartItem.getDiscountAmount())
                                        .lineTotal(cartItem.getUnitPrice() * cartItem.getQuantity()
                                                        - (cartItem.getDiscountAmount() != null
                                                                        ? cartItem.getDiscountAmount()
                                                                        : 0))
                                        .build();
                        orderItemRepository.save(orderItem);
                }

                // 5. Update Cart Status
                cart.setStatus("CONVERTED");
                return savedOrder;
        }

        @Override
        public java.util.List<com.example.finalexam_jvnc.dto.OrderDTO> getOrdersByStatus(String status) {
                return orderRepository.findByStatus(status).stream()
                                .map(this::convertToDTO)
                                .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public java.util.List<com.example.finalexam_jvnc.dto.OrderDTO> getAllOrders() {
                return orderRepository.findAllWithCustomer().stream()
                                .map(this::convertToDTO)
                                .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public void updateOrderStatus(Long orderId, String status) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found"));

                String oldStatus = order.getStatus();

                // Inventory Logic: Handle transitions
                // 1. If moving to SHIPPED/DELIVERED from a non-complete state, deduct stock
                // permanently (move from Reserved to Gone)
                if (("SHIPPED".equals(status) || "DELIVERED".equals(status) || "On Delivery".equals(status))
                                && !oldStatus.equals("SHIPPED") && !oldStatus.equals("DELIVERED")
                                && !oldStatus.equals("On Delivery")) {
                        List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);
                        for (OrderItem item : items) {
                                try {
                                        stockItemService.deductStock(item.getItem().getItemId(), item.getQuantity());
                                } catch (Exception e) {
                                        // Log error, but what to do? Maybe stock was already messed up.
                                        // Ensure we don't block status update? Or do we?
                                        // For now, throw to prevent "fake" shipping if stock is missing
                                        throw new RuntimeException("Inventory Error: " + e.getMessage());
                                }
                        }
                }

                order.setStatus(status);
                orderRepository.save(order);
        }

        @Override
        public java.util.List<com.example.finalexam_jvnc.dto.OrderDTO> getOrdersByCustomer(String username) {
                return orderRepository.findByCustomer_UsernameOrderByCreatedAtDesc(username).stream()
                                .map(this::convertToDTO)
                                .collect(java.util.stream.Collectors.toList());
        }

        // Helper method to convert Entity to DTO
        private com.example.finalexam_jvnc.dto.OrderDTO convertToDTO(Order order) {
                Long refundId = null;
                String refundStatus = null;
                List<Refund> refunds = refundRepository.findByOrderOrderId(order.getOrderId());
                if (!refunds.isEmpty()) {
                        refundId = refunds.get(0).getRefundId();
                        refundStatus = refunds.get(0).getStatus();
                }

                return com.example.finalexam_jvnc.dto.OrderDTO.builder()
                                .orderId(order.getOrderId())
                                .customerId(order.getCustomer().getAccountId())
                                .customerUsername(order.getCustomer().getUsername())
                                .orderNumber(order.getOrderNumber())
                                .status(order.getStatus())
                                .shippingAddress(order.getShippingAddress())
                                .billingAddress(order.getBillingAddress())
                                .paymentMethod(order.getPaymentMethod())
                                .subtotal(order.getSubtotal())
                                .discountTotal(order.getDiscountTotal())
                                .taxAmount(order.getTaxAmount())
                                .shippingFee(order.getShippingFee())
                                .codFee(order.getCodFee())
                                .gatewayFee(order.getGatewayFee())
                                .grandTotal(order.getGrandTotal())
                                .amountDue(order.getAmountDue())
                                .createdAt(order.getCreatedAt())
                                .refundId(refundId)
                                .refundStatus(refundStatus)
                                .build();
        }

        @Override
        public void cancelOrder(Long orderId, String username) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found"));

                if (!order.getCustomer().getUsername().equals(username)) {
                        throw new RuntimeException("You are not authorized to cancel this order");
                }

                if (!"PENDING_CONFIRMATION".equals(order.getStatus())) {
                        throw new RuntimeException(
                                        "Order cannot be canceled because it is not in PENDING_CONFIRMATION status");
                }

                // 1. Update status to CANCELLED
                order.setStatus("CANCELLED");
                orderRepository.save(order);

                // 2. Handle Auto-Refund Logic (Only for WALLET)
                if ("WALLET".equals(order.getPaymentMethod())) {
                        // Do NOT refund immediately. Create a REQUESTED refund for Staff/Admin
                        // approval.

                        // Create Refund Record (REQUESTED)
                        Refund refund = Refund.builder()
                                        .order(order)
                                        .refundAmount(order.getGrandTotal())
                                        .status("REQUESTED") // Wait for approval
                                        .requestedAt(java.time.LocalDateTime.now())
                                        .reason("Auto-request for Cancelled Order (Wallet)")
                                        .build();
                        refundRepository.save(refund);
                }
                // For BANK_TRANSFER and COD: Just cancel, no auto-refund request.

                // 3. Release Stock
                List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);
                for (OrderItem item : items) {
                        stockItemService.releaseStock(item.getItem().getItemId(), item.getQuantity());
                }
        }

        @Override
        public void requestRefund(Long orderId, String username) {
                // 1. Verify order exists and belongs to user
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found"));
                if (!order.getCustomer().getUsername().equals(username)) {
                        throw new RuntimeException("You are not authorized to request a refund for this order");
                }

                // 2. Allow refund request ONLY for completed orders (Returns)
                // User requirement: "Only when received and error can they send refund request"
                if (!"DONE".equals(order.getStatus())) {
                        throw new RuntimeException(
                                        "Refund/Return request is only available for completed (received) orders.");
                }

                // Check if a refund already exists for this order
                if (!refundRepository.findByOrderOrderId(orderId).isEmpty()) {
                        throw new RuntimeException("Refund request already submitted for this order");
                }

                // 3. Create refund entry with REQUESTED status (For Staff Review)
                Refund refund = Refund.builder()
                                .order(order)
                                .refundAmount(order.getGrandTotal())
                                .status("REQUESTED")
                                .requestedAt(java.time.LocalDateTime.now())
                                .reason("Item Defective/Error (Customer Request After Receipt)")
                                .build();
                refundRepository.save(refund);
        }
}
