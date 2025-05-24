package com.nqbm.services;

import com.nqbm.pojo.User;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends UserDetailsService {
    User getUserById(Long id);
    User getUserByUsername(String username);
    List<User> getUsers(String username);
    boolean addUser(User user);
    boolean updateUser(User user);
    boolean deleteUser(Long id);
    
    
    User addUser(Map<String, String> params, MultipartFile avatar);
    boolean authenticate(String username, String password);
}