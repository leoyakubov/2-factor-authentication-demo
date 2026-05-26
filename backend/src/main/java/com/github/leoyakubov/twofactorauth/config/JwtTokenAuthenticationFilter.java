package com.github.leoyakubov.twofactorauth.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import com.github.leoyakubov.twofactorauth.model.AuthUserDetails;
import com.github.leoyakubov.twofactorauth.service.JwtTokenManager;
import com.github.leoyakubov.twofactorauth.service.UserService;

import java.io.IOException;

public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtCookieManager cookieManager;
    private final JwtTokenManager tokenProvider;
    private final UserService userService;

    public JwtTokenAuthenticationFilter(
            JwtCookieManager cookieManager,
            JwtTokenManager tokenProvider,
            UserService userService) {
        this.cookieManager = cookieManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = cookieManager.resolveToken(request);

        if(token == null || token.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        if(tokenProvider.validateToken(token)) {
            Claims claims = tokenProvider.getClaimsFromJWT(token);
            String username = claims.getSubject();

            UsernamePasswordAuthenticationToken auth =
                    userService.findByUsername(username)
                            .map(AuthUserDetails::new)
                            .map(userDetails -> {
                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails, null, userDetails.getAuthorities());
                                authentication
                                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                                return authentication;
                            })
                            .orElse(null);

            SecurityContextHolder.getContext().setAuthentication(auth);
        } else {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }

}
