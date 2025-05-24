/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.filters;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.utils.JwtUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JwtFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        
        System.out.println(" JWT Filter processing: " + requestURI);
        
        // FIX: Check for both /api/secure and /api/auth/secure paths
        if (requestURI.startsWith(contextPath + "/api/auth/secure") || 
            requestURI.startsWith(contextPath + "/api/secure")) {
            
            System.out.println(" JWT Filter: Processing secure endpoint");
            
            String header = httpRequest.getHeader("Authorization");
            System.out.println(" Authorization header: " + (header != null ? header.substring(0, Math.min(30, header.length())) + "..." : "null"));
            
            if (header == null || !header.startsWith("Bearer ")) {
                System.out.println(" JWT Filter: Missing or invalid Authorization header");
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header.");
                return;
            }
            
            String token = header.substring(7);
            try {
                String username = JwtUtils.validateTokenAndGetUsername(token);
                if (username != null) {
                    System.out.println("JWT Filter: Token valid for user: " + username);
                    
                    // Set username attribute for controller access
                    httpRequest.setAttribute("username", username);
                    
                    // Set Spring Security authentication
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(username, null, null);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // Continue with the request
                    chain.doFilter(request, response);
                    return;
                } else {
                    System.out.println("JWT Filter: Token validation returned null username");
                }
            } catch (Exception e) {
                System.out.println(" JWT Filter: Token validation exception: " + e.getMessage());
                e.printStackTrace();
            }

            // If we reach here, token is invalid
            System.out.println(" JWT Filter: Sending UNAUTHORIZED response");
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, 
                    "Token không hợp lệ hoặc hết hạn");
            return;
        }
        
        // For non-secure endpoints, continue normally
        System.out.println("➡️ JWT Filter: Non-secure endpoint, continuing...");
        chain.doFilter(request, response);
    }
}