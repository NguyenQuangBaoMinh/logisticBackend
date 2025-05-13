/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.controllers;

/**
 *
 * @author baominh14022004gmail.com
 */
import com.nqbm.pojo.Role;
import com.nqbm.pojo.User;
import com.nqbm.services.RoleService;
import com.nqbm.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage(@RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "logout", required = false) String logout,
            Model model) {
        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng!");
        }
        if (logout != null) {
            model.addAttribute("message", "Đăng xuất thành công!");
        }
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping({"/", "/home"})
    public String home() {
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @PostMapping("/register")
    public String register(@RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("displayName") String displayName,
            @RequestParam("email") String email,
            @RequestParam(value = "phone", required = false) String phone) {
        try {
            // Kiểm tra username đã tồn tại chưa
            User existingUser = userService.getUserByUsername(username);
            if (existingUser != null) {
                return "redirect:/login?register_error=true";
            }

            // Tạo người dùng mới
            User user = new User();
            user.setUsername(username);
            user.setPassword(password); // Sẽ được mã hóa trong service
            user.setDisplayName(displayName);
            user.setEmail(email);
            user.setPhone(phone);
            user.setActive(true);

            // Mặc định gán vai trò USER
            Role userRole = roleService.getRoleByName("USER");
            if (userRole == null) {
                // Nếu role USER chưa có, tạo mới
                userRole = new Role();
                userRole.setName("USER");
                userRole.setDescription("Người dùng thông thường");
                roleService.addRole(userRole);
            }
            user.addRole(userRole);

            // Lưu người dùng
            boolean success = userService.addUser(user);

            if (success) {
                return "redirect:/login?register_success=true";
            } else {
                return "redirect:/login?register_error=true";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login?register_error=true";
        }
    }
}
