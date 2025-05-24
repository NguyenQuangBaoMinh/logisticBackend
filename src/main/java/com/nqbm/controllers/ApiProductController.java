/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.controllers;

/**
 *
 * @author baominh14022004gmail.com
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.nqbm.pojo.Product;
import com.nqbm.pojo.Supplier;
import com.nqbm.pojo.User;
import com.nqbm.services.ProductService;
import com.nqbm.services.SupplierService;
import com.nqbm.services.UserService;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author baominh14022004gmail.com
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ApiProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private SupplierService supplierService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Lấy danh sách sản phẩm với phân trang và lọc
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getProducts(@RequestParam Map<String, String> params) {
        try {
            System.out.println("=== GET PRODUCTS ===");
            System.out.println("Parameters: " + params);
            
            List<Product> products = productService.getProducts(params);
            Long totalCount = productService.countProducts();
            
            System.out.println("Found " + products.size() + " products");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("products", products);
            result.put("totalCount", totalCount);
            result.put("currentPage", params.get("page") != null ? Integer.parseInt(params.get("page")) : 1);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi lấy danh sách sản phẩm: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy thông tin sản phẩm theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            System.out.println("=== GET PRODUCT BY ID ===");
            System.out.println("Product ID: " + id);
            
            Product product = productService.getProductById(id);
            if (product == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(Map.of("success", true, "product", product));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi lấy thông tin sản phẩm: " + e.getMessage()));
        }
    }
    
    /**
     * Tạo sản phẩm mới
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@RequestBody Map<String, Object> productData, Principal principal) {
        try {
            System.out.println("=== CREATE PRODUCT ===");
            System.out.println("Product Data: " + productData);
            System.out.println("Principal: " + (principal != null ? principal.getName() : "null"));
            
            // Tạo object Product từ dữ liệu request
            Product product = new Product();
            
            // Set thông tin cơ bản
            product.setName((String) productData.get("name"));
            product.setSku((String) productData.get("sku"));
            product.setDescription((String) productData.get("description"));
            
            // Set giá
            if (productData.get("unitPrice") != null) {
                product.setUnitPrice(new java.math.BigDecimal(productData.get("unitPrice").toString()));
            }
            
            // Set số lượng trong kho
            if (productData.get("unitInStock") != null) {
                product.setUnitInStock(Integer.valueOf(productData.get("unitInStock").toString()));
            }
            
            // Set reorder level
            if (productData.get("reorderLevel") != null) {
                product.setReorderLevel(Integer.valueOf(productData.get("reorderLevel").toString()));
            }
            
            // Set active status
            if (productData.get("active") != null) {
                product.setActive(Boolean.valueOf(productData.get("active").toString()));
            }
            
            // Set supplier nếu có
            if (productData.get("supplierId") != null) {
                Long supplierId = Long.valueOf(productData.get("supplierId").toString());
                Supplier supplier = supplierService.getSupplierById(supplierId);
                if (supplier != null) {
                    product.setSupplier(supplier);
                }
            }
            
            // Set người tạo
            if (principal != null) {
                User currentUser = userService.getUserByUsername(principal.getName());
                if (currentUser != null) {
                    product.setCreatedBy(currentUser);
                }
            }
            
            // Tạo sản phẩm
            boolean created = productService.addProduct(product);
            
            if (created) {
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "Tạo sản phẩm thành công", "product", product));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Không thể tạo sản phẩm"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi tạo sản phẩm: " + e.getMessage()));
        }
    }
    
    /**
     * Cập nhật sản phẩm
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> productData) {
        try {
            System.out.println("=== UPDATE PRODUCT ===");
            System.out.println("Product ID: " + id);
            System.out.println("Update Data: " + productData);
            
            Product existingProduct = productService.getProductById(id);
            if (existingProduct == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Cập nhật thông tin cơ bản
            if (productData.get("name") != null) {
                existingProduct.setName((String) productData.get("name"));
            }
            if (productData.get("sku") != null) {
                existingProduct.setSku((String) productData.get("sku"));
            }
            if (productData.get("description") != null) {
                existingProduct.setDescription((String) productData.get("description"));
            }
            
            // Cập nhật giá
            if (productData.get("unitPrice") != null) {
                existingProduct.setUnitPrice(new java.math.BigDecimal(productData.get("unitPrice").toString()));
            }
            
            // Cập nhật số lượng trong kho
            if (productData.get("unitInStock") != null) {
                existingProduct.setUnitInStock(Integer.valueOf(productData.get("unitInStock").toString()));
            }
            
            // Cập nhật reorder level
            if (productData.get("reorderLevel") != null) {
                existingProduct.setReorderLevel(Integer.valueOf(productData.get("reorderLevel").toString()));
            }
            
            // Cập nhật active status
            if (productData.get("active") != null) {
                existingProduct.setActive(Boolean.valueOf(productData.get("active").toString()));
            }
            
            // Cập nhật supplier nếu có
            if (productData.get("supplierId") != null) {
                Long supplierId = Long.valueOf(productData.get("supplierId").toString());
                Supplier supplier = supplierService.getSupplierById(supplierId);
                if (supplier != null) {
                    existingProduct.setSupplier(supplier);
                }
            }
            
            // Cập nhật sản phẩm
            boolean updated = productService.updateProduct(existingProduct);
            
            if (updated) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật sản phẩm thành công", "product", existingProduct));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Không thể cập nhật sản phẩm"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi cập nhật sản phẩm: " + e.getMessage()));
        }
    }
    
    /**
     * Xóa sản phẩm
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            System.out.println("=== DELETE PRODUCT ===");
            System.out.println("Product ID: " + id);
            
            Product existingProduct = productService.getProductById(id);
            if (existingProduct == null) {
                return ResponseEntity.notFound().build();
            }
            
            boolean deleted = productService.deleteProduct(id);
            
            if (deleted) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Xóa sản phẩm thành công"));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Không thể xóa sản phẩm"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi xóa sản phẩm: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy sản phẩm theo nhà cung cấp
     */
    @GetMapping("/by-supplier/{supplierId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getProductsBySupplier(@PathVariable Long supplierId) {
        try {
            System.out.println("=== GET PRODUCTS BY SUPPLIER ===");
            System.out.println("Supplier ID: " + supplierId);
            
            List<Product> products = productService.getProductsBySupplier(supplierId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "products", products,
                "totalCount", products.size()
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi lấy sản phẩm theo nhà cung cấp: " + e.getMessage()));
        }
    }
    
    /**
     * Tìm kiếm sản phẩm
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String supplierId,
            @RequestParam(required = false) String active,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        try {
            System.out.println("=== SEARCH PRODUCTS ===");
            System.out.println("Keyword: " + keyword);
            System.out.println("Supplier ID: " + supplierId);
            System.out.println("Active: " + active);
            
            Map<String, String> params = new HashMap<>();
            params.put("page", String.valueOf(page));
            params.put("size", String.valueOf(size));
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                params.put("keyword", keyword);
            }
            if (supplierId != null && !supplierId.trim().isEmpty()) {
                params.put("supplierId", supplierId);
            }
            if (active != null && !active.trim().isEmpty()) {
                params.put("active", active);
            }
            
            List<Product> products = productService.getProducts(params);
            Long totalCount = productService.countProducts();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("products", products);
            result.put("totalCount", totalCount);
            result.put("currentPage", page);
            result.put("totalPages", (totalCount + size - 1) / size);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi tìm kiếm sản phẩm: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy sản phẩm có tồn kho thấp (cần đặt hàng lại)
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getLowStockProducts() {
        try {
            System.out.println("=== GET LOW STOCK PRODUCTS ===");
            
            Map<String, String> params = new HashMap<>();
            params.put("lowStock", "true");
            
            List<Product> products = productService.getProducts(params);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "products", products,
                "totalCount", products.size()
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi lấy sản phẩm tồn kho thấp: " + e.getMessage()));
        }
    }
}