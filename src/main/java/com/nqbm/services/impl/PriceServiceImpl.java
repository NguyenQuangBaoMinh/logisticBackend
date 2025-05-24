/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services.impl;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Price;
import com.nqbm.repositories.PriceRepository;
import com.nqbm.services.PriceService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PriceServiceImpl implements PriceService {
    @Autowired
    private PriceRepository priceRepository;
    
    @Override
    public List<Price> getPrices(Map<String, String> params) {
        return this.priceRepository.getPrices(params);
    }
    
    @Override
    public Price getPriceById(Long id) {
        return this.priceRepository.getPriceById(id);
    }
    
    @Override
    public boolean addPrice(Price price) {
        return this.priceRepository.addPrice(price);
    }
    
    @Override
    public boolean updatePrice(Price price) {
        return this.priceRepository.updatePrice(price);
    }
    
    @Override
    public boolean deletePrice(Long id) {
        return this.priceRepository.deletePrice(id);
    }
    
    @Override
    public Long countPrices() {
        return this.priceRepository.countPrices();
    }
    
    @Override
    public List<Price> getPricesBySupplier(Long supplierId) {
        return this.priceRepository.getPricesBySupplier(supplierId);
    }
    
    @Override
    public List<Price> getPricesByProduct(Long productId) {
        return this.priceRepository.getPricesByProduct(productId);
    }
    
    @Override
    public Price getCurrentPriceBySupplierAndProduct(Long supplierId, Long productId) {
        return this.priceRepository.getCurrentPriceBySupplierAndProduct(supplierId, productId);
    }
}
