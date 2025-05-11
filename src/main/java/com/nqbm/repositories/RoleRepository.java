/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.repositories;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Role;
import java.util.List;

public interface RoleRepository {
    Role getRoleById(Long id);
    Role getRoleByName(String name);
    List<Role> getRoles();
    boolean addRole(Role role);
    boolean updateRole(Role role);
    boolean deleteRole(Long id);
}
