package com.github.leoyakubov.twofactorauth.config;

import com.github.leoyakubov.twofactorauth.controller.routes.ApiRoutes;
import com.github.leoyakubov.twofactorauth.config.properties.CorsProperties;
import com.github.leoyakubov.twofactorauth.config.properties.JwtConfigProperties;
import com.github.leoyakubov.twofactorauth.config.properties.SecurityHeaderProperties;
import com.github.leoyakubov.twofactorauth.service.JwtTokenService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityCredentialsConfig {

    private final CorsProperties corsProperties;
    private final SecurityHeaderProperties securityHeaderProperties;
    private final JwtCookieManager cookieManager;
    private final JwtTokenService tokenProvider;

    public SecurityCredentialsConfig(CorsProperties corsProperties,
                                     SecurityHeaderProperties securityHeaderProperties,
                                     JwtCookieManager cookieManager,
                                     JwtTokenService tokenProvider) {
        this.corsProperties = corsProperties;
        this.securityHeaderProperties = securityHeaderProperties;
        this.cookieManager = cookieManager;
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter) throws Exception {
        http.authorizeHttpRequests((authz) -> authz
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.GET, ApiRoutes.ROOT_PATH, ApiRoutes.LOGIN_PATH,
                        ApiRoutes.SIGNUP_PATH, ApiRoutes.VERIFY_PATH, ApiRoutes.QRCODE_PATH,
                        ApiRoutes.CSRF_PATH).permitAll()
                .requestMatchers(ApiRoutes.SIGNIN_PATH, ApiRoutes.VERIFY_PATH,
                        ApiRoutes.LOGOUT_PATH, ApiRoutes.USERS_PATH).permitAll()
                .anyRequest().authenticated()
        );

        http.cors(Customizer.withDefaults());
        http.csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository()));
        http.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.exceptionHandling(exConf -> exConf.authenticationEntryPoint((req, resp, ex) ->
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")));
        http.headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(securityHeaderProperties.contentSecurityPolicyHeaderValue()))
                .referrerPolicy(referrer -> referrer.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
        );
        http.addFilterBefore(jwtTokenAuthenticationFilter,
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
        config.setAllowedOrigins(corsProperties.allowedOrigins());
        config.addAllowedHeader("*");
        config.addExposedHeader("X-Request-Id");
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
    public JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter(UserDetailsService userDetailsService) {
        return new JwtTokenAuthenticationFilter(cookieManager, tokenProvider, userDetailsService);
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
}
