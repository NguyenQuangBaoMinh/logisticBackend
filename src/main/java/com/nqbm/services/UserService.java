package com.nqbm.services;

import com.nqbm.pojo.User;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User getUserById(Long id);
    User getUserByUsername(String username);
    List<User> getUsers(String username);
    boolean addUser(User user);
    boolean updateUser(User user);
    boolean deleteUser(Long id);
}