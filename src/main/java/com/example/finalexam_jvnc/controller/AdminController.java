package com.example.finalexam_jvnc.controller;

import com.example.finalexam_jvnc.dto.*;

import com.example.finalexam_jvnc.repository.AccountRepository;
import com.example.finalexam_jvnc.repository.RefundRepository;
import com.example.finalexam_jvnc.service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private StockItemService stockItemService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private RefundService refundService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private PromotionUsageService promotionUsageService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RefundRepository refundRepository;

    // Admin Login - kept for backward compatibility, but redirects to unified login
    @GetMapping("/login")
    public String showLoginPage() {
        return "redirect:/login";
    }

    // Admin Dashboard
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        // Get statistics for dashboard
        long totalUsers = 0;
        long pendingRefunds = 0;
        long todayAccess = 0;
        long lowStockAlerts = 0;

        try {
            totalUsers = accountRepository.count();
        } catch (Exception e) {
            totalUsers = 0;
        }

        // Count pending refunds (PENDING or REQUESTED status)
        try {
            List<RefundDTO> pendingRefundsList = refundService.getRefundsByStatus("PENDING");
            List<RefundDTO> requestedRefundsList = refundService.getRefundsByStatus("REQUESTED");
            if (pendingRefundsList != null) {
                pendingRefunds += pendingRefundsList.size();
            }
            if (requestedRefundsList != null) {
                pendingRefunds += requestedRefundsList.size();
            }
        } catch (Exception e) {
            // If there's an error, set to 0
            pendingRefunds = 0;
        }

        // Count accounts that logged in today
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
            Long count = accountRepository.countAccountsLoggedInToday(startOfDay, endOfDay);
            todayAccess = count != null ? count : 0;
        } catch (Exception e) {
            todayAccess = 0;
        }

        // Count low stock alerts
        try {
            List<StockItemDTO> lowStockItems = stockItemService.getLowStockItems();
            lowStockAlerts = lowStockItems != null ? lowStockItems.size() : 0;
        } catch (Exception e) {
            lowStockAlerts = 0;
        }

        // Get today's order/refund statistics
        long totalOrdersToday = 0;
        long totalRefundsToday = 0;
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            com.example.finalexam_jvnc.dto.RevenueReportDTO todayReport = reportService.getRevenueReportByDay(today);
            if (todayReport != null) {
                totalOrdersToday = todayReport.getTotalOrders() != null ? todayReport.getTotalOrders() : 0L;
                totalRefundsToday = todayReport.getTotalRefunds() != null ? todayReport.getTotalRefunds() : 0L;
            }
        } catch (Exception e) {
            totalOrdersToday = 0;
            totalRefundsToday = 0;
        }

        model.addAttribute("adminUsername", adminUsername);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("pendingApprovals", pendingRefunds);
        model.addAttribute("todayAccess", todayAccess);
        model.addAttribute("lowStockAlerts", lowStockAlerts);
        model.addAttribute("totalOrdersToday", totalOrdersToday);
        model.addAttribute("totalRefundsToday", totalRefundsToday);
        return "admin/dashboard-admin";
    }

    // View all accounts
    @GetMapping("/accounts")
    public String listAccounts(HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        List<AccountDTO> accounts = adminService.getAllAccountsForAdmin();
        model.addAttribute("accounts", accounts);
        return "admin/accounts-list-admin";
    }

    // Show create account form
    @GetMapping("/accounts/new")
    public String showCreateAccountForm(HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        model.addAttribute("availableRoles", List.of("ADMIN", "STAFF", "CUSTOMER"));
        return "admin/account-create-admin";
    }

    // Handle create account
    @PostMapping("/accounts/new")
    public String createAccount(@RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam(required = false) List<String> roleCodes,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp");
            return "redirect:/admin/accounts/new";
        }

        // Validate password length
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự");
            return "redirect:/admin/accounts/new";
        }

        // Validate roles
        if (roleCodes == null || roleCodes.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất một vai trò");
            return "redirect:/admin/accounts/new";
        }

        try {
            accountService.createAccount(username, email, password, roleCodes);
            redirectAttributes.addFlashAttribute("success", "Tạo tài khoản thành công");
            return "redirect:/admin/accounts";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/accounts/new";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Tạo tài khoản thất bại: " + e.getMessage());
            return "redirect:/admin/accounts/new";
        }
    }

    // View account details with audit log
    @GetMapping("/accounts/{id}")
    public String viewAccountDetails(@PathVariable Long id,
            HttpSession session,
            Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        AccountDTO account = adminService.getAccountDetails(id);
        model.addAttribute("account", account);
        return "admin/account-details-admin";
    }

    // Lock/Unlock account
    @PostMapping("/accounts/{id}/toggle-lock")
    public String toggleAccountLock(@PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        AccountDTO account = adminService.toggleAccountLock(id);
        String message = Boolean.TRUE.equals(account.getIsLocked())
                ? "Account locked successfully"
                : "Account unlocked successfully";
        redirectAttributes.addFlashAttribute("success", message);
        return "redirect:/admin/accounts";
    }

    // Assign roles
    @GetMapping("/accounts/{id}/roles")
    public String showRoleAssignmentPage(@PathVariable Long id,
            HttpSession session,
            Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        AccountDTO account = adminService.getAccountDetails(id);
        model.addAttribute("account", account);
        model.addAttribute("availableRoles", List.of("ADMIN", "STAFF", "CUSTOMER"));
        return "admin/assign-roles-admin";
    }

    @PostMapping("/accounts/{id}/roles")
    public String assignRoles(@PathVariable Long id,
            @RequestParam("roleCodes") List<String> roleCodes,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        adminService.updateAccountRoles(id, roleCodes);
        redirectAttributes.addFlashAttribute("success", "Roles assigned successfully");
        return "redirect:/admin/accounts";
    }

    // View audit log
    @GetMapping("/audit-log")
    public String viewAuditLog(HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        List<AccountDTO> accounts = adminService.getAuditLog();
        model.addAttribute("accounts", accounts);
        return "admin/audit-log-admin";
    }

    // ========== CATEGORY MANAGEMENT ==========

    @GetMapping("/categories")
    public String listCategories(HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        List<CategoryDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "admin/categories-list";
    }

    @GetMapping("/categories/new")
    public String showCreateCategoryForm(HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        model.addAttribute("category", new CategoryDTO());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/category-form";
    }

    @GetMapping("/categories/{id}/edit")
    public String showEditCategoryForm(@PathVariable Long id, HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        CategoryDTO category = categoryService.getCategoryById(id);
        model.addAttribute("category", category);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/category-form";
    }

    @PostMapping("/categories")
    public String saveCategory(@ModelAttribute CategoryDTO categoryDTO,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        try {
            if (categoryDTO.getCategoryId() == null) {
                categoryService.createCategory(categoryDTO);
                redirectAttributes.addFlashAttribute("success", "Category created successfully");
            } else {
                categoryService.updateCategory(categoryDTO.getCategoryId(), categoryDTO);
                redirectAttributes.addFlashAttribute("success", "Category updated successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/categories/new";
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // ========== ITEM MANAGEMENT ==========

    @GetMapping("/items")
    public String listItems(HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        List<ItemDTO> items = itemService.getAllItems();
        model.addAttribute("items", items);
        return "admin/items-list";
    }

    @GetMapping("/items/new")
    public String showCreateItemForm(HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        model.addAttribute("item", new ItemDTO());
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "admin/item-form";
    }

    @GetMapping("/items/{id}/edit")
    public String showEditItemForm(@PathVariable Long id, HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        ItemDTO item = itemService.getItemById(id);
        model.addAttribute("item", item);
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "admin/item-form";
    }

    @PostMapping("/items")
    public String saveItem(@ModelAttribute ItemDTO itemDTO,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        try {
            if (itemDTO.getItemId() == null) {
                itemService.createItem(itemDTO);
                redirectAttributes.addFlashAttribute("success", "Item created successfully");
            } else {
                itemService.updateItem(itemDTO.getItemId(), itemDTO);
                redirectAttributes.addFlashAttribute("success", "Item updated successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return itemDTO.getItemId() == null ? "redirect:/admin/items/new"
                    : "redirect:/admin/items/" + itemDTO.getItemId() + "/edit";
        }
        return "redirect:/admin/items";
    }

    @PostMapping("/items/{id}/delete")
    public String deleteItem(@PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        try {
            itemService.deleteItem(id);
            redirectAttributes.addFlashAttribute("success", "Item deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/items";
    }

    @GetMapping("/items/import")
    public String showImportItemsForm(HttpSession session) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        return "admin/items-import";
    }

    @PostMapping("/items/import")
    public String importItems(@RequestParam("file") MultipartFile file,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        try {
            List<ItemDTO> importedItems = itemService.importFromCSV(file);
            redirectAttributes.addFlashAttribute("success",
                    "Successfully imported " + importedItems.size() + " items");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Import failed: " + e.getMessage());
        }
        return "redirect:/admin/items";
    }

    @GetMapping("/items/export")
    public void exportItems(HttpSession session, HttpServletResponse response) throws Exception {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            response.sendRedirect("/login");
            return;
        }
        byte[] csvData = itemService.exportToCSV();
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=items_export.csv");
        response.setContentLength(csvData.length);
        response.getOutputStream().write(csvData);
        response.getOutputStream().flush();
    }

    // ========== WAREHOUSE MANAGEMENT ==========

    @GetMapping("/warehouses")
    public String listWarehouses(HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        List<WarehouseDTO> warehouses = warehouseService.getAllWarehouses();
        model.addAttribute("warehouses", warehouses);
        return "admin/warehouses-list";
    }

    @GetMapping("/warehouses/new")
    public String showCreateWarehouseForm(HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        model.addAttribute("warehouse", new WarehouseDTO());
        return "admin/warehouse-form";
    }

    @GetMapping("/warehouses/{id}/edit")
    public String showEditWarehouseForm(@PathVariable Long id, HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        WarehouseDTO warehouse = warehouseService.getWarehouseById(id);
        model.addAttribute("warehouse", warehouse);
        return "admin/warehouse-form";
    }

    @PostMapping("/warehouses")
    public String saveWarehouse(@ModelAttribute WarehouseDTO warehouseDTO,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        try {
            if (warehouseDTO.getWarehouseId() == null) {
                warehouseService.createWarehouse(warehouseDTO);
                redirectAttributes.addFlashAttribute("success", "Warehouse created successfully");
            } else {
                warehouseService.updateWarehouse(warehouseDTO.getWarehouseId(), warehouseDTO);
                redirectAttributes.addFlashAttribute("success", "Warehouse updated successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return warehouseDTO.getWarehouseId() == null ? "redirect:/admin/warehouses/new"
                    : "redirect:/admin/warehouses/" + warehouseDTO.getWarehouseId() + "/edit";
        }
        return "redirect:/admin/warehouses";
    }

    @PostMapping("/warehouses/{id}/delete")
    public String deleteWarehouse(@PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        try {
            warehouseService.deleteWarehouse(id);
            redirectAttributes.addFlashAttribute("success", "Warehouse deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/warehouses";
    }

    // ========== REFUND MANAGEMENT ==========

    @GetMapping("/refunds")
    public String listRefunds(HttpSession session,
            Model model,
            @RequestParam(required = false) String status) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
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
        model.addAttribute("adminUsername", adminUsername);
        return "admin/refunds-list";
    }

    @PostMapping("/refunds/{id}/update-status")
    public String updateRefundStatus(@PathVariable Long id,
            @RequestParam String status,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
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

        return "redirect:/admin/refunds";
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

    // ========== SYSTEM CONFIG MANAGEMENT ==========

    @GetMapping("/system-config")
    public String listSystemConfigs(HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        List<SystemConfigDTO> configs = systemConfigService.getAllConfigs();
        model.addAttribute("configs", configs);
        model.addAttribute("adminUsername", adminUsername);
        return "admin/system-config-list";
    }

    @PostMapping("/system-config/{key}/update")
    public String updateSystemConfig(@PathVariable String key,
            @RequestParam String value,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        try {
            systemConfigService.updateConfig(key, value);
            redirectAttributes.addFlashAttribute("success",
                    "Cập nhật cấu hình hệ thống thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi cập nhật cấu hình: " + e.getMessage());
        }

        return "redirect:/admin/system-config";
    }

    @GetMapping("/system-config/new")
    public String showCreateSystemConfigForm(HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        model.addAttribute("config", new SystemConfigDTO());
        model.addAttribute("adminUsername", adminUsername);
        return "admin/system-config-form";
    }

    @PostMapping("/system-config")
    public String createSystemConfig(@ModelAttribute SystemConfigDTO configDTO,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        try {
            systemConfigService.createOrUpdateConfig(configDTO);
            redirectAttributes.addFlashAttribute("success",
                    "Tạo cấu hình hệ thống thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi tạo cấu hình: " + e.getMessage());
            return "redirect:/admin/system-config/new";
        }

        return "redirect:/admin/system-config";
    }

    @PostMapping("/system-config/{key}/delete")
    public String deleteSystemConfig(@PathVariable String key,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        try {
            systemConfigService.deleteConfig(key);
            redirectAttributes.addFlashAttribute("success",
                    "Xóa cấu hình hệ thống thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi xóa cấu hình: " + e.getMessage());
        }

        return "redirect:/admin/system-config";
    }

    @PostMapping("/system-config/{key}/toggle-active")
    public String toggleSystemConfigActive(@PathVariable String key,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        try {
            SystemConfigDTO config = systemConfigService.toggleActive(key);
            String status = config.getIsActive() ? "kích hoạt" : "vô hiệu hóa";
            redirectAttributes.addFlashAttribute("success",
                    "Đã " + status + " cấu hình hệ thống thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }

        return "redirect:/admin/system-config";
    }

    // ========== STOCK MANAGEMENT ==========

    @GetMapping("/stock/low-stock")
    public String viewLowStockAlerts(HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        List<StockItemDTO> lowStockItems = stockItemService.getLowStockItems();
        model.addAttribute("lowStockItems", lowStockItems);
        model.addAttribute("adminUsername", adminUsername);
        return "admin/stock-low-stock";
    }

    // ========== ORDER MANAGEMENT ==========

    @GetMapping("/orders")
    public String listOrders(HttpSession session,
            Model model,
            @RequestParam(required = false) String status) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
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
                "PENDING_CONFIRMATION", "PROCESSING", "SHIPPED", "DELIVERED", "DONE", "CANCELLED");

        model.addAttribute("orders", orders);
        model.addAttribute("availableStatuses", availableStatuses);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("adminUsername", adminUsername);
        return "admin/orders-list";
    }

    @PostMapping("/orders/{id}/update-status")
    public String updateOrderStatus(@PathVariable Long id,
            @RequestParam String status,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        try {
            orderService.updateOrderStatus(id, status);
            String statusName = getOrderStatusName(status);
            redirectAttributes.addFlashAttribute("success",
                    "Cập nhật trạng thái đơn hàng thành công! Trạng thái mới: " + statusName);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }

        return "redirect:/admin/orders";
    }

    private String getOrderStatusName(String status) {
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

    // ========== SHIPMENT MANAGEMENT ==========

    @GetMapping("/shipments")
    public String listShipments(HttpSession session,
            Model model,
            @RequestParam(required = false) String status) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        List<ShipmentDTO> shipments;

        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            shipments = shipmentService.getShipmentsByStatus(status);
        } else {
            shipments = shipmentService.getAllShipments();
        }

        // Define available statuses
        List<String> availableStatuses = List.of(
                "PENDING", "SHIPPED", "IN_TRANSIT", "DELIVERED", "CANCELLED");

        model.addAttribute("shipments", shipments);
        model.addAttribute("availableStatuses", availableStatuses);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("adminUsername", adminUsername);
        return "admin/shipments-list";
    }

    // ========== FEES CONFIGURATION ==========

    @GetMapping("/fees")
    public String showFeesConfig(HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        // Get current fee values or default to 0
        Double shippingFee = systemConfigService.getShippingFee();
        Double taxRate = systemConfigService.getTaxRate();
        Double codFee = systemConfigService.getCodFee();
        Double gatewayFee = systemConfigService.getGatewayFee();

        model.addAttribute("shippingFee", shippingFee != null ? shippingFee : 0.0);
        model.addAttribute("taxRate", taxRate != null ? taxRate : 0.0);
        model.addAttribute("codFee", codFee != null ? codFee : 0.0);
        model.addAttribute("gatewayFee", gatewayFee != null ? gatewayFee : 0.0);
        model.addAttribute("adminUsername", adminUsername);
        return "admin/fees-config";
    }

    @PostMapping("/fees")
    public String updateFeesConfig(@RequestParam(required = false) Double shippingFee,
            @RequestParam(required = false) Double taxRate,
            @RequestParam(required = false) Double codFee,
            @RequestParam(required = false) Double gatewayFee,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        try {
            // Update or create each fee configuration
            if (shippingFee != null) {
                SystemConfigDTO config = new SystemConfigDTO();
                config.setConfigKey("SHIPPING_FEE");
                config.setConfigName("Phí vận chuyển");
                config.setConfigValue(String.valueOf(shippingFee));
                config.setDescription("Phí vận chuyển hàng hóa (VNĐ)");
                config.setIsActive(true);
                systemConfigService.createOrUpdateConfig(config);
            }

            if (taxRate != null) {
                SystemConfigDTO config = new SystemConfigDTO();
                config.setConfigKey("TAX_RATE");
                config.setConfigName("Tỷ lệ thuế");
                config.setConfigValue(String.valueOf(taxRate));
                config.setDescription("Tỷ lệ thuế VAT (số thập phân, VD: 0.1 = 10%)");
                config.setIsActive(true);
                systemConfigService.createOrUpdateConfig(config);
            }

            if (codFee != null) {
                SystemConfigDTO config = new SystemConfigDTO();
                config.setConfigKey("COD_FEE");
                config.setConfigName("Phí thu hộ COD");
                config.setConfigValue(String.valueOf(codFee));
                config.setDescription("Phí thu hộ COD (VNĐ)");
                config.setIsActive(true);
                systemConfigService.createOrUpdateConfig(config);
            }

            if (gatewayFee != null) {
                SystemConfigDTO config = new SystemConfigDTO();
                config.setConfigKey("GATEWAY_FEE");
                config.setConfigName("Phí cổng thanh toán");
                config.setConfigValue(String.valueOf(gatewayFee));
                config.setDescription("Phí cổng thanh toán (VNĐ hoặc %)");
                config.setIsActive(true);
                systemConfigService.createOrUpdateConfig(config);
            }

            redirectAttributes.addFlashAttribute("success",
                    "Cập nhật cấu hình phí hệ thống thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Lỗi khi cập nhật cấu hình: " + e.getMessage());
        }

        return "redirect:/admin/fees";
    }

    // ========== PROMOTION MANAGEMENT ==========

    @GetMapping("/promotions")
    public String listPromotions(HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        List<PromotionDTO> promotions = promotionService.getAllPromotions();
        model.addAttribute("promotions", promotions);
        model.addAttribute("adminUsername", adminUsername);
        return "admin/promotions-list";
    }

    @GetMapping("/promotions/new")
    public String showCreatePromotionForm(HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        model.addAttribute("promotion", new PromotionDTO());
        model.addAttribute("adminUsername", adminUsername);
        return "admin/promotion-form";
    }

    @GetMapping("/promotions/{id}/edit")
    public String showEditPromotionForm(@PathVariable Long id, HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        PromotionDTO promotion = promotionService.getPromotionById(id);
        model.addAttribute("promotion", promotion);
        model.addAttribute("adminUsername", adminUsername);
        return "admin/promotion-form";
    }

    @PostMapping("/promotions")
    public String savePromotion(@ModelAttribute PromotionDTO promotionDTO,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        try {
            if (promotionDTO.getPromotionId() == null) {
                promotionService.createPromotion(promotionDTO);
                redirectAttributes.addFlashAttribute("success", "Mã khuyến mãi đã được tạo thành công!");
            } else {
                promotionService.updatePromotion(promotionDTO.getPromotionId(), promotionDTO);
                redirectAttributes.addFlashAttribute("success", "Mã khuyến mãi đã được cập nhật thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return promotionDTO.getPromotionId() == null ? "redirect:/admin/promotions/new"
                    : "redirect:/admin/promotions/" + promotionDTO.getPromotionId() + "/edit";
        }

        return "redirect:/admin/promotions";
    }

    @PostMapping("/promotions/{id}/delete")
    public String deletePromotion(@PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        try {
            promotionService.deletePromotion(id);
            redirectAttributes.addFlashAttribute("success", "Mã khuyến mãi đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa: " + e.getMessage());
        }

        return "redirect:/admin/promotions";
    }

    @GetMapping("/promotion-usages")
    public String listPromotionUsages(HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        var usages = promotionUsageService.getAllPromotionUsages();
        model.addAttribute("usages", usages);
        model.addAttribute("adminUsername", adminUsername);
        return "admin/promotion-usage-list";
    }

    @GetMapping("/revenue-report")
    public String revenueReport(@RequestParam(value = "mode", required = false, defaultValue = "day") String mode,
            @RequestParam(value = "day", required = false) String day,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }
        var now = java.time.LocalDate.now();
        com.example.finalexam_jvnc.dto.RevenueReportDTO report = null;
        if ("day".equals(mode)) {
            java.time.LocalDate targetDay = (day != null && !day.isEmpty()) ? java.time.LocalDate.parse(day) : now;
            report = reportService.getRevenueReportByDay(targetDay);
            model.addAttribute("day", targetDay);
        } else if ("month".equals(mode)) {
            int m = (month != null) ? month : now.getMonthValue();
            int y = (year != null) ? year : now.getYear();
            report = reportService.getRevenueReportByMonth(y, m);
            model.addAttribute("month", m);
            model.addAttribute("year", y);
        } else if ("range".equals(mode)) {
            java.time.LocalDate start = (startDate != null && !startDate.isEmpty())
                    ? java.time.LocalDate.parse(startDate)
                    : now.withDayOfMonth(1);
            java.time.LocalDate end = (endDate != null && !endDate.isEmpty()) ? java.time.LocalDate.parse(endDate)
                    : now;
            report = reportService.getOrderStatistics(start, end);
            model.addAttribute("startDate", start);
            model.addAttribute("endDate", end);
        } else {
            return "redirect:/admin/revenue-report?mode=day";
        }
        model.addAttribute("mode", mode);
        model.addAttribute("report", report);
        model.addAttribute("adminUsername", adminUsername);
        return "admin/revenue-report";
    }

    @GetMapping("/best-selling")
    public String bestSelling(@RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
            HttpSession session, Model model) {
        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || !accountService.isAdmin(adminUsername)) {
            return "redirect:/login";
        }

        var items = reportService.getBestSellingItems(limit);
        model.addAttribute("items", items);
        model.addAttribute("limit", limit);
        model.addAttribute("adminUsername", adminUsername);
        return "admin/best-selling";
    }
}
