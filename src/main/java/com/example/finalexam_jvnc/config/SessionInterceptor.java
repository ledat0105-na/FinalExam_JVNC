package com.example.finalexam_jvnc.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Bỏ qua các route public và static resources
        String path = request.getRequestURI();
        if (path.equals("/") || 
            path.equals("/dashboard") || 
            path.equals("/login") || 
            path.equals("/register") ||
            path.equals("/logout") ||
            path.startsWith("/css/") ||
            path.startsWith("/js/") ||
            path.startsWith("/images/") ||
            path.startsWith("/static/") ||
            path.startsWith("/error")) {
            return true;
        }

        // Kiểm tra session cho các route protected (admin, staff, customer)
        HttpSession session = request.getSession(false);
        
        // Nếu không có session hoặc session không có user nào đăng nhập
        if (session == null) {
            // Invalidate session nếu có và redirect về trang chủ
            response.sendRedirect("/dashboard");
            return false;
        }
        
        // Kiểm tra xem có user nào đăng nhập không
        boolean hasUser = session.getAttribute("adminUsername") != null || 
                         session.getAttribute("staffUsername") != null || 
                         session.getAttribute("customerUsername") != null;
        
        if (!hasUser) {
            // Session tồn tại nhưng không có user, invalidate và redirect
            session.invalidate();
            response.sendRedirect("/dashboard");
            return false;
        }

        return true;
    }
}

