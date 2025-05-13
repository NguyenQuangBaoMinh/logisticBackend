/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.configs;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import javax.sql.DataSource;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.debug.DebugFilter;

@Configuration
@EnableWebSecurity
@EnableTransactionManagement
@EnableMethodSecurity(prePostEnabled = true)
@ComponentScan(basePackages = {
    "com.nqbm.controllers",
    "com.nqbm.repositories", 
    "com.nqbm.services"
})
public class SpringSecurityConfigs {

    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private DataSource dataSource;


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
        System.out.println("Configuring AuthenticationManager with userDetailsService");
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
        return auth.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler("/dashboard");
        handler.setUseReferer(true);
        return handler;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/", "/home", "/login", "/logout", 
                               "/register","/api/auth/**", "/error").permitAll()
                .requestMatchers("/static/**", "/resources/**").permitAll()
                
                // Debug endpoints
                .requestMatchers("/debug-auth", "/create-new-admin", "/generate-password").permitAll()
                
                // Admin management
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Supplier management
                .requestMatchers("/suppliers").hasAnyRole("ADMIN", "PURCHASER")
                .requestMatchers("/suppliers/**").hasRole("PURCHASER")
                
                // Order management
                .requestMatchers("/orders").hasAnyRole("ADMIN", "SALES", "WAREHOUSE")
                .requestMatchers("/orders/create").hasRole("SALES")
                .requestMatchers("/orders/approve").hasRole("WAREHOUSE")
                
                // Inventory management
                .requestMatchers("/inventory/**").hasAnyRole("ADMIN", "WAREHOUSE")
                
                // Shipping management
                .requestMatchers("/shipping/**").hasAnyRole("ADMIN", "SHIPPER")
                
                // Payment management
                .requestMatchers("/payments/**").hasAnyRole("ADMIN", "ACCOUNTANT")
                
                // API endpoints
                .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("ADMIN", "MANAGER")
                
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard")
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .deleteCookies("JSESSIONID", "remember-me")
                .invalidateHttpSession(true)
                .permitAll()
            )
            .rememberMe(remember -> remember
                .tokenRepository(persistentTokenRepository())
                .tokenValiditySeconds(7 * 24 * 60 * 60) // 1 tuần
                .userDetailsService(userDetailsService)
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            );
        
        return http.build();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization", "X-Requested-With"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "dfkq1dhjr",
            "api_key", "621292875447649",
            "api_secret", "jJlrnzJlp7ujU_Wq9aQbSpLlYlQ",
            "secure", true
        ));
    }

    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
    
    @Bean
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }
}