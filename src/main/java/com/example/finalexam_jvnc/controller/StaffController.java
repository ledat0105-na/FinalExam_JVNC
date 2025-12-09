package com.example.finalexam_jvnc.controller;

import com.example.finalexam_jvnc.dto.ItemDTO;
import com.example.finalexam_jvnc.dto.OrderDTO;
import com.example.finalexam_jvnc.dto.StockItemDTO;
import com.example.finalexam_jvnc.dto.WarehouseDTO;
import com.example.finalexam_jvnc.service.AccountService;
import com.example.finalexam_jvnc.service.ItemService;
import com.example.finalexam_jvnc.service.OrderService;
import com.example.finalexam_jvnc.service.RefundService;
import com.example.finalexam_jvnc.service.ShipmentService;
import com.example.finalexam_jvnc.service.StockItemService;
import com.example.finalexam_jvnc.service.WarehouseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private StockItemService stockItemService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private RefundService refundService;

    // Staff Login - kept for backward compatibility, but redirects to unified login
    @GetMapping("/login")
    public String showLoginPage() {
        return "redirect:/login";
    }

    // Staff Dashboard
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }
        
        // Count new orders (PENDING status)
        long newOrdersCount = 0;
        try {
            List<OrderDTO> pendingOrders = orderService.getOrdersByStatus("PENDING");
            newOrdersCount = pendingOrders != null ? pendingOrders.size() : 0;
        } catch (Exception e) {
            newOrdersCount = 0;
        }
        
        // Count pending refunds (PENDING or REQUESTED status)
        long pendingRefundsCount = 0;
        try {
            List<com.example.finalexam_jvnc.dto.RefundDTO> pendingRefunds = refundService.getRefundsByStatus("PENDING");
            List<com.example.finalexam_jvnc.dto.RefundDTO> requestedRefunds = refundService.getRefundsByStatus("REQUESTED");
            if (pendingRefunds != null) {
                pendingRefundsCount += pendingRefunds.size();
            }
            if (requestedRefunds != null) {
                pendingRefundsCount += requestedRefunds.size();
            }
        } catch (Exception e) {
            pendingRefundsCount = 0;
        }
        
        // Calculate total tasks (new orders + pending refunds)
        long totalTasksToday = newOrdersCount + pendingRefundsCount;
        
        model.addAttribute("staffUsername", staffUsername);
        model.addAttribute("newOrdersCount", newOrdersCount);
        model.addAttribute("pendingRefundsCount", pendingRefundsCount);
        model.addAttribute("totalTasksToday", totalTasksToday);
        return "staff/dashboard-staff";
    }

    // View Products List for Staff
    @GetMapping("/items")
    public String listItems(HttpSession session, Model model) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }

        // Get all items
        List<ItemDTO> items = itemService.getAllItems();
        
        // Get all stock items to calculate total stock per item
        List<StockItemDTO> stockItems = stockItemService.getAllStockItems();
        
        // Create maps to store total stock per item
        Map<Long, Integer> totalStockMap = new HashMap<>();
        Map<Long, Integer> totalAvailableMap = new HashMap<>();
        
        for (StockItemDTO stockItem : stockItems) {
            Long itemId = stockItem.getItemId();
            totalStockMap.put(itemId, 
                totalStockMap.getOrDefault(itemId, 0) + stockItem.getQuantityOnHand());
            totalAvailableMap.put(itemId, 
                totalAvailableMap.getOrDefault(itemId, 0) + 
                (stockItem.getAvailableQuantity() != null ? stockItem.getAvailableQuantity() : 0));
        }

        model.addAttribute("items", items);
        model.addAttribute("totalStockMap", totalStockMap);
        model.addAttribute("totalAvailableMap", totalAvailableMap);
        model.addAttribute("staffUsername", staffUsername);
        return "staff/items-list";
    }

    // View Stock Management for Staff
    @GetMapping("/stock")
    public String listStock(HttpSession session, 
                            Model model,
                            @RequestParam(required = false) Long warehouseId,
                            @RequestParam(required = false) Long itemId) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }

        List<StockItemDTO> stockItems;
        
        // Filter by warehouse and/or item if provided
        if (warehouseId != null && itemId != null) {
            // Filter by both warehouse and item
            List<StockItemDTO> warehouseItems = stockItemService.getStockItemsByWarehouse(warehouseId);
            stockItems = warehouseItems.stream()
                    .filter(si -> si.getItemId().equals(itemId))
                    .collect(Collectors.toList());
        } else if (warehouseId != null) {
            stockItems = stockItemService.getStockItemsByWarehouse(warehouseId);
        } else if (itemId != null) {
            stockItems = stockItemService.getStockItemsByItem(itemId);
        } else {
            stockItems = stockItemService.getAllStockItems();
        }

        List<WarehouseDTO> warehouses = warehouseService.getActiveWarehouses();
        List<ItemDTO> items = itemService.getActiveItems();

        model.addAttribute("stockItems", stockItems);
        model.addAttribute("warehouses", warehouses);
        model.addAttribute("items", items);
        model.addAttribute("selectedWarehouseId", warehouseId);
        model.addAttribute("selectedItemId", itemId);
        model.addAttribute("staffUsername", staffUsername);
        return "staff/stock-list";
    }

    // Show Edit Stock Form
    @GetMapping("/stock/{id}/edit")
    public String showEditStockForm(@PathVariable Long id, 
                                   HttpSession session, 
                                   Model model) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }

        StockItemDTO stockItem = stockItemService.getStockItemById(id);
        model.addAttribute("stockItem", stockItem);
        model.addAttribute("staffUsername", staffUsername);
        return "staff/stock-edit";
    }

    // Update Stock Quantity
    @PostMapping("/stock/{id}/update")
    public String updateStock(@PathVariable Long id,
                             @RequestParam Integer quantityOnHand,
                             @RequestParam(required = false) Integer lowStockThreshold,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }

        try {
            stockItemService.updateStockQuantity(id, quantityOnHand, lowStockThreshold);
            redirectAttributes.addFlashAttribute("success", "Cập nhật tồn kho thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật tồn kho: " + e.getMessage());
        }

        return "redirect:/staff/stock";
    }

    // Show Import Stock Form
    @GetMapping("/stock/{id}/import")
    public String showImportStockForm(@PathVariable Long id, 
                                     HttpSession session, 
                                     Model model) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }

        StockItemDTO stockItem = stockItemService.getStockItemById(id);
        model.addAttribute("stockItem", stockItem);
        model.addAttribute("staffUsername", staffUsername);
        return "staff/stock-import";
    }

    // Process Import Stock
    @PostMapping("/stock/{id}/import")
    public String importStock(@PathVariable Long id,
                             @RequestParam Integer quantity,
                             @RequestParam(required = false) String note,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }

        try {
            StockItemDTO currentStock = stockItemService.getStockItemById(id);
            Integer newQuantity = currentStock.getQuantityOnHand() + quantity;
            
            if (newQuantity < 0) {
                redirectAttributes.addFlashAttribute("error", "Số lượng sau khi nhập không được âm!");
                return "redirect:/staff/stock/" + id + "/import";
            }
            
            stockItemService.updateStockQuantity(id, newQuantity, null);
            redirectAttributes.addFlashAttribute("success", 
                "Nhập kho thành công! Đã thêm " + quantity + " sản phẩm vào kho.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi nhập kho: " + e.getMessage());
        }

        return "redirect:/staff/stock";
    }

    // Show Export Stock Form
    @GetMapping("/stock/{id}/export")
    public String showExportStockForm(@PathVariable Long id, 
                                     HttpSession session, 
                                     Model model) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }

        StockItemDTO stockItem = stockItemService.getStockItemById(id);
        model.addAttribute("stockItem", stockItem);
        model.addAttribute("staffUsername", staffUsername);
        return "staff/stock-export";
    }

    // Process Export Stock
    @PostMapping("/stock/{id}/export")
    public String exportStock(@PathVariable Long id,
                             @RequestParam Integer quantity,
                             @RequestParam(required = false) String note,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }

        try {
            StockItemDTO currentStock = stockItemService.getStockItemById(id);
            Integer availableQuantity = currentStock.getAvailableQuantity();
            
            if (quantity <= 0) {
                redirectAttributes.addFlashAttribute("error", "Số lượng xuất kho phải lớn hơn 0!");
                return "redirect:/staff/stock/" + id + "/export";
            }
            
            if (quantity > availableQuantity) {
                redirectAttributes.addFlashAttribute("error", 
                    "Số lượng xuất kho (" + quantity + ") vượt quá số lượng khả dụng (" + availableQuantity + ")!");
                return "redirect:/staff/stock/" + id + "/export";
            }
            
            Integer newQuantity = currentStock.getQuantityOnHand() - quantity;
            stockItemService.updateStockQuantity(id, newQuantity, null);
            redirectAttributes.addFlashAttribute("success", 
                "Xuất kho thành công! Đã xuất " + quantity + " sản phẩm khỏi kho.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xuất kho: " + e.getMessage());
        }

        return "redirect:/staff/stock";
    }

    // View Low Stock Alerts
    @GetMapping("/stock/low-stock")
    public String viewLowStockAlerts(HttpSession session, Model model) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }

        List<StockItemDTO> lowStockItems = stockItemService.getLowStockItems();
        model.addAttribute("lowStockItems", lowStockItems);
        model.addAttribute("staffUsername", staffUsername);
        return "staff/stock-low-stock";
    }

    // View Orders List for Staff
    @GetMapping("/orders")
    public String listOrders(HttpSession session, 
                            Model model,
                            @RequestParam(required = false) String status) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }

        List<OrderDTO> orders;
        
        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            orders = orderService.getOrdersByStatus(status);
        } else {
            orders = orderService.getAllOrders();
        }

        // Define available statuses
        List<String> availableStatuses = List.of(
            "PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "DONE", "CANCELLED"
        );

        model.addAttribute("orders", orders);
        model.addAttribute("availableStatuses", availableStatuses);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("staffUsername", staffUsername);
        return "staff/orders-list";
    }

    // Update Order Status
    @PostMapping("/orders/{id}/update-status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }

        try {
            orderService.updateOrderStatus(id, status);
            String statusName = getStatusName(status);
            redirectAttributes.addFlashAttribute("success", 
                "Cập nhật trạng thái đơn hàng thành công! Trạng thái mới: " + statusName);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }

        return "redirect:/staff/orders";
    }

    // Create Shipment Form
    @GetMapping("/orders/{id}/create-shipment")
    public String showCreateShipmentForm(@PathVariable Long id,
                                        HttpSession session,
                                        Model model) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }

        try {
            OrderDTO order = orderService.getOrderById(id);
            model.addAttribute("order", order);
            model.addAttribute("staffUsername", staffUsername);
            return "staff/shipment-create";
        } catch (Exception e) {
            return "redirect:/staff/orders?error=Order not found";
        }
    }

    // Create Shipment Handler
    @PostMapping("/orders/{id}/create-shipment")
    public String createShipment(@PathVariable Long id,
                                @RequestParam String trackingNumber,
                                @RequestParam String carrier,
                                @RequestParam(required = false, defaultValue = "PENDING") String status,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }

        try {
            shipmentService.createShipment(id, trackingNumber, carrier, status);
            redirectAttributes.addFlashAttribute("success", 
                "Tạo vận đơn thành công! Mã vận đơn: " + trackingNumber);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Lỗi khi tạo vận đơn: " + e.getMessage());
        }

        return "redirect:/staff/orders";
    }

    // View Refunds List for Staff
    @GetMapping("/refunds")
    public String listRefunds(HttpSession session,
                              Model model,
                              @RequestParam(required = false) String status) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }

        List<com.example.finalexam_jvnc.dto.RefundDTO> refunds;
        
        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            refunds = refundService.getRefundsByStatus(status);
        } else {
            refunds = refundService.getAllRefunds();
        }

        model.addAttribute("refunds", refunds);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("staffUsername", staffUsername);
        return "staff/refunds-list";
    }

    // Approve Refund
    @PostMapping("/refunds/{id}/approve")
    public String approveRefund(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }

        try {
            refundService.approveRefund(id);
            redirectAttributes.addFlashAttribute("success", 
                "Đã duyệt yêu cầu hoàn tiền thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Lỗi khi duyệt yêu cầu hoàn tiền: " + e.getMessage());
        }

        return "redirect:/staff/refunds";
    }

    // Reject Refund
    @PostMapping("/refunds/{id}/reject")
    public String rejectRefund(@PathVariable Long id,
                               @RequestParam(required = false) String reason,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }

        try {
            refundService.rejectRefund(id, reason != null ? reason : "Không đủ điều kiện hoàn tiền");
            redirectAttributes.addFlashAttribute("success", 
                "Đã từ chối yêu cầu hoàn tiền thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Lỗi khi từ chối yêu cầu hoàn tiền: " + e.getMessage());
        }

        return "redirect:/staff/refunds";
    }

    // Update Refund Status
    @PostMapping("/refunds/{id}/update-status")
    public String updateRefundStatus(@PathVariable Long id,
                                    @RequestParam String status,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        String staffUsername = (String) session.getAttribute("staffUsername");
        if (staffUsername == null || !accountService.isStaff(staffUsername)) {
            return "redirect:/login";
        }

        try {
            refundService.updateRefundStatus(id, status);
            String statusName = getRefundStatusName(status);
            redirectAttributes.addFlashAttribute("success", 
                "Cập nhật trạng thái hoàn tiền thành công! Trạng thái mới: " + statusName);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }

        return "redirect:/staff/refunds";
    }

    private String getRefundStatusName(String status) {
        return switch (status) {
            case "PENDING" -> "Chờ xử lý";
            case "REQUESTED" -> "Đã yêu cầu";
            case "APPROVED" -> "Đã duyệt";
            case "REJECTED" -> "Đã từ chối";
            case "PROCESSING" -> "Đang xử lý";
            case "COMPLETED" -> "Hoàn thành";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }

    private String getStatusName(String status) {
        return switch (status) {
            case "PENDING" -> "Chờ xử lý";
            case "PROCESSING" -> "Đang xử lý";
            case "SHIPPED" -> "Đã gửi hàng";
            case "DELIVERED" -> "Đã giao hàng";
            case "DONE" -> "Hoàn thành";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }
}

