package com.nqbm.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @JsonIgnore 
    @Column(nullable = false)
    private String password;
    
    @Column(name = "display_name")
    private String displayName;
    
    @Column(name = "email", unique = true)
    private String email;
    
    @Column(name = "phone", length = 15)
    private String phone;
    
    @Column(name = "is_active")
    private boolean active = true;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    private Date createdDate;
    
    @Column(name = "avatar")
    private String avatar;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    // Constructors
    public User() {
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
    public void addRole(Role role) {
        this.roles.add(role);
    }
    
    
    public String getUserRole() {
        if (roles != null && !roles.isEmpty()) {
            
            Role firstRole = roles.iterator().next();
            String roleName = firstRole.getName().toUpperCase();
            
            
            if (!roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName;
            }
            
            System.out.println("getUserRole() returning: " + roleName + " for user: " + username);
            return roleName;
        }
        System.out.println("getUserRole() returning null for user: " + username + " (no roles)");
        return null;
    }
    
    // Helper method to check if user has specific role
    public boolean hasRole(String roleName) {
        if (roles == null) return false;
        
        String normalizedRoleName = roleName.toUpperCase();
        if (normalizedRoleName.startsWith("ROLE_")) {
            normalizedRoleName = normalizedRoleName.substring(5);
        }
        
        for (Role role : roles) {
            if (role.getName().toUpperCase().equals(normalizedRoleName)) {
                return true;
            }
        }
        return false;
    }
    
    // Helper method to get all role names
    public Set<String> getRoleNames() {
        Set<String> roleNames = new HashSet<>();
        if (roles != null) {
            for (Role role : roles) {
                roleNames.add("ROLE_" + role.getName().toUpperCase());
            }
        }
        return roleNames;
    }
    
    @PrePersist
    protected void onCreate() {
        this.createdDate = new Date();
    }
}