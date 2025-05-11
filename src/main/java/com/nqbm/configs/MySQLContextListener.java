/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.configs;

/**
 *
 * @author baominh14022004gmail.com
 */


import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

@WebListener
public class MySQLContextListener implements ServletContextListener {
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Đóng AbandonedConnectionCleanupThread
        try {
            Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread")
                .getMethod("checkedShutdown").invoke(null);
            System.out.println("MySQL AbandonedConnectionCleanupThread shutdown successfully");
        } catch (Exception e) {
            System.err.println("Error shutting down MySQL thread: " + e.getMessage());
        }
        
        // Hủy đăng ký các JDBC drivers
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                System.out.println("Deregistered JDBC driver: " + driver);
            } catch (SQLException e) {
                System.err.println("Error deregistering JDBC driver: " + e.getMessage());
            }
        }
    }
}