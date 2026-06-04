package com.github.leoyakubov.twofactorauth.controller;

import com.github.leoyakubov.twofactorauth.config.FrontendProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Arrays;

@Controller
@Slf4j
public class SpaRouteController {

    private final FrontendProperties frontendProperties;

    public SpaRouteController(FrontendProperties frontendProperties) {
        this.frontendProperties = frontendProperties;
    }

    @GetMapping({"/", "/login", "/signup", "/verify", "/qrcode"})
    public String redirectToFrontend(HttpServletRequest request) {
        LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        request.getParameterMap().forEach((key, values) -> queryParams.addAll(key, Arrays.asList(values)));

        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontendProperties.baseUrl())
                .path(request.getRequestURI())
                .queryParams(queryParams)
                .build(true)
                .toUriString();

        log.info("redirecting {} {} to frontend {}", request.getMethod(), request.getRequestURI(), redirectUrl);
        return "redirect:" + redirectUrl;
    }
}
