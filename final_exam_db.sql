CREATE DATABASE final_exam_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE final_exam_db;
-- Roles
CREATE TABLE Roles (
  roleId INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  roleCode VARCHAR(50) NOT NULL UNIQUE,
  roleName VARCHAR(100) NOT NULL,
  description VARCHAR(255),
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  updatedAt DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  updatedBy VARCHAR(100)
);

-- Accounts
CREATE TABLE Accounts (
  accountId BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  passwordHash VARCHAR(255) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  isActive TINYINT(1) NOT NULL DEFAULT 1,
  isLocked TINYINT(1) NOT NULL DEFAULT 0,
  lastLoginAt DATETIME NULL,
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  updatedAt DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  updatedBy VARCHAR(100)
);

-- AccountProfiles (1-1)
CREATE TABLE AccountProfiles (
  accountId BIGINT NOT NULL PRIMARY KEY,
  fullName VARCHAR(150) NOT NULL,
  phoneNumber VARCHAR(50),
  addressLine VARCHAR(255),
  city VARCHAR(100),
  country VARCHAR(100),
  dateOfBirth DATE,
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  updatedAt DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  updatedBy VARCHAR(100),
  CONSTRAINT FK_AccountProfiles_Accounts FOREIGN KEY (accountId) REFERENCES Accounts(accountId) ON DELETE CASCADE
);

-- AccountRoles (many-to-many)
CREATE TABLE AccountRoles (
  accountId BIGINT NOT NULL,
  roleId INT NOT NULL,
  PRIMARY KEY (accountId, roleId),
  CONSTRAINT FK_AccountRoles_Account FOREIGN KEY (accountId) REFERENCES Accounts(accountId) ON DELETE CASCADE,
  CONSTRAINT FK_AccountRoles_Role FOREIGN KEY (roleId) REFERENCES Roles(roleId) ON DELETE CASCADE
);

-- Categories (multi-level)
CREATE TABLE Categories (
  categoryId BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  categoryCode VARCHAR(50) NOT NULL UNIQUE,
  categoryName VARCHAR(150) NOT NULL,
  parentCategoryId BIGINT NULL,
  description VARCHAR(255),
  isActive TINYINT(1) NOT NULL DEFAULT 1,
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  updatedAt DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  updatedBy VARCHAR(100),
  CONSTRAINT FK_Categories_Parent FOREIGN KEY (parentCategoryId) REFERENCES Categories(categoryId) ON DELETE SET NULL
);

-- Items
CREATE TABLE Items (
  itemId BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  categoryId BIGINT NOT NULL,
  sku VARCHAR(50) NOT NULL UNIQUE,
  itemName VARCHAR(200) NOT NULL,
  itemType VARCHAR(20) NOT NULL,
  unitName VARCHAR(50) NOT NULL,
  unitPrice DECIMAL(18,2) NOT NULL,
  weightKg DECIMAL(10,3) NULL,
  description TEXT,
  isActive TINYINT(1) NOT NULL DEFAULT 1,
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  updatedAt DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  updatedBy VARCHAR(100),
  CONSTRAINT FK_Items_Category FOREIGN KEY (categoryId) REFERENCES Categories(categoryId) ON DELETE RESTRICT
);

-- Warehouses
CREATE TABLE Warehouses (
  warehouseId BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  warehouseCode VARCHAR(50) NOT NULL UNIQUE,
  warehouseName VARCHAR(150) NOT NULL,
  addressLine VARCHAR(255),
  city VARCHAR(100),
  country VARCHAR(100),
  isActive TINYINT(1) NOT NULL DEFAULT 1,
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  updatedAt DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  updatedBy VARCHAR(100)
);

-- StockItems (unique constraint on warehouse+item)
CREATE TABLE StockItems (
  stockItemId BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  warehouseId BIGINT NOT NULL,
  itemId BIGINT NOT NULL,
  quantityOnHand INT NOT NULL DEFAULT 0,
  quantityReserved INT NOT NULL DEFAULT 0,
  lowStockThreshold INT NOT NULL DEFAULT 0,
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  updatedAt DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  updatedBy VARCHAR(100),
  CONSTRAINT UQ_Stock_Warehouse_Item UNIQUE (warehouseId, itemId),
  CONSTRAINT FK_StockItems_Warehouse FOREIGN KEY (warehouseId) REFERENCES Warehouses(warehouseId) ON DELETE CASCADE,
  CONSTRAINT FK_StockItems_Item FOREIGN KEY (itemId) REFERENCES Items(itemId) ON DELETE CASCADE
);

-- Carts
CREATE TABLE Carts (
  cartId BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  customerId BIGINT NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'CART',
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  updatedAt DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  updatedBy VARCHAR(100),
  CONSTRAINT FK_Carts_Customer FOREIGN KEY (customerId) REFERENCES Accounts(accountId) ON DELETE CASCADE
);

-- CartItems
CREATE TABLE CartItems (
  cartItemId BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  cartId BIGINT NOT NULL,
  itemId BIGINT NOT NULL,
  quantity INT NOT NULL,
  unitPrice DECIMAL(18,2) NOT NULL,
  discountAmount DECIMAL(18,2) NOT NULL DEFAULT 0,
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  updatedAt DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  updatedBy VARCHAR(100),
  CONSTRAINT FK_CartItems_Cart FOREIGN KEY (cartId) REFERENCES Carts(cartId) ON DELETE CASCADE,
  CONSTRAINT FK_CartItems_Item FOREIGN KEY (itemId) REFERENCES Items(itemId) ON DELETE RESTRICT
);

-- Orders
CREATE TABLE Orders (
  orderId BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  customerId BIGINT NOT NULL,
  cartId BIGINT NULL,
  orderNumber VARCHAR(50) NOT NULL UNIQUE,
  status VARCHAR(50) NOT NULL,
  shippingAddress VARCHAR(255),
  billingAddress VARCHAR(255),
  subtotal DECIMAL(18,2) NOT NULL DEFAULT 0,
  discountTotal DECIMAL(18,2) NOT NULL DEFAULT 0,
  taxAmount DECIMAL(18,2) NOT NULL DEFAULT 0,
  shippingFee DECIMAL(18,2) NOT NULL DEFAULT 0,
  codFee DECIMAL(18,2) NOT NULL DEFAULT 0,
  gatewayFee DECIMAL(18,2) NOT NULL DEFAULT 0,
  grandTotal DECIMAL(18,2) NOT NULL DEFAULT 0,
  amountDue DECIMAL(18,2) NOT NULL DEFAULT 0,
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  updatedAt DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  updatedBy VARCHAR(100),
  CONSTRAINT FK_Orders_Customer FOREIGN KEY (customerId) REFERENCES Accounts(accountId) ON DELETE RESTRICT,
  CONSTRAINT FK_Orders_Cart FOREIGN KEY (cartId) REFERENCES Carts(cartId) ON DELETE SET NULL
);

-- OrderItems
CREATE TABLE OrderItems (
  orderItemId BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  orderId BIGINT NOT NULL,
  itemId BIGINT NOT NULL,
  quantity INT NOT NULL,
  unitPrice DECIMAL(18,2) NOT NULL,
  discountAmount DECIMAL(18,2) NOT NULL DEFAULT 0,
  lineTotal DECIMAL(18,2) NOT NULL,
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  updatedAt DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  updatedBy VARCHAR(100),
  CONSTRAINT FK_OrderItems_Orders FOREIGN KEY (orderId) REFERENCES Orders(orderId) ON DELETE CASCADE,
  CONSTRAINT FK_OrderItems_Items FOREIGN KEY (itemId) REFERENCES Items(itemId) ON DELETE RESTRICT
);

-- Shipments
CREATE TABLE Shipments (
  shipmentId BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  orderId BIGINT NOT NULL,
  trackingNumber VARCHAR(100),
  carrier VARCHAR(100),
  status VARCHAR(50),
  shippedAt DATETIME NULL,
  deliveredAt DATETIME NULL,
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  updatedAt DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  updatedBy VARCHAR(100),
  CONSTRAINT FK_Shipments_Orders FOREIGN KEY (orderId) REFERENCES Orders(orderId) ON DELETE CASCADE
);

-- Appointments
CREATE TABLE Appointments (
  appointmentId BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  orderId BIGINT NOT NULL,
  scheduledAt DATETIME NOT NULL,
  location VARCHAR(255),
  status VARCHAR(50) NOT NULL,
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  updatedAt DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  updatedBy VARCHAR(100),
  CONSTRAINT FK_Appointments_Orders FOREIGN KEY (orderId) REFERENCES Orders(orderId) ON DELETE CASCADE
);

-- Payments
CREATE TABLE Payments (
  paymentId BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  orderId BIGINT NOT NULL,
  paymentMethod VARCHAR(50) NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  status VARCHAR(50) NOT NULL,
  paidAt DATETIME NULL,
  transactionRef VARCHAR(100),
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  updatedAt DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  updatedBy VARCHAR(100),
  CONSTRAINT FK_Payments_Orders FOREIGN KEY (orderId) REFERENCES Orders(orderId) ON DELETE CASCADE
);

-- Wallets
CREATE TABLE Wallets (
  walletId BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  accountId BIGINT NOT NULL UNIQUE,
  balance DECIMAL(18,2) NOT NULL DEFAULT 0,
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  updatedAt DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  updatedBy VARCHAR(100),
  CONSTRAINT FK_Wallets_Accounts FOREIGN KEY (accountId) REFERENCES Accounts(accountId) ON DELETE CASCADE
);

-- WalletTransactions
CREATE TABLE WalletTransactions (
  walletTransactionId BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  walletId BIGINT NOT NULL,
  paymentId BIGINT NULL,
  transactionType VARCHAR(50) NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  description VARCHAR(255),
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  CONSTRAINT FK_WalletTransactions_Wallets FOREIGN KEY (walletId) REFERENCES Wallets(walletId) ON DELETE CASCADE,
  CONSTRAINT FK_WalletTransactions_Payments FOREIGN KEY (paymentId) REFERENCES Payments(paymentId) ON DELETE SET NULL
);

-- Refunds
CREATE TABLE Refunds (
  refundId BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  orderId BIGINT NOT NULL,
  paymentId BIGINT NULL,
  refundAmount DECIMAL(18,2) NOT NULL,
  reason VARCHAR(255),
  status VARCHAR(50) NOT NULL,
  requestedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completedAt DATETIME NULL,
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  updatedAt DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  updatedBy VARCHAR(100),
  CONSTRAINT FK_Refunds_Orders FOREIGN KEY (orderId) REFERENCES Orders(orderId) ON DELETE CASCADE,
  CONSTRAINT FK_Refunds_Payments FOREIGN KEY (paymentId) REFERENCES Payments(paymentId) ON DELETE SET NULL
);

-- Promotions
CREATE TABLE Promotions (
  promotionId BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  promotionCode VARCHAR(50) NOT NULL UNIQUE,
  promotionName VARCHAR(150),
  description VARCHAR(255),
  discountLevel VARCHAR(20) NOT NULL,
  discountType VARCHAR(20) NOT NULL,
  discountValue DECIMAL(18,2) NOT NULL,
  maxUsesTotal INT NULL,
  maxUsesPerUser INT NULL,
  minOrderAmount DECIMAL(18,2) NULL,
  startDate DATETIME NOT NULL,
  endDate DATETIME NOT NULL,
  isActive TINYINT(1) NOT NULL DEFAULT 1,
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  updatedAt DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  updatedBy VARCHAR(100)
);

-- PromotionItems (many-to-many Promotion <-> Item)
CREATE TABLE PromotionItems (
  promotionId BIGINT NOT NULL,
  itemId BIGINT NOT NULL,
  PRIMARY KEY (promotionId, itemId),
  CONSTRAINT FK_PromotionItems_Promotions FOREIGN KEY (promotionId) REFERENCES Promotions(promotionId) ON DELETE CASCADE,
  CONSTRAINT FK_PromotionItems_Items FOREIGN KEY (itemId) REFERENCES Items(itemId) ON DELETE CASCADE
);

-- PromotionUsages
CREATE TABLE PromotionUsages (
  promotionUsageId BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  promotionId BIGINT NOT NULL,
  orderId BIGINT NOT NULL,
  accountId BIGINT NOT NULL,
  discountAmount DECIMAL(18,2) NOT NULL,
  usedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy VARCHAR(100),
  CONSTRAINT FK_PromotionUsages_Promotions FOREIGN KEY (promotionId) REFERENCES Promotions(promotionId) ON DELETE CASCADE,
  CONSTRAINT FK_PromotionUsages_Orders FOREIGN KEY (orderId) REFERENCES Orders(orderId) ON DELETE CASCADE,
  CONSTRAINT FK_PromotionUsages_Accounts FOREIGN KEY (accountId) REFERENCES Accounts(accountId) ON DELETE CASCADE
);

-- Useful indexes
CREATE INDEX idx_items_sku ON Items(sku);
CREATE INDEX idx_orders_createdAt ON Orders(createdAt);
CREATE INDEX idx_orderitems_itemid ON OrderItems(itemId);
CREATE INDEX idx_stockitems_itemid ON StockItems(itemId);

-- =========================
-- SAMPLE DATA (SEED)
-- =========================

-- 1) ROLES
INSERT INTO Roles (roleCode, roleName, description, createdBy)
VALUES
('ADMIN','Administrator','System administrator','seed'),
('STAFF','Staff','Operational staff','seed'),
('CUSTOMER','Customer','End user / buyer','seed');

-- 2) ACCOUNTS (passwordHash placeholders - replace with real hashes)
INSERT INTO Accounts (username, passwordHash, email, isActive, isLocked, createdBy)
VALUES
('admin', '$2y$10$ADMIN_HASH_PLACEHOLDER', 'admin@example.com', 1, 0, 'seed'),
('staff1', '$2y$10$STAFF_HASH_PLACEHOLDER', 'staff1@example.com',1,0,'seed'),
('customer1', '$2y$10$CUST1_HASH_PLACEHOLDER', 'cust1@example.com',1,0,'seed'),
('customer2', '$2y$10$CUST2_HASH_PLACEHOLDER', 'cust2@example.com',1,0,'seed');

-- 3) ACCOUNT PROFILES (1-1)
INSERT INTO AccountProfiles (accountId, fullName, phoneNumber, addressLine, city, country, dateOfBirth, createdBy)
VALUES
((SELECT accountId FROM Accounts WHERE username='admin'), 'Nguyen Admin', '0123456789', '1 Admin St', 'Ho Chi Minh', 'Vietnam', '1990-01-01', 'seed'),
((SELECT accountId FROM Accounts WHERE username='staff1'), 'Le Staff', '0987654321', '2 Staff Rd', 'Ho Chi Minh', 'Vietnam', '1995-02-02', 'seed'),
((SELECT accountId FROM Accounts WHERE username='customer1'), 'Tran Customer', '0912345678', '10 Customer Ln', 'Ho Chi Minh', 'Vietnam', '1998-03-03', 'seed'),
((SELECT accountId FROM Accounts WHERE username='customer2'), 'Pham Buyer', '0909876543', '20 Buyer Blvd', 'Ho Chi Minh', 'Vietnam', '2000-04-04', 'seed');

-- 4) ACCOUNT ROLES
INSERT INTO AccountRoles (accountId, roleId)
VALUES
((SELECT accountId FROM Accounts WHERE username='admin'), (SELECT roleId FROM Roles WHERE roleCode='ADMIN')),
((SELECT accountId FROM Accounts WHERE username='staff1'), (SELECT roleId FROM Roles WHERE roleCode='STAFF')),
((SELECT accountId FROM Accounts WHERE username='customer1'), (SELECT roleId FROM Roles WHERE roleCode='CUSTOMER')),
((SELECT accountId FROM Accounts WHERE username='customer2'), (SELECT roleId FROM Roles WHERE roleCode='CUSTOMER'));

-- 5) CATEGORIES
INSERT INTO Categories (categoryCode, categoryName, description, createdBy)
VALUES
('CAT-APPAREL','Apparel & Fashion','Quần áo & phụ kiện','seed'),
('CAT-ELECTRONICS','Electronics','Thiết bị điện tử','seed'),
('CAT-ACCESS','Accessories','Phụ kiện & đồ dùng','seed');

-- 6) ITEMS (10 sample)
INSERT INTO Items (categoryId, sku, itemName, itemType, unitName, unitPrice, weightKg, description, isActive, createdBy)
VALUES
((SELECT categoryId FROM Categories WHERE categoryCode='CAT-APPAREL'),'SKU-001','Áo Thun Cotton Trơn','PRODUCT','Cái',129000.00,0.25,'Áo thun cotton co giãn','1','seed'),
((SELECT categoryId FROM Categories WHERE categoryCode='CAT-APPAREL'),'SKU-002','Áo Hoodie Nỉ Unisex','PRODUCT','Cái',299000.00,0.80,'Hoddie nỉ ấm','1','seed'),
((SELECT categoryId FROM Categories WHERE categoryCode='CAT-APPAREL'),'SKU-003','Quần Jean SlimFit Nam','PRODUCT','Cái',399000.00,0.70,'Quần jean form slim','1','seed'),
((SELECT categoryId FROM Categories WHERE categoryCode='CAT-APPAREL'),'SKU-004','Giày Sneaker Trắng','PRODUCT','Đôi',499000.00,1.00,'Giày sneaker basic','1','seed'),
((SELECT categoryId FROM Categories WHERE categoryCode='CAT-APPAREL'),'SKU-005','Balo Laptop 15.6"','PRODUCT','Cái',350000.00,0.90,'Balo chống sốc','1','seed'),
((SELECT categoryId FROM Categories WHERE categoryCode='CAT-ACCESS'),'SKU-006','Bình Giữ Nhiệt 1L','PRODUCT','Cái',159000.00,0.60,'Bình inox giữ nhiệt','1','seed'),
((SELECT categoryId FROM Categories WHERE categoryCode='CAT-ELECTRONICS'),'SKU-007','Tai Nghe Bluetooth Pro','PRODUCT','Cái',590000.00,0.15,'Tai nghe bluetooth','1','seed'),
((SELECT categoryId FROM Categories WHERE categoryCode='CAT-ELECTRONICS'),'SKU-008','Bàn Phím Cơ RGB 87 Key','PRODUCT','Cái',690000.00,0.85,'Bàn phím cơ RGB','1','seed'),
((SELECT categoryId FROM Categories WHERE categoryCode='CAT-ELECTRONICS'),'SKU-009','Chuột Gaming 7200DPI','PRODUCT','Cái',240000.00,0.20,'Chuột gaming','1','seed'),
((SELECT categoryId FROM Categories WHERE categoryCode='CAT-ACCESS'),'SKU-010','Dịch Vụ Vệ Sinh Laptop','SERVICE','Lần',120000.00,NULL,'Dịch vụ vệ sinh laptop','1','seed');

-- 7) WAREHOUSES
INSERT INTO Warehouses (warehouseCode, warehouseName, addressLine, city, country, createdBy)
VALUES
('WH-01','Kho Chính','100 Tran Hung Dao','Ho Chi Minh','Vietnam','seed'),
('WH-02','Kho Dự Trữ','200 Le Lai','Ho Chi Minh','Vietnam','seed');

-- 8) STOCKITEMS (set stock for most products in WH-01)
INSERT INTO StockItems (warehouseId, itemId, quantityOnHand, quantityReserved, lowStockThreshold, createdBy)
VALUES
((SELECT warehouseId FROM Warehouses WHERE warehouseCode='WH-01'), (SELECT itemId FROM Items WHERE sku='SKU-001'), 120, 0, 10, 'seed'),
((SELECT warehouseId FROM Warehouses WHERE warehouseCode='WH-01'), (SELECT itemId FROM Items WHERE sku='SKU-002'), 80, 0, 10, 'seed'),
((SELECT warehouseId FROM Warehouses WHERE warehouseCode='WH-01'), (SELECT itemId FROM Items WHERE sku='SKU-003'), 60, 0, 5, 'seed'),
((SELECT warehouseId FROM Warehouses WHERE warehouseCode='WH-01'), (SELECT itemId FROM Items WHERE sku='SKU-004'), 50, 0, 5, 'seed'),
((SELECT warehouseId FROM Warehouses WHERE warehouseCode='WH-01'), (SELECT itemId FROM Items WHERE sku='SKU-005'), 40, 0, 5, 'seed'),
((SELECT warehouseId FROM Warehouses WHERE warehouseCode='WH-01'), (SELECT itemId FROM Items WHERE sku='SKU-006'), 200, 0, 20, 'seed'),
((SELECT warehouseId FROM Warehouses WHERE warehouseCode='WH-01'), (SELECT itemId FROM Items WHERE sku='SKU-007'), 30, 0, 5, 'seed'),
((SELECT warehouseId FROM Warehouses WHERE warehouseCode='WH-01'), (SELECT itemId FROM Items WHERE sku='SKU-008'), 25, 0, 5, 'seed'),
((SELECT warehouseId FROM Warehouses WHERE warehouseCode='WH-01'), (SELECT itemId FROM Items WHERE sku='SKU-009'), 90, 0, 10, 'seed');

-- 9) CART for customer1 and customer2
INSERT INTO Carts (customerId, status, createdBy)
VALUES
((SELECT accountId FROM Accounts WHERE username='customer1'),'CART','seed'),
((SELECT accountId FROM Accounts WHERE username='customer2'),'CART','seed');

-- 10) CART ITEMS (customer1 added 2 items, customer2 1 item)
INSERT INTO CartItems (cartId, itemId, quantity, unitPrice, discountAmount, createdBy)
VALUES
((SELECT cartId FROM Carts WHERE customerId=(SELECT accountId FROM Accounts WHERE username='customer1') LIMIT 1), (SELECT itemId FROM Items WHERE sku='SKU-001'), 2, 129000.00, 0.00, 'seed'),
((SELECT cartId FROM Carts WHERE customerId=(SELECT accountId FROM Accounts WHERE username='customer1') LIMIT 1), (SELECT itemId FROM Items WHERE sku='SKU-009'), 1, 240000.00, 0.00, 'seed'),
((SELECT cartId FROM Carts WHERE customerId=(SELECT accountId FROM Accounts WHERE username='customer2') LIMIT 1), (SELECT itemId FROM Items WHERE sku='SKU-006'), 1, 159000.00, 0.00, 'seed');

-- 11) ORDERS (create 3 sample orders: one paid, one pending, one service appointment)
INSERT INTO Orders (customerId, cartId, orderNumber, status, shippingAddress, billingAddress, subtotal, discountTotal, taxAmount, shippingFee, codFee, gatewayFee, grandTotal, amountDue, createdBy)
VALUES
((SELECT accountId FROM Accounts WHERE username='customer1'), (SELECT cartId FROM Carts WHERE customerId=(SELECT accountId FROM Accounts WHERE username='customer1') LIMIT 1), 'ORD-2025001', 'PAID', '10 Customer Ln', '10 Customer Ln',  (129000.00*2 + 240000.00), 0.00, 0.00, 30000.00, 0.00, 0.00, (129000.00*2 + 240000.00 + 30000.00), 0.00, 'seed'),
((SELECT accountId FROM Accounts WHERE username='customer2'), (SELECT cartId FROM Carts WHERE customerId=(SELECT accountId FROM Accounts WHERE username='customer2') LIMIT 1), 'ORD-2025002', 'PENDING_PAYMENT', '20 Buyer Blvd', '20 Buyer Blvd', 159000.00, 0.00, 0.00, 0.00, 0.00, 0.00, 159000.00, 159000.00, 'seed'),
((SELECT accountId FROM Accounts WHERE username='customer1'), NULL, 'ORD-2025003', 'SCHEDULED', '10 Customer Ln', '10 Customer Ln', 120000.00,0.00,0.00,0.00,0.00,0.00,120000.00,120000.00,'seed');

-- 12) ORDER ITEMS (link to above orders)
INSERT INTO OrderItems (orderId, itemId, quantity, unitPrice, discountAmount, lineTotal, createdBy)
VALUES
((SELECT orderId FROM Orders WHERE orderNumber='ORD-2025001'), (SELECT itemId FROM Items WHERE sku='SKU-001'), 2, 129000.00, 0.00, 258000.00, 'seed'),
((SELECT orderId FROM Orders WHERE orderNumber='ORD-2025001'), (SELECT itemId FROM Items WHERE sku='SKU-009'), 1, 240000.00, 0.00, 240000.00, 'seed'),
((SELECT orderId FROM Orders WHERE orderNumber='ORD-2025002'), (SELECT itemId FROM Items WHERE sku='SKU-006'), 1, 159000.00, 0.00, 159000.00, 'seed'),
((SELECT orderId FROM Orders WHERE orderNumber='ORD-2025003'), (SELECT itemId FROM Items WHERE sku='SKU-010'), 1, 120000.00, 0.00, 120000.00, 'seed');

-- 13) SHIPMENTS (for order ORD-2025001)
INSERT INTO Shipments (orderId, trackingNumber, carrier, status, shippedAt, createdBy)
VALUES
((SELECT orderId FROM Orders WHERE orderNumber='ORD-2025001'), 'TRK2025001', 'GiaoHangNhanh', 'SHIPPED', NOW(), 'seed');

-- 14) APPOINTMENTS (for service order ORD-2025003)
INSERT INTO Appointments (orderId, scheduledAt, location, status, createdBy)
VALUES
((SELECT orderId FROM Orders WHERE orderNumber='ORD-2025003'), DATE_ADD(NOW(), INTERVAL 3 DAY), 'Cửa hàng 1', 'SCHEDULED', 'seed');

-- 15) PAYMENTS (one payment for ORD-2025001, one pending for ORD-2025002)
INSERT INTO Payments (orderId, paymentMethod, amount, status, paidAt, transactionRef, createdBy)
VALUES
((SELECT orderId FROM Orders WHERE orderNumber='ORD-2025001'), 'GATEWAY', (SELECT grandTotal FROM Orders WHERE orderNumber='ORD-2025001'), 'COMPLETED', NOW(), 'TXN-2025001', 'seed'),
((SELECT orderId FROM Orders WHERE orderNumber='ORD-2025002'), 'COD', (SELECT grandTotal FROM Orders WHERE orderNumber='ORD-2025002'), 'PENDING', NULL, NULL, 'seed');

-- 16) WALLETS (for customer1)
INSERT INTO Wallets (accountId, balance, createdBy)
VALUES
((SELECT accountId FROM Accounts WHERE username='customer1'), 500000.00, 'seed'),
((SELECT accountId FROM Accounts WHERE username='customer2'), 100000.00, 'seed');

-- 17) WALLET TRANSACTIONS (topup and payment)
INSERT INTO WalletTransactions (walletId, paymentId, transactionType, amount, description, createdBy)
VALUES
((SELECT walletId FROM Wallets WHERE accountId=(SELECT accountId FROM Accounts WHERE username='customer1')), NULL, 'TOPUP', 500000.00, 'Initial top-up', 'seed'),
((SELECT walletId FROM Wallets WHERE accountId=(SELECT accountId FROM Accounts WHERE username='customer1')), (SELECT paymentId FROM Payments WHERE orderId=(SELECT orderId FROM Orders WHERE orderNumber='ORD-2025001')), 'PAYMENT', (SELECT amount FROM Payments WHERE orderId=(SELECT orderId FROM Orders WHERE orderNumber='ORD-2025001')), 'Pay order ORD-2025001', 'seed');

-- 18) REFUNDS (example: refund request for ORD-2025001 - partial)
INSERT INTO Refunds (orderId, paymentId, refundAmount, reason, status, requestedAt, createdBy)
VALUES
((SELECT orderId FROM Orders WHERE orderNumber='ORD-2025001'), (SELECT paymentId FROM Payments WHERE orderId=(SELECT orderId FROM Orders WHERE orderNumber='ORD-2025001')), 50000.00, 'Item damaged', 'REQUESTED', NOW(), 'seed');

-- 19) PROMOTIONS
INSERT INTO Promotions (promotionCode, promotionName, description, discountLevel, discountType, discountValue, maxUsesTotal, maxUsesPerUser, minOrderAmount, startDate, endDate, isActive, createdBy)
VALUES
('PROMO10','10% Off Cart','10% off on entire cart','CART','PERCENT',10.00,1000,1,100000.00, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY),1,'seed'),
('ITEM50','50K Off Item','50k off specific item','ITEM','AMOUNT',50000.00,500,1,50000.00, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY),1,'seed');

-- 20) PROMOTION ITEMS (apply ITEM50 to SKU-004 and SKU-008)
INSERT INTO PromotionItems (promotionId, itemId)
VALUES
((SELECT promotionId FROM Promotions WHERE promotionCode='ITEM50'), (SELECT itemId FROM Items WHERE sku='SKU-004')),
((SELECT promotionId FROM Promotions WHERE promotionCode='ITEM50'), (SELECT itemId FROM Items WHERE sku='SKU-008'));

-- 21) PROMOTION USAGES (example usages)
INSERT INTO PromotionUsages (promotionId, orderId, accountId, discountAmount, createdBy)
VALUES
((SELECT promotionId FROM Promotions WHERE promotionCode='PROMO10'), (SELECT orderId FROM Orders WHERE orderNumber='ORD-2025002'), (SELECT accountId FROM Accounts WHERE username='customer2'), 15900.00, 'seed'),
((SELECT promotionId FROM Promotions WHERE promotionCode='ITEM50'), (SELECT orderId FROM Orders WHERE orderNumber='ORD-2025001'), (SELECT accountId FROM Accounts WHERE username='customer1'), 50000.00, 'seed');

-- 22) UPDATE stock quantities to reflect placed orders (decrease OnHand)
UPDATE StockItems s
JOIN OrderItems oi ON s.itemId = oi.itemId
JOIN Orders o ON oi.orderId = o.orderId
SET s.quantityOnHand = GREATEST(0, s.quantityOnHand - oi.quantity)
WHERE o.orderNumber IN ('ORD-2025001','ORD-2025002','ORD-2025003');

-- 23) OPTIONAL: mark updatedBy fields for seed provenance
UPDATE Accounts SET updatedBy='seed' WHERE username IN ('admin','staff1','customer1','customer2');
UPDATE Items SET updatedBy='seed';
UPDATE Orders SET updatedBy='seed';
UPDATE Payments SET updatedBy='seed';

-- =========================
-- Seed complete
-- =========================

