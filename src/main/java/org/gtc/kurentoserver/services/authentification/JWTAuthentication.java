package org.gtc.kurentoserver.services.authentification;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.gtc.kurentoserver.api.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.function.Function;


public class JWTAuthentication implements SessionManager
{
    private static final Logger log = LoggerFactory.getLogger(JWTAuthentication.class);

    private static final long serialVersionUID = -2550185165626007488L;

    private final List<String> activeTokens = new ArrayList<>();

    @Value( "${jwt.time.valid}" )
    public long JWT_TOKEN_VALIDITY;

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public boolean sessionAlive(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            boolean isAlive = expiration.after(new Date());
            if (!isAlive || !activeTokens.contains(token))
                activeTokens.remove(token);
            return isAlive && activeTokens.contains(token);
        } catch (ExpiredJwtException ex) {
            activeTokens.remove(token);
            return false;
        }
    }

    @Override
    public void destroySession(String token) {
        activeTokens.remove(token);
    }

    @Override
    public String createSession(String username) {
        Map<String, Object> claims = new HashMap<>();
        String token = Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
        activeTokens.add(token);
        return token;
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

}
