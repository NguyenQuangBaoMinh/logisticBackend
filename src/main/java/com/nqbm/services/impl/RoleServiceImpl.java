/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services.impl;

/**
 *
 * @author baominh14022004gmail.com
 */


import com.nqbm.pojo.Role;
import com.nqbm.repositories.RoleRepository;
import com.nqbm.services.RoleService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Role getRoleById(Long id) {
        return this.roleRepository.getRoleById(id);
    }

    @Override
    public Role getRoleByName(String name) {
        return this.roleRepository.getRoleByName(name);
    }

    @Override
    public List<Role> getRoles() {
        return this.roleRepository.getRoles();
    }

    @Override
    public boolean addRole(Role role) {
        return this.roleRepository.addRole(role);
    }

    @Override
    public boolean updateRole(Role role) {
        return this.roleRepository.updateRole(role);
    }

    @Override
    public boolean deleteRole(Long id) {
        return this.roleRepository.deleteRole(id);
    }
}
