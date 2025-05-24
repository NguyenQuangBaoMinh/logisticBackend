package com.nqbm.controllers;

import com.nqbm.pojo.Role;
import com.nqbm.pojo.User;
import com.nqbm.services.UserService;
import com.nqbm.utils.JwtUtils;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping; 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody; 
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;      

@RestController
@RequestMapping("/api/auth") 
@CrossOrigin
public class ApiUserController {

    @Autowired
    private UserService userDetailsService; 

    @PostMapping(path = "/users",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> create(@RequestParam Map<String, String> params, 
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        
        try {
            System.out.println("📝 Creating new user with params: " + params.keySet());
            User newUser = this.userDetailsService.addUser(params, avatar);
            if (newUser != null) {
                System.out.println("✅ User created successfully: " + newUser.getUsername());
                // Don't return password
                newUser.setPassword(null);
                return new ResponseEntity<>(newUser, HttpStatus.CREATED);
            } else {
                System.out.println("❌ Failed to create user");
                return ResponseEntity.badRequest().body(null); 
            }
        } catch (Exception ex) {
            System.out.println("❌ Exception creating user: " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User u) {
        System.out.println("🔐 Login attempt for username: " + (u != null ? u.getUsername() : "null"));
        
        if (u == null || u.getUsername() == null || u.getPassword() == null || 
            u.getUsername().isEmpty() || u.getPassword().isEmpty()) {
            System.out.println("❌ Login failed: Missing username or password");
            return ResponseEntity.badRequest().body("Tên đăng nhập và mật khẩu không được để trống.");
        }

        if (this.userDetailsService.authenticate(u.getUsername(), u.getPassword())) {
            try {
                System.out.println("✅ Authentication successful for: " + u.getUsername());
                
                // Debug: Get user info to see roles
                User authenticatedUser = this.userDetailsService.getUserByUsername(u.getUsername());
                if (authenticatedUser != null) {
                    System.out.println("👤 User details:");
                    System.out.println("  - Username: " + authenticatedUser.getUsername());
                    System.out.println("  - Display Name: " + authenticatedUser.getDisplayName());
                    System.out.println("  - Email: " + authenticatedUser.getEmail());
                    System.out.println("  - Roles count: " + (authenticatedUser.getRoles() != null ? authenticatedUser.getRoles().size() : 0));
                    
                    if (authenticatedUser.getRoles() != null) {
                        for (Role role : authenticatedUser.getRoles()) {
                            System.out.println("  - Role: " + role.getName());
                        }
                    }
                    
                    System.out.println("  - getUserRole(): " + authenticatedUser.getUserRole());
                }
                
                String token = JwtUtils.generateToken(u.getUsername());
                System.out.println("🎫 JWT token generated successfully");
                return ResponseEntity.ok().body(Collections.singletonMap("token", token));
            } catch (Exception e) {
                System.out.println("❌ Error generating JWT: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi tạo JWT: " + e.getMessage());
            }
        }
        
        System.out.println("❌ Authentication failed for: " + u.getUsername());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai thông tin đăng nhập.");
    }

    @GetMapping("/secure/profile") 
    @ResponseBody 
    @CrossOrigin 
    public ResponseEntity<User> getProfile(Principal principal, HttpServletRequest request) {
        System.out.println("👤 Profile request received");
        System.out.println("🔍 Principal: " + (principal != null ? principal.getName() : "null"));
        System.out.println("🔍 Security Context: " + SecurityContextHolder.getContext().getAuthentication());
        
        // Try to get username from request attribute (set by JWT filter)
        String username = (String) request.getAttribute("username");
        System.out.println("🔍 Username from request attribute: " + username);
        
        // Fallback to principal if available
        if (username == null && principal != null) {
            username = principal.getName();
            System.out.println("🔍 Username from principal: " + username);
        }
        
        if (username == null || username.isEmpty()) {
            System.out.println("❌ No username found in request");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            User user = this.userDetailsService.getUserByUsername(username);
            if (user != null) {
                System.out.println("✅ Profile found for user: " + username);
                
                // Enhanced debugging
                System.out.println("📊 User Profile Debug:");
                System.out.println("  - ID: " + user.getId());
                System.out.println("  - Username: " + user.getUsername());
                System.out.println("  - Display Name: " + user.getDisplayName());
                System.out.println("  - Email: " + user.getEmail());
                System.out.println("  - Phone: " + user.getPhone());
                System.out.println("  - Active: " + user.isActive());
                System.out.println("  - Avatar: " + user.getAvatar());
                System.out.println("  - Roles: " + (user.getRoles() != null ? user.getRoles().size() : 0));
                
                if (user.getRoles() != null) {
                    for (Role role : user.getRoles()) {
                        System.out.println("    - Role: " + role.getName() + " (ID: " + role.getId() + ")");
                    }
                }
                
                System.out.println("  - getUserRole(): " + user.getUserRole());
                System.out.println("  - getRoleNames(): " + user.getRoleNames());
                
                // Don't return password in response
                user.setPassword(null);
                return new ResponseEntity<>(user, HttpStatus.OK);
            } else {
                System.out.println("❌ User not found in database: " + username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            System.out.println("❌ Error getting user profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            System.out.println("🔓 Logout request received");
            SecurityContextHolder.clearContext();

            HttpSession session = request.getSession(false); 
            if (session != null) {
                session.invalidate();
            }

            System.out.println("✅ Logout successful");
            return ResponseEntity.ok().body(Collections.singletonMap("message", "Đăng xuất thành công."));
        } catch (Exception e) {
            System.out.println("❌ Logout error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Lỗi trong quá trình đăng xuất: " + e.getMessage()));
        }
    }
}