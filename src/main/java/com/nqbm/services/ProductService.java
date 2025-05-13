/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Product;
import java.util.List;
import java.util.Map;

public interface ProductService {
    List<Product> getProducts(Map<String, String> params);
    Product getProductById(Long id);
    boolean addProduct(Product product);
    boolean updateProduct(Product product);
    boolean deleteProduct(Long id);
    Long countProducts();
    List<Product> getProductsBySupplier(Long supplierId);
}
