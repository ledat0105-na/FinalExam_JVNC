package com.example.finalexam_jvnc.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;


@Component
@Order(1)
public class SessionValidationFilter implements Filter {
    

    private static final String SERVER_INSTANCE_ID = UUID.randomUUID().toString();
    private static final String SESSION_SERVER_ID_KEY = "serverInstanceId";
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();
        
        if (requestURI.equals("/login") || requestURI.equals("/register") || 
            requestURI.startsWith("/css/") || requestURI.startsWith("/js/") || 
            requestURI.startsWith("/uploads/") || requestURI.startsWith("/images/") ||
            requestURI.equals("/logout")) {
            chain.doFilter(request, response);
            return;
        }
        
        HttpSession session = httpRequest.getSession(false);

        if (session != null) {
            String sessionServerId = (String) session.getAttribute(SESSION_SERVER_ID_KEY);
            
            if (sessionServerId == null || !sessionServerId.equals(SERVER_INSTANCE_ID)) {
                String adminUsername = (String) session.getAttribute("adminUsername");
                String staffUsername = (String) session.getAttribute("staffUsername");
                String customerUsername = (String) session.getAttribute("customerUsername");
                
                if (adminUsername != null || staffUsername != null || customerUsername != null) {
                    session.setAttribute(SESSION_SERVER_ID_KEY, SERVER_INSTANCE_ID);
                } else {
                    session.invalidate();
                    if (requestURI.startsWith("/admin/") || 
                        requestURI.startsWith("/staff/") || 
                        requestURI.startsWith("/customer/")) {
                        httpResponse.sendRedirect("/login");
                        return;
                    }
                }
            } else {
                session.setAttribute(SESSION_SERVER_ID_KEY, SERVER_INSTANCE_ID);
            }
        } else {
            if (requestURI.startsWith("/admin/") || 
                requestURI.startsWith("/staff/") || 
                requestURI.startsWith("/customer/")) {
                httpResponse.sendRedirect("/login");
                return;
            }
        }
        
        chain.doFilter(request, response);
        
        HttpSession sessionAfter = httpRequest.getSession(false);
        if (sessionAfter != null && sessionAfter.getAttribute(SESSION_SERVER_ID_KEY) == null) {
            sessionAfter.setAttribute(SESSION_SERVER_ID_KEY, SERVER_INSTANCE_ID);
        }
    }
    
    @Override
    public void destroy() {
    }
}

