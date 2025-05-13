/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services.impl;

/**
 *
 * @author baominh14022004gmail.com
 */
import com.nqbm.pojo.Supplier;
import com.nqbm.repositories.SupplierRepository;
import com.nqbm.services.SupplierService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SupplierServiceImpl implements SupplierService {
    @Autowired
    private SupplierRepository supplierRepository;
    
    @Override
    public List<Supplier> getSuppliers(Map<String, String> params) {
        return this.supplierRepository.getSuppliers(params);
    }
    
    @Override
    public Supplier getSupplierById(Long id) {
        return this.supplierRepository.getSupplierById(id);
    }
    
    @Override
    public boolean addSupplier(Supplier supplier) {
        return this.supplierRepository.addSupplier(supplier);
    }
    
    @Override
    public boolean updateSupplier(Supplier supplier) {
        return this.supplierRepository.updateSupplier(supplier);
    }
    
    @Override
    public boolean deleteSupplier(Long id) {
        return this.supplierRepository.deleteSupplier(id);
    }
    
    @Override
    public Long countSuppliers() {
        return this.supplierRepository.countSuppliers();
    }
}
