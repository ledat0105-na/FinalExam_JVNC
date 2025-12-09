-- Script để tạo đơn hàng mẫu và yêu cầu hoàn tiền để test
-- Chạy script này trong MySQL để tạo dữ liệu test

USE final_exam_db;

-- Tạo đơn hàng mới cho customer1
INSERT INTO Orders (customerId, cartId, orderNumber, status, shippingAddress, billingAddress, 
                    subtotal, discountTotal, taxAmount, shippingFee, codFee, gatewayFee, 
                    grandTotal, amountDue, createdAt, createdBy)
VALUES
((SELECT accountId FROM Accounts WHERE username='customer1'), 
 NULL, 
 'ORD-2025004', 
 'PAID', 
 '123 Test Street, Ho Chi Minh City', 
 '123 Test Street, Ho Chi Minh City',
 250000.00,  -- subtotal
 0.00,       -- discountTotal
 0.00,       -- taxAmount
 30000.00,   -- shippingFee
 0.00,       -- codFee
 0.00,       -- gatewayFee
 280000.00,  -- grandTotal
 0.00,       -- amountDue (đã thanh toán)
 NOW(), 
 'test');

-- Thêm OrderItems cho đơn hàng mới
INSERT INTO OrderItems (orderId, itemId, quantity, unitPrice, discountAmount, lineTotal, createdBy)
VALUES
((SELECT orderId FROM Orders WHERE orderNumber='ORD-2025004'), 
 (SELECT itemId FROM Items WHERE sku='SKU-001'), 
 1, 
 129000.00, 
 0.00, 
 129000.00, 
 'test'),
((SELECT orderId FROM Orders WHERE orderNumber='ORD-2025004'), 
 (SELECT itemId FROM Items WHERE sku='SKU-006'), 
 1, 
 159000.00, 
 0.00, 
 159000.00, 
 'test');

-- Tạo Payment cho đơn hàng
INSERT INTO Payments (orderId, paymentMethod, amount, status, paidAt, transactionRef, createdBy)
VALUES
((SELECT orderId FROM Orders WHERE orderNumber='ORD-2025004'), 
 'GATEWAY', 
 280000.00, 
 'COMPLETED', 
 NOW(), 
 'TXN-2025004', 
 'test');

-- Tạo yêu cầu hoàn tiền (REQUESTED status) để test
INSERT INTO Refunds (orderId, paymentId, refundAmount, reason, status, requestedAt, createdBy)
VALUES
((SELECT orderId FROM Orders WHERE orderNumber='ORD-2025004'), 
 (SELECT paymentId FROM Payments WHERE orderId=(SELECT orderId FROM Orders WHERE orderNumber='ORD-2025004')), 
 100000.00,  -- Số tiền hoàn
 'Sản phẩm không đúng mô tả, muốn hoàn tiền một phần',  -- Lý do
 'REQUESTED',  -- Trạng thái: REQUESTED để có thể test duyệt/từ chối
 NOW(), 
 'test');

-- Kiểm tra kết quả
SELECT 
    o.orderNumber,
    o.status as orderStatus,
    o.grandTotal,
    r.refundId,
    r.refundAmount,
    r.reason,
    r.status as refundStatus,
    r.requestedAt
FROM Orders o
LEFT JOIN Refunds r ON o.orderId = r.orderId
WHERE o.orderNumber = 'ORD-2025004';

