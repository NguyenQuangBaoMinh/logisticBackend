package com.nqbm.repositories;

import com.nqbm.pojo.User;
import java.util.List;

public interface UserRepository {
    User getUserById(Long id);
    User getUserByUsername(String username);
    List<User> getUsers(String username);
    boolean addUser(User user);
    boolean updateUser(User user);
    boolean deleteUser(Long id);
}