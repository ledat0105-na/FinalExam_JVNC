package com.example.finalexam_jvnc.controller;

import com.example.finalexam_jvnc.service.AccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private com.example.finalexam_jvnc.service.ItemService itemService;

    @Autowired
    private com.example.finalexam_jvnc.service.CategoryService categoryService;

    @Autowired
    private com.example.finalexam_jvnc.service.CartService cartService;

    @Autowired
    private com.example.finalexam_jvnc.service.OrderService orderService;

    // Customer Dashboard - yêu cầu đăng nhập
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }
        model.addAttribute("customerUsername", customerUsername);
        return "customer/dashboard-customer";
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        com.example.finalexam_jvnc.dto.AccountProfileDTO profileDTO = accountService.getProfile(customerUsername);
        model.addAttribute("profile", profileDTO);
        model.addAttribute("customerUsername", customerUsername);

        return "customer/profile";
    }

    @org.springframework.web.bind.annotation.PostMapping("/profile")
    public String updateProfile(HttpSession session,
            @org.springframework.web.bind.annotation.ModelAttribute("profile") com.example.finalexam_jvnc.dto.AccountProfileDTO profileDTO,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        try {
            accountService.updateProfile(customerUsername, profileDTO);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }

        return "redirect:/customer/profile";
    }

    @GetMapping("/change-password")
    public String showChangePasswordPage(HttpSession session, Model model) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }
        model.addAttribute("changePasswordDTO", new com.example.finalexam_jvnc.dto.ChangePasswordDTO());
        return "customer/change_password";
    }

    @org.springframework.web.bind.annotation.PostMapping("/change-password")
    public String changePassword(HttpSession session,
            @org.springframework.web.bind.annotation.ModelAttribute("changePasswordDTO") com.example.finalexam_jvnc.dto.ChangePasswordDTO changePasswordDTO,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        // Validate basic rules
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match");
            return "redirect:/customer/change-password";
        }

        if (changePasswordDTO.getNewPassword().length() < 6) {
            redirectAttributes.addFlashAttribute("error", "New password must be at least 6 characters");
            return "redirect:/customer/change-password";
        }

        try {
            accountService.changePassword(customerUsername, changePasswordDTO.getCurrentPassword(),
                    changePasswordDTO.getNewPassword());
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to change password");
        }

        return "redirect:/customer/change-password";
    }

    @GetMapping("/products")
    public String showProducts(HttpSession session, Model model,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String keyword,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long categoryId) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        java.util.List<com.example.finalexam_jvnc.dto.ItemDTO> items;
        if ((keyword != null && !keyword.isEmpty()) || categoryId != null) {
            items = itemService.searchItems(keyword, categoryId);
        } else {
            items = itemService.getActiveItems();
        }

        java.util.List<com.example.finalexam_jvnc.dto.CategoryDTO> categories = categoryService.getActiveCategories();

        model.addAttribute("items", items);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("customerUsername", customerUsername);

        return "customer/products";
    }

    @org.springframework.web.bind.annotation.PostMapping("/cart/add")
    public String addToCart(HttpSession session,
            @org.springframework.web.bind.annotation.RequestParam Long itemId,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") Integer quantity,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        try {
            com.example.finalexam_jvnc.dto.AddToCartDTO addToCartDTO = new com.example.finalexam_jvnc.dto.AddToCartDTO(
                    itemId, quantity);
            cartService.addToCart(customerUsername, addToCartDTO);
            redirectAttributes.addFlashAttribute("success", "Added to cart successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to add to cart: " + e.getMessage());
        }

        return "redirect:/customer/products";
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        com.example.finalexam_jvnc.dto.CartDTO cart = cartService.getCart(customerUsername);
        model.addAttribute("cart", cart);

        // Fetch profile for shipping info
        com.example.finalexam_jvnc.dto.AccountProfileDTO profile = accountService.getProfile(customerUsername);
        model.addAttribute("profile", profile);

        model.addAttribute("customerUsername", customerUsername);

        return "customer/cart";
    }

    @org.springframework.web.bind.annotation.PostMapping("/cart/update")
    public String updateCartItem(HttpSession session,
            @org.springframework.web.bind.annotation.RequestParam Long cartItemId,
            @org.springframework.web.bind.annotation.RequestParam Integer quantity,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        try {
            cartService.updateCartItemQuantity(customerUsername, cartItemId, quantity);
            redirectAttributes.addFlashAttribute("success", "Cart updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update cart: " + e.getMessage());
        }

        return "redirect:/customer/cart";
    }

    @org.springframework.web.bind.annotation.PostMapping("/cart/remove")
    public String removeCartItem(HttpSession session,
            @org.springframework.web.bind.annotation.RequestParam Long cartItemId,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        try {
            cartService.removeFromCart(customerUsername, cartItemId);
            redirectAttributes.addFlashAttribute("success", "Item removed from cart!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove item: " + e.getMessage());
        }

        return "redirect:/customer/cart";
    }

    @org.springframework.web.bind.annotation.PostMapping("/cart/checkout")
    public String checkout(HttpSession session,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "COD") String paymentMethod,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        try {
            com.example.finalexam_jvnc.model.Order order = orderService.createOrderFromCart(customerUsername,
                    paymentMethod);
            redirectAttributes.addFlashAttribute("success",
                    "Order placed successfully! Order #" + order.getOrderNumber());
            // Redirect to order details or dashboard? Let's go to dashboard for now or a
            // generic success page
            return "redirect:/customer/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to place order: " + e.getMessage());
            return "redirect:/customer/cart";
        }
    }

    @org.springframework.web.bind.annotation.PostMapping("/cart/apply-coupon")
    public String applyCoupon(HttpSession session,
            @org.springframework.web.bind.annotation.RequestParam String code,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        try {
            cartService.applyCoupon(customerUsername, code);
            redirectAttributes.addFlashAttribute("success", "Coupon applied successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to apply coupon: " + e.getMessage());
        }

        return "redirect:/customer/cart";
    }

    @org.springframework.web.bind.annotation.PostMapping("/cart/remove-coupon")
    public String removeCoupon(HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        try {
            cartService.removeCoupon(customerUsername);
            redirectAttributes.addFlashAttribute("success", "Coupon removed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove coupon: " + e.getMessage());
        }

        return "redirect:/customer/cart";
    }

    @GetMapping("/orders")
    public String showOrders(HttpSession session, Model model) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        java.util.List<com.example.finalexam_jvnc.dto.OrderDTO> orders = orderService
                .getOrdersByCustomer(customerUsername);
        model.addAttribute("orders", orders);
        model.addAttribute("customerUsername", customerUsername);

        return "customer/orders";
    }

    @GetMapping("/orders/{id}")
    public String viewOrderDetails(HttpSession session, @org.springframework.web.bind.annotation.PathVariable Long id,
            Model model) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        try {
            com.example.finalexam_jvnc.dto.OrderDTO order = orderService.getOrderById(id);
            // Basic security check: ensure the order belongs to this customer
            if (!order.getCustomerUsername().equals(customerUsername)) {
                return "redirect:/error/403";
            }

            model.addAttribute("order", order);
            model.addAttribute("customerUsername", customerUsername);
            return "customer/order_details";
        } catch (Exception e) {
            return "redirect:/customer/orders";
        }
    }

    @org.springframework.web.bind.annotation.PostMapping("/orders/{id}/cancel")
    public String cancelOrder(HttpSession session, @org.springframework.web.bind.annotation.PathVariable Long id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        try {
            orderService.cancelOrder(id, customerUsername);
            redirectAttributes.addFlashAttribute("success", "Order cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to cancel order: " + e.getMessage());
        }

        return "redirect:/customer/orders/" + id;
    }

    @Autowired
    private com.example.finalexam_jvnc.service.WalletService walletService;

    @GetMapping("/wallet")
    public String showWallet(HttpSession session, Model model) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        Double balance = walletService.getBalance(customerUsername);
        model.addAttribute("balance", balance);
        model.addAttribute("customerUsername", customerUsername);

        return "customer/wallet";
    }

    @org.springframework.web.bind.annotation.PostMapping("/wallet/deposit")
    public String deposit(HttpSession session,
            @org.springframework.web.bind.annotation.RequestParam Double amount,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        try {
            walletService.deposit(customerUsername, amount);
            redirectAttributes.addFlashAttribute("success", "Deposit successful!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to deposit: " + e.getMessage());
        }

        return "redirect:/customer/wallet";
    }

    @org.springframework.web.bind.annotation.PostMapping("/orders/{id}/refund")
    public String requestRefund(HttpSession session, @org.springframework.web.bind.annotation.PathVariable Long id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        try {
            orderService.requestRefund(id, customerUsername);
            redirectAttributes.addFlashAttribute("success", "Refund request submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to request refund: " + e.getMessage());
        }

        return "redirect:/customer/orders/" + id;
    }
}
