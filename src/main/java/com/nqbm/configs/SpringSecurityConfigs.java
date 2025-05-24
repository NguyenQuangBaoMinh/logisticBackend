/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.configs;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.nqbm.filters.JwtFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableWebSecurity
@EnableTransactionManagement
@EnableMethodSecurity(prePostEnabled = true)
@ComponentScan(basePackages = {
    "com.nqbm.controllers",
    "com.nqbm.repositories",
    "com.nqbm.services",
    "com.nqbm.filters" 
})
public class SpringSecurityConfigs {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private DataSource dataSource;
    
    // FIX: Inject JWT Filter
    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
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
                
                // FIX: Add JWT Filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                
                .authorizeHttpRequests(auth -> auth
                // ===== PUBLIC ENDPOINTS =====
                .requestMatchers("/", "/home", "/login", "/logout", "/register", "/error").permitAll()
                .requestMatchers("/static/**", "/resources/**").permitAll()
                .requestMatchers("/debug-auth", "/create-new-admin", "/generate-password").permitAll()
                .requestMatchers("/api/auth/login", "/api/auth/users", "/api/auth/logout").permitAll()  // Public auth endpoints
                
                // ===== SECURE ENDPOINTS REQUIRING JWT =====
                .requestMatchers("/api/auth/secure/**").authenticated()  // JWT protected endpoints
                .requestMatchers("/api/secure/**").authenticated()       // Additional secure endpoints
                
                // ===== ADMIN ONLY ENDPOINTS =====
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/suppliers/manage/**").hasRole("ADMIN")
                .requestMatchers("/api/inventory/**").hasRole("ADMIN")
                .requestMatchers("/api/reports/**").hasRole("ADMIN")
                .requestMatchers("/inventory/**").hasRole("ADMIN")
                
                // ===== PRODUCTS - Mixed Access =====
                .requestMatchers(HttpMethod.GET, "/api/products/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/products/**").hasRole("ADMIN") // CRUD operations for Admin only

                // ===== SUPPLIERS - Mixed Access =====
                .requestMatchers(HttpMethod.GET, "/api/suppliers/ratings/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers(HttpMethod.GET, "/api/suppliers/reviews/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers(HttpMethod.GET, "/api/suppliers/public/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/suppliers").hasRole("ADMIN") // Web interface for admin
                .requestMatchers("/suppliers/**").hasRole("ADMIN")
                .requestMatchers("/api/suppliers/**").hasRole("ADMIN") // API CRUD for Admin

                // ===== ORDERS - Mixed Access =====
                .requestMatchers(HttpMethod.GET, "/api/orders/my/**").hasRole("USER") // User's own orders
                .requestMatchers(HttpMethod.POST, "/api/orders").hasAnyRole("ADMIN", "USER") // Create orders
                .requestMatchers(HttpMethod.PUT, "/api/orders/my/**").hasRole("USER") // Update own orders
                .requestMatchers("/orders").hasRole("ADMIN") // Web interface for admin
                .requestMatchers("/orders/**").hasRole("ADMIN")
                .requestMatchers("/api/orders/**").hasRole("ADMIN") // Admin sees all orders

                // ===== PAYMENTS - Mixed Access =====
                .requestMatchers(HttpMethod.GET, "/api/payments/my/**").hasRole("USER") // User's own payments
                .requestMatchers(HttpMethod.POST, "/api/payments").hasAnyRole("ADMIN", "USER") // Make payments
                .requestMatchers("/payments/**").hasRole("ADMIN") // Web interface for admin
                .requestMatchers("/api/payments/**").hasRole("ADMIN") // Admin sees all payments

                // ===== SHIPPING - Mixed Access =====
                .requestMatchers(HttpMethod.GET, "/api/shipping/track/**").hasAnyRole("ADMIN", "USER") // Track shipments
                .requestMatchers(HttpMethod.GET, "/api/shipping/my/**").hasRole("USER") // User's own shipments
                .requestMatchers("/shipping/**").hasRole("ADMIN") // Web interface for admin
                .requestMatchers("/api/shipping/**").hasRole("ADMIN") // Admin manages shipping

                // ===== SUPPORT - Mixed Access =====
                .requestMatchers(HttpMethod.POST, "/api/support/tickets").hasAnyRole("ADMIN", "USER") // Create support tickets
                .requestMatchers(HttpMethod.GET, "/api/support/my-tickets/**").hasRole("USER") // User's own tickets
                .requestMatchers("/api/support/**").hasRole("ADMIN") // Admin manages support

                // ===== PRICES - Admin Only =====
                .requestMatchers("/prices/**").hasRole("ADMIN")
                .requestMatchers("/api/prices/**").hasRole("ADMIN")
                
                // ===== DEFAULT =====
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
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*")); // Allow all headers
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