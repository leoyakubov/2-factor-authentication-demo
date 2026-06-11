package com.github.leoyakubov.twofactorauth.config;

import com.github.leoyakubov.twofactorauth.config.properties.CorsProperties;
import com.github.leoyakubov.twofactorauth.config.properties.FrontendProperties;
import com.github.leoyakubov.twofactorauth.config.properties.SecurityHeaderProperties;
import com.github.leoyakubov.twofactorauth.config.JwtCookieManager;
import com.github.leoyakubov.twofactorauth.controller.routes.ApiRoutes;
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityCredentialsConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CookieCsrfTokenRepository csrfTokenRepository,
                                                   JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter,
                                                   SecurityHeaderProperties securityHeaderProperties,
                                                   JwtCookieManager jwtCookieManager) throws Exception {
        http.authorizeHttpRequests((authz) -> authz
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.GET, ApiRoutes.ROOT_PATH, ApiRoutes.LOGIN_PATH,
                        ApiRoutes.SIGNUP_PATH, ApiRoutes.VERIFY_PATH, ApiRoutes.QRCODE_PATH,
                        ApiRoutes.CSRF_PATH, ApiRoutes.ACTUATOR_HEALTH_PATH).permitAll()
                .requestMatchers(HttpMethod.GET, ApiRoutes.OPENAPI_PATH, ApiRoutes.OPENAPI_PATH_PATTERN,
                        ApiRoutes.SWAGGER_UI_PATH, ApiRoutes.SWAGGER_UI_HTML_PATH).permitAll()
                .requestMatchers(ApiRoutes.SIGNIN_PATH, ApiRoutes.VERIFY_PATH,
                        ApiRoutes.LOGOUT_PATH, ApiRoutes.USERS_PATH).permitAll()
                .anyRequest().authenticated()
        );

        http.cors(Customizer.withDefaults());
        http.csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository));
        http.logout(logout -> logout
                .logoutUrl(ApiRoutes.LOGOUT_PATH)
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    response.setHeader(org.springframework.http.HttpHeaders.SET_COOKIE,
                            jwtCookieManager.clearCookie().toString());
                })
                .clearAuthentication(true)
                .invalidateHttpSession(true));
        http.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.exceptionHandling(exConf -> exConf
                .authenticationEntryPoint((req, resp, ex) ->
                        writeApiError(resp, HttpServletResponse.SC_UNAUTHORIZED,
                                "We couldn't sign you in. Please check your details and try again."))
                .accessDeniedHandler((req, resp, ex) -> {
                    String message = getAccessDeniedMessage(ex);
                    writeApiError(resp, HttpServletResponse.SC_FORBIDDEN, message);
                }));
        http.headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(securityHeaderProperties.contentSecurityPolicyHeaderValue()))
                .referrerPolicy(referrer -> referrer.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
        );
        http.addFilterBefore(jwtTokenAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CookieCsrfTokenRepository csrfTokenRepository(FrontendProperties frontendProperties) {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        boolean secureDeployment = frontendProperties.baseUrl().startsWith("https://");
        repository.setCookieCustomizer(cookie -> {
            cookie.path("/");
            cookie.secure(secureDeployment);
            cookie.sameSite(secureDeployment ? "None" : "Lax");
        });
        return repository;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
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
    public JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter(JwtCookieManager cookieManager,
                                                                      JwtTokenService tokenProvider,
                                                                      UserDetailsService userDetailsService) {
        return new JwtTokenAuthenticationFilter(cookieManager, tokenProvider, userDetailsService);
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    private static void writeApiError(HttpServletResponse response,
                                      int status,
                                      String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"" + escapeJson(message) + "\"}");
    }

    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static String getAccessDeniedMessage(Exception ex) {
        if (ex instanceof MissingCsrfTokenException || ex instanceof InvalidCsrfTokenException) {
            return "Your security token expired or is missing. Please refresh the page and try again.";
        }

        return "Your request was blocked by browser security checks. Please refresh the page and try again.";
    }
}
