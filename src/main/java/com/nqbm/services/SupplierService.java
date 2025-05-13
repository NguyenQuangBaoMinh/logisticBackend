/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Supplier;
import java.util.List;
import java.util.Map;

public interface SupplierService {
    List<Supplier> getSuppliers(Map<String, String> params);
    Supplier getSupplierById(Long id);
    boolean addSupplier(Supplier supplier);
    boolean updateSupplier(Supplier supplier);
    boolean deleteSupplier(Long id);
    Long countSuppliers();
}
