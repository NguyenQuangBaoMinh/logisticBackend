/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.utils;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Date;

public class JwtUtils {
    // SECRET nên được lưu bằng biến môi trường
    private static final String SECRET = "12345678901234567890123456789012"; // 32 ký tự (AES key)
    private static final long EXPIRATION_MS = 86400000; // 1 ngày

    public static String generateToken(String username) throws Exception {
        System.out.println("🔑 Generating JWT token for user: " + username);
        
        JWSSigner signer = new MACSigner(SECRET);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(username)
                .expirationTime(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claimsSet
        );

        signedJWT.sign(signer);
        String token = signedJWT.serialize();
        
        System.out.println(" JWT token generated successfully: " + token.substring(0, 50) + "...");
        return token;
    }

    public static String validateTokenAndGetUsername(String token) throws Exception {
        System.out.println(" Validating JWT token: " + token.substring(0, 50) + "...");
        
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(SECRET);

            if (signedJWT.verify(verifier)) {
                Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
                if (expiration.after(new Date())) {
                    String username = signedJWT.getJWTClaimsSet().getSubject();
                    System.out.println(" JWT token valid for user: " + username);
                    return username;
                } else {
                    System.out.println(" JWT token expired");
                }
            } else {
                System.out.println(" JWT token signature invalid");
            }
        } catch (Exception e) {
            System.out.println(" JWT token validation error: " + e.getMessage());
            throw e;
        }
        
        return null;
    }

    // FIX: Implement the missing method
    public static String getUsernameFromToken(String token) {
        try {
            return validateTokenAndGetUsername(token);
        } catch (Exception e) {
            System.out.println(" Error getting username from token: " + e.getMessage());
            return null;
        }
    }
    
    // Additional helper method to check if token is valid
    public static boolean isTokenValid(String token) {
        try {
            String username = validateTokenAndGetUsername(token);
            return username != null;
        } catch (Exception e) {
            return false;
        }
    }
}