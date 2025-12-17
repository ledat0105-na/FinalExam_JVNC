package com.example.finalexam_jvnc.controller;

import com.example.finalexam_jvnc.repository.ItemRepository;
import com.example.finalexam_jvnc.service.AccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @Autowired
    private ItemRepository itemRepository;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }
        
        long totalOrders = 0;
        double totalSpent = 0.0;
        int cartItemCount = 0;
        double walletBalance = 0.0;
        long pendingOrders = 0;
        long completedOrders = 0;
        
        try {
            java.util.List<com.example.finalexam_jvnc.dto.OrderDTO> orders = orderService.getOrdersByCustomer(customerUsername);
            totalOrders = orders != null ? orders.size() : 0;
            
            if (orders != null) {
                totalSpent = orders.stream()
                    .filter(o -> "DONE".equals(o.getStatus()))
                    .mapToDouble(o -> o.getGrandTotal() != null ? o.getGrandTotal() : 0.0)
                    .sum();
                
                pendingOrders = orders.stream()
                    .filter(o -> "PENDING_CONFIRMATION".equals(o.getStatus()) || 
                                "PROCESSING".equals(o.getStatus()) || 
                                "PACKED".equals(o.getStatus()) || 
                                "SHIPPED".equals(o.getStatus()))
                    .count();
                
                completedOrders = orders.stream()
                    .filter(o -> "DONE".equals(o.getStatus()))
                    .count();
            }
        } catch (Exception e) {
            totalOrders = 0;
            totalSpent = 0.0;
        }
        
        try {
            cartItemCount = cartService.getCartItemCount(customerUsername);
        } catch (Exception e) {
            cartItemCount = 0;
        }
        
        try {
            walletBalance = walletService.getBalance(customerUsername);
        } catch (Exception e) {
            walletBalance = 0.0;
        }
        
        model.addAttribute("customerUsername", customerUsername);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("walletBalance", walletBalance);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("completedOrders", completedOrders);
        
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

    @GetMapping("/products/{id}")
    public String viewProductDetail(@PathVariable Long id, HttpSession session, Model model) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        try {
            com.example.finalexam_jvnc.dto.ItemDTO item = itemService.getItemById(id);
            if (item == null || (item.getIsActive() != null && !item.getIsActive())) {
                return "redirect:/customer/products";
            }
            model.addAttribute("item", item);
            model.addAttribute("customerUsername", customerUsername);
            return "customer/product-detail";
        } catch (Exception e) {
            return "redirect:/customer/products";
        }
    }

    @GetMapping("/items/{id}/image")
    @ResponseBody
    public ResponseEntity<byte[]> getItemImage(@PathVariable Long id) {
        try {
            com.example.finalexam_jvnc.model.Item item = itemRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Item not found"));
            
            if (item.getImageData() != null && item.getImageData().length > 0) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(
                    item.getImageContentType() != null ? item.getImageContentType() : "image/jpeg"));
                headers.setContentLength(item.getImageData().length);
                return new ResponseEntity<>(item.getImageData(), headers, org.springframework.http.HttpStatus.OK);
            } else {
                return new ResponseEntity<>(org.springframework.http.HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(org.springframework.http.HttpStatus.NOT_FOUND);
        }
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

    @Autowired
    private com.example.finalexam_jvnc.service.PaymentService paymentService;

    @GetMapping("/wallet")
    public String showWallet(HttpSession session, Model model) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        Double balance = walletService.getBalance(customerUsername);
        java.util.List<com.example.finalexam_jvnc.dto.WalletTransactionDTO> transactions = 
                walletService.getTransactionHistory(customerUsername);
        
        model.addAttribute("balance", balance);
        model.addAttribute("transactions", transactions);
        model.addAttribute("customerUsername", customerUsername);

        return "customer/wallet";
    }

    @org.springframework.web.bind.annotation.GetMapping("/wallet/deposit")
    public String showDepositOptions(HttpSession session, Model model) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        Double balance = walletService.getBalance(customerUsername);
        model.addAttribute("balance", balance);
        model.addAttribute("customerUsername", customerUsername);
        model.addAttribute("bankAccountName", paymentService.getBankAccountName());
        model.addAttribute("bankAccountNumber", paymentService.getBankAccountNumber());
        model.addAttribute("bankName", paymentService.getBankName());
        model.addAttribute("momoAccountName", paymentService.getMoMoAccountName());
        model.addAttribute("momoPhoneNumber", paymentService.getMoMoPhoneNumber());
        
        return "customer/wallet-deposit";
    }

    @org.springframework.web.bind.annotation.PostMapping("/wallet/deposit/momo")
    public String depositViaMoMo(HttpSession session,
            @org.springframework.web.bind.annotation.RequestParam Double amount,
            jakarta.servlet.http.HttpServletRequest request,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        try {
            if (amount < 10000) {
                redirectAttributes.addFlashAttribute("error", "Số tiền nạp tối thiểu là 10.000 đ");
                return "redirect:/customer/wallet/deposit";
            }

            String orderInfo = "Nap tien vao vi - " + customerUsername;
            String returnUrl = request.getRequestURL().toString().replace("/deposit/momo", "/wallet/momo/callback");
            String paymentUrl = paymentService.createMoMoPaymentUrl(amount, orderInfo, returnUrl);
            
            session.setAttribute("pendingDepositAmount", amount);
            session.setAttribute("pendingDepositUsername", customerUsername);
            
            return "redirect:" + paymentUrl;
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.length() > 150) {
                errorMessage = errorMessage.substring(0, 147) + "...";
            }
            redirectAttributes.addFlashAttribute("error", 
                errorMessage != null && errorMessage.contains("MoMo") 
                    ? errorMessage 
                    : "Lỗi khi tạo thanh toán MoMo. Vui lòng thử lại sau hoặc sử dụng phương thức thanh toán khác.");
            return "redirect:/customer/wallet/deposit";
        }
    }

    @org.springframework.web.bind.annotation.GetMapping("/wallet/momo/callback")
    public String momoCallback(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String resultCode,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String amount,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String orderId,
            HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        Double pendingAmount = (Double) session.getAttribute("pendingDepositAmount");
        
        if (customerUsername == null || pendingAmount == null) {
            redirectAttributes.addFlashAttribute("error", "Phiên làm việc đã hết hạn");
            return "redirect:/customer/wallet";
        }

        try {
            if (paymentService.verifyMoMoCallback(resultCode, amount, orderId)) {
                walletService.deposit(customerUsername, pendingAmount);
                session.removeAttribute("pendingDepositAmount");
                session.removeAttribute("pendingDepositUsername");
                redirectAttributes.addFlashAttribute("success", 
                    "Nạp tiền thành công! Đã nạp " + String.format("%,.0f", pendingAmount) + " đ vào ví");
            } else {
                redirectAttributes.addFlashAttribute("error", "Thanh toán thất bại hoặc đã bị hủy");
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.length() > 150) {
                errorMessage = errorMessage.substring(0, 147) + "...";
            }
            redirectAttributes.addFlashAttribute("error", 
                errorMessage != null && errorMessage.contains("MoMo") 
                    ? errorMessage 
                    : "Lỗi khi xử lý thanh toán. Vui lòng thử lại sau hoặc sử dụng phương thức thanh toán khác.");
        }

        return "redirect:/customer/wallet";
    }

    @org.springframework.web.bind.annotation.PostMapping("/wallet/deposit/qr")
    public String showQRCode(HttpSession session,
            @org.springframework.web.bind.annotation.RequestParam Double amount,
            Model model) {
        String customerUsername = (String) session.getAttribute("customerUsername");
        if (customerUsername == null) {
            return "redirect:/login";
        }

        if (amount < 10000) {
            model.addAttribute("error", "Số tiền nạp tối thiểu là 10.000 đ");
            return showDepositOptions(session, model);
        }

        String qrData = paymentService.generateQRCodeData(
            amount,
            paymentService.getBankAccountName(),
            paymentService.getBankAccountNumber(),
            paymentService.getBankName()
        );

        model.addAttribute("amount", amount);
        model.addAttribute("qrData", qrData);
        model.addAttribute("bankAccountName", paymentService.getBankAccountName());
        model.addAttribute("bankAccountNumber", paymentService.getBankAccountNumber());
        model.addAttribute("bankName", paymentService.getBankName());
        model.addAttribute("customerUsername", customerUsername);
        
        return "customer/wallet-qr";
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
