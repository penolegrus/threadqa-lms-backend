package com.threadqa.lms.service.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    private final ObjectMapper objectMapper;
    
    @Value("classpath:keys/private-key.pem")
    private Resource privateKeyResource;
    
    @Value("classpath:keys/public-key.pem")
    private Resource publicKeyResource;
    
    private PrivateKey privateKey;
    private PublicKey publicKey;
    
    public JwtService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @PostConstruct
    public void init() {
        try {
            this.privateKey = loadPrivateKey();
            this.publicKey = loadPublicKey();
        } catch (Exception e) {
            log.error("Failed to load keys", e);
            throw new RuntimeException("Failed to load keys", e);
        }
    }
    
    private PrivateKey loadPrivateKey() throws Exception {
        String privateKeyContent = new String(Files.readAllBytes(privateKeyResource.getFile().toPath()));
        privateKeyContent = privateKeyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
    
    private PublicKey loadPublicKey() throws Exception {
        String publicKeyContent = new String(Files.readAllBytes(publicKeyResource.getFile().toPath()));
        publicKeyContent = publicKeyContent
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyContent);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
    
    /**
     * Кодирует объект в JWT токен, используя приватный ключ
     * 
     * @param payload объект для кодирования
     * @return JWT токен
     */
    public String encode(Object payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            return Jwts.builder()
                    .setPayload(jsonPayload)
                    .signWith(privateKey, SignatureAlgorithm.RS256)
                    .compact();
        } catch (Exception e) {
            log.error("Failed to encode JWT", e);
            throw new RuntimeException("Failed to encode JWT", e);
        }
    }
    
    /**
     * Декодирует JWT токен в объект указанного класса, используя публичный ключ
     * 
     * @param token JWT токен
     * @param clazz класс объекта для декодирования
     * @param <T> тип объекта
     * @return декодированный объект
     */
    public <T> T decode(String token, Class<T> clazz) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return objectMapper.readValue(claims.toString(), clazz);
        } catch (Exception e) {
            log.error("Failed to decode JWT", e);
            throw new RuntimeException("Failed to decode JWT", e);
        }
    }
}
