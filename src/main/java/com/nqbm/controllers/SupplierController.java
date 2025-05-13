/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.controllers;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Supplier;
import com.nqbm.pojo.User;
import com.nqbm.services.SupplierService;
import com.nqbm.services.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/suppliers")
public class SupplierController {
    @Autowired
    private SupplierService supplierService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String suppliers(Model model, 
                          @RequestParam(required = false) Map<String, String> params) {
        List<Supplier> suppliers = this.supplierService.getSuppliers(params);
        Long count = this.supplierService.countSuppliers();
        
        // Tính số trang
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) count / pageSize);
        
        // Lấy trang hiện tại
        int currentPage = 1;
        if (params != null && params.containsKey("page")) {
            try {
                currentPage = Integer.parseInt(params.get("page"));
            } catch (NumberFormatException ex) {
                // Giữ trang 1 nếu có lỗi
            }
        }
        
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("count", count);
        
        // Truyền các tham số tìm kiếm/sắp xếp để giữ nguyên khi chuyển trang
        if (params != null) {
            model.addAttribute("name", params.getOrDefault("name", ""));
            model.addAttribute("active", params.getOrDefault("active", ""));
            model.addAttribute("sort", params.getOrDefault("sort", ""));
        }
        
        return "suppliers";
    }
    
    @GetMapping("/add")
    public String addSupplierForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        model.addAttribute("action", "add");
        return "supplier-form";
    }
    
    @GetMapping("/edit/{id}")
    public String editSupplierForm(@PathVariable("id") Long id, Model model) {
        Supplier supplier = this.supplierService.getSupplierById(id);
        
        if (supplier == null) {
            return "redirect:/suppliers";
        }
        
        model.addAttribute("supplier", supplier);
        model.addAttribute("action", "edit");
        return "supplier-form";
    }
    
    @PostMapping("/save")
    public String saveSupplier(@ModelAttribute("supplier") Supplier supplier,
                             @RequestParam("action") String action,
                             RedirectAttributes redirectAttributes) {
        boolean success;
        
        // Lấy thông tin người dùng hiện tại
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByUsername(auth.getName());
        
        if ("add".equals(action)) {
            // Thiết lập người tạo khi thêm mới
            supplier.setCreatedBy(currentUser);
            success = this.supplierService.addSupplier(supplier);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Thêm nhà cung cấp thành công");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Thêm nhà cung cấp thất bại");
            }
        } else {
            // Lấy thông tin cũ của supplier
            Supplier existingSupplier = this.supplierService.getSupplierById(supplier.getId());
            if (existingSupplier != null) {
                // Giữ nguyên người tạo ban đầu
                supplier.setCreatedBy(existingSupplier.getCreatedBy());
                supplier.setCreatedDate(existingSupplier.getCreatedDate());
            }
            
            success = this.supplierService.updateSupplier(supplier);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nhà cung cấp thành công");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật nhà cung cấp thất bại");
            }
        }
        
        return "redirect:/suppliers";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable("id") Long id, 
                               RedirectAttributes redirectAttributes) {
        boolean success = this.supplierService.deleteSupplier(id);
        
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Xóa nhà cung cấp thành công");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Xóa nhà cung cấp thất bại");
        }
        
        return "redirect:/suppliers";
    }
    
    @GetMapping("/view/{id}")
    public String viewSupplier(@PathVariable("id") Long id, Model model) {
        Supplier supplier = this.supplierService.getSupplierById(id);
        
        if (supplier == null) {
            return "redirect:/suppliers";
        }
        
        model.addAttribute("supplier", supplier);
        return "supplier-details";
    }
}
