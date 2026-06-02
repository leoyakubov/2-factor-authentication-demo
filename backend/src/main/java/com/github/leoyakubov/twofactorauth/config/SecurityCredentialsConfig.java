package com.github.leoyakubov.twofactorauth.config;

import com.github.leoyakubov.twofactorauth.service.JwtTokenManager;
import com.github.leoyakubov.twofactorauth.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityCredentialsConfig {

    private final CorsProperties corsProperties;
    private final JwtCookieManager cookieManager;
    private final JwtTokenManager tokenProvider;
    private final UserService userService;
    private final UserDetailsService userDetailsService;

    public SecurityCredentialsConfig(CorsProperties corsProperties,
                                     JwtCookieManager cookieManager,
                                     JwtTokenManager tokenProvider,
                                     UserService userService,
                                     UserDetailsService userDetailsService) {
        this.corsProperties = corsProperties;
        this.cookieManager = cookieManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authz) -> authz
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.GET, "/", "/login", "/signup", "/verify", "/qrcode", "/csrf").permitAll()
                .requestMatchers("/signin", "/verify", "/logout", "/users").permitAll()
                .anyRequest().authenticated()
        );

        http.cors(Customizer.withDefaults());
        http.csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository()));
        http.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.exceptionHandling(exConf -> exConf.authenticationEntryPoint((req, resp, ex) ->
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")));
        http.headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                        "default-src 'self'; " +
                                "base-uri 'self'; " +
                                "form-action 'self'; " +
                                "frame-ancestors 'none'; " +
                                "img-src 'self' data:; " +
                                "script-src 'self'; " +
                                "style-src 'self' 'unsafe-inline'; " +
                                "connect-src 'self' http://localhost:3000 http://localhost:8081"))
                .referrerPolicy(referrer -> referrer.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
        );
        http.addFilterBefore(new JwtTokenAuthenticationFilter(cookieManager, tokenProvider, userService),
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        return repository;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(corsProperties.getAllowedOrigins());
        config.addAllowedHeader("*");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("HEAD");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
}
