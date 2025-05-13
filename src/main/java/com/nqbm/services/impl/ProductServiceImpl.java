/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services.impl;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Product;
import com.nqbm.repositories.ProductRepository;
import com.nqbm.services.ProductService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    
    @Override
    public List<Product> getProducts(Map<String, String> params) {
        return this.productRepository.getProducts(params);
    }
    
    @Override
    public Product getProductById(Long id) {
        return this.productRepository.getProductById(id);
    }
    
    @Override
    public boolean addProduct(Product product) {
        return this.productRepository.addProduct(product);
    }
    
    @Override
    public boolean updateProduct(Product product) {
        return this.productRepository.updateProduct(product);
    }
    
    @Override
    public boolean deleteProduct(Long id) {
        return this.productRepository.deleteProduct(id);
    }
    
    @Override
    public Long countProducts() {
        return this.productRepository.countProducts();
    }
    
    @Override
    public List<Product> getProductsBySupplier(Long supplierId) {
        return this.productRepository.getProductsBySupplier(supplierId);
    }
}
