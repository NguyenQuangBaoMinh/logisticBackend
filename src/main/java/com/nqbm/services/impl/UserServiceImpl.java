package com.nqbm.services.impl;

import com.nqbm.pojo.Role;
import com.nqbm.pojo.User;
import com.nqbm.repositories.UserRepository;
import com.nqbm.services.UserService;
import com.nqbm.services.RoleService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service("userDetailsService")
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private Cloudinary cloudinary;

    @Override
    public User getUserById(Long id) {
        return this.userRepository.getUserById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        return this.userRepository.getUserByUsername(username);
    }

    @Override
    public List<User> getUsers(String username) {
        return this.userRepository.getUsers(username);
    }

    @Override
    public boolean addUser(User user) {
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        
        // FIXED: Assign default role if no roles set
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Role defaultRole = roleService.getRoleByName("USER");
            if (defaultRole != null) {
                user.addRole(defaultRole);
            } else {
                System.err.println("Warning: Role 'USER' not found in database!");
            }
        }
        
        return this.userRepository.addUser(user);
    }

    @Override
    public boolean updateUser(User user) {
        return this.userRepository.updateUser(user);
    }

    @Override
    public boolean deleteUser(Long id) {
        return this.userRepository.deleteUser(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("====== DEBUG AUTHENTICATION ======");
        System.out.println("Looking for user: " + username);

        User user = this.userRepository.getUserByUsername(username);

        if (user == null) {
            System.out.println("User not found! Username: " + username);
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        System.out.println("User found! Username: " + username);
        System.out.println("Password hash: " + user.getPassword());
        System.out.println("User active: " + user.isActive());
        System.out.println("User roles count: " + user.getRoles().size());

        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role role : user.getRoles()) {
            System.out.println("Role: " + role.getName());
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isActive(),
                true, true, true,
                authorities);
    }

  
    @Override
    public User addUser(Map<String, String> params, MultipartFile avatar) {
        try {
            User user = new User();
            
            // Set basic user information from params
            user.setUsername(params.get("username"));
            user.setPassword(this.passwordEncoder.encode(params.get("password")));
            user.setDisplayName(params.get("displayName"));
            user.setEmail(params.get("email"));
            user.setPhone(params.get("phone"));
            user.setActive(true);
            
            // Handle avatar upload if provided
            if (avatar != null && !avatar.isEmpty()) {
                try {
                    Map uploadResult = cloudinary.uploader().upload(avatar.getBytes(), 
                        ObjectUtils.asMap(
                            "folder", "avatars",
                            "public_id", "avatar_" + user.getUsername(),
                            "overwrite", true,
                            "resource_type", "image"
                        ));
                    user.setAvatar((String) uploadResult.get("secure_url"));
                } catch (IOException e) {
                    System.err.println("Error uploading avatar: " + e.getMessage());
                    // Continue without avatar if upload fails
                }
            }
            
            // Assign role based on roleParam or default to USER
            String roleName = params.get("role");
            if (roleName == null || roleName.isEmpty()) {
                roleName = "USER"; // Default role
            }
            
            Role role = roleService.getRoleByName(roleName);
            if (role == null) {
                role = roleService.getRoleByName("USER"); // Fallback to USER role
            }
            
            if (role != null) {
                user.addRole(role);
            }
            
            // Save user
            boolean saved = this.userRepository.addUser(user);
            if (saved) {
                return user;
            }
            return null;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating user: " + e.getMessage());
        }
    }

    
    @Override
    public boolean authenticate(String username, String password) {
        try {
            User user = this.getUserByUsername(username);
            if (user != null && user.isActive()) {
                return passwordEncoder.matches(password, user.getPassword());
            }
            return false;
        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
            return false;
        }
    }
}