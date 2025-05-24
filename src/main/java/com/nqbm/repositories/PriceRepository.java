/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.repositories;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Price;
import java.util.List;
import java.util.Map;

public interface PriceRepository {
    List<Price> getPrices(Map<String, String> params);
    Price getPriceById(Long id);
    boolean addPrice(Price price);
    boolean updatePrice(Price price);
    boolean deletePrice(Long id);
    Long countPrices();
    List<Price> getPricesBySupplier(Long supplierId);
    List<Price> getPricesByProduct(Long productId);
    Price getCurrentPriceBySupplierAndProduct(Long supplierId, Long productId);
}
