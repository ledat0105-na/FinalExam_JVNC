package com.example.finalexam_jvnc.controller;

import com.example.finalexam_jvnc.dto.*;
import com.example.finalexam_jvnc.model.Refund;
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
        
        model.addAttribute("adminUsername", adminUsername);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("pendingApprovals", pendingRefunds);
        model.addAttribute("todayAccess", todayAccess);
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
            return itemDTO.getItemId() == null ? "redirect:/admin/items/new" : "redirect:/admin/items/" + itemDTO.getItemId() + "/edit";
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
            return warehouseDTO.getWarehouseId() == null ? "redirect:/admin/warehouses/new" : "redirect:/admin/warehouses/" + warehouseDTO.getWarehouseId() + "/edit";
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
}

