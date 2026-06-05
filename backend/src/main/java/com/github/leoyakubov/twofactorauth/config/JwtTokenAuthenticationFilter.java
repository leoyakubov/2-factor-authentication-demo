package com.github.leoyakubov.twofactorauth.config;

import com.github.leoyakubov.twofactorauth.model.AuthUserDetails;
import com.github.leoyakubov.twofactorauth.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtCookieManager cookieManager;
    private final JwtTokenService tokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtTokenAuthenticationFilter(
            JwtCookieManager cookieManager,
            JwtTokenService tokenProvider,
            UserDetailsService userDetailsService) {
        this.cookieManager = cookieManager;
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = cookieManager.resolveToken(request);

        if (token == null || token.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        if (tokenProvider.validateToken(token)) {
            Claims claims = tokenProvider.getClaimsFromJWT(token);
            String username = claims.getSubject();

            try {
                AuthUserDetails userDetails = (AuthUserDetails) userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("authenticated {} on {} {}", username, request.getMethod(), request.getRequestURI());
            } catch (UsernameNotFoundException ex) {
                SecurityContextHolder.clearContext();
                log.warn("rejected JWT for missing user on {} {}", request.getMethod(), request.getRequestURI());
            }
        } else {
            SecurityContextHolder.clearContext();
            log.warn("rejected invalid JWT on {} {}", request.getMethod(), request.getRequestURI());
        }

        chain.doFilter(request, response);
    }

}
