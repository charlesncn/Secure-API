package com.example.demo_cyber_shujaa.config;


import com.example.demo_cyber_shujaa.exceptions.ErrorMsg;
import com.example.demo_cyber_shujaa.exceptions.CustomException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

@Service
public class JwtService {


    private final Logger logger = Logger.getLogger(JwtService.class.getName());
    @Value("${properties.security.jwt.secret}")
    private String secretKey;
    @Value("${properties.security.jwt.expiration}")
    private long jwtExpiration;
    @Value("${properties.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public String extractUsername(String token) throws CustomException {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) throws CustomException {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    public String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            if (username.equals(userDetails.getUsername())) {
                return isTokenActive(token);
            }
            return false;
        } catch (ExpiredJwtException | CustomException e) {
            logger.info(e.toString());
            return false;
        }
    }

    public boolean isTokenActive(String token) {
        try {
            return extractExpiration(token).after(new Date());
        } catch (ExpiredJwtException | CustomException e) {
            logger.info(e.toString());
            return false;
        }
    }

    public Date extractExpiration(String token) throws CustomException {
        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims(String token) throws CustomException {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (
                ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e) {
            logger.warning(e.toString());
            if (e instanceof io.jsonwebtoken.ExpiredJwtException)
                throw new CustomException(ErrorMsg.TOKEN_EXPIRED);
            if (e instanceof MalformedJwtException)
                throw new CustomException(ErrorMsg.INVALID_TOKEN);
            throw new CustomException(ErrorMsg.ILLEGAL_ARGUMENT);
        }
    }

    public Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
