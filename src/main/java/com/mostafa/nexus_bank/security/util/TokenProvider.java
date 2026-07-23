package com.mostafa.nexus_bank.security.util;

import com.mostafa.nexus_bank.security.jwt.JwtService;
import com.mostafa.nexus_bank.security.service.CustomUserDetails;
import com.mostafa.nexus_bank.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    private static final Logger log = LoggerFactory.getLogger(TokenProvider.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public Authentication getAuthentication(String token) {
        String username = jwtService.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtService.isTokenValid(token, userDetails)) {
            return new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
        }
        return null;
    }

    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }
}
