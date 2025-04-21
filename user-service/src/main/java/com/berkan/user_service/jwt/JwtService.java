package com.berkan.user_service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.security.Signature;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Component
@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String  SECRET_KEY;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    public <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token).getBody();
        return claimsTFunction.apply(claims);
    }

    public String getUsernameByToken(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public long getExpirationTime(){
        return jwtExpiration;
    }

    public boolean isTokenValid(String token) {
        String usernameFromToken = getUsernameByToken(token);
        return usernameFromToken != null && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        Date expiredDate = extractClaims(token, Claims::getExpiration);
        return expiredDate.before(new Date());
    }
    public Key getKey() {
       byte[] keyBytes= Decoders.BASE64.decode(SECRET_KEY);
       return Keys.hmacShaKeyFor(keyBytes);
    }
}
