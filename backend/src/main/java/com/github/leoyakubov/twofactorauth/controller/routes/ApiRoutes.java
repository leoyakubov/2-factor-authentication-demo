package com.github.leoyakubov.twofactorauth.controller.routes;

public final class ApiRoutes {

    public static final String ROOT_PATH = "/";
    public static final String LOGIN_PATH = "/login";
    public static final String SIGNUP_PATH = "/signup";
    public static final String VERIFY_PATH = "/verify";
    public static final String QRCODE_PATH = "/qrcode";
    public static final String SIGNIN_PATH = "/signin";
    public static final String USERS_PATH = "/users";
    public static final String LOGOUT_PATH = "/logout";
    public static final String CSRF_PATH = "/csrf";
    public static final String OPENAPI_PATH = "/v3/api-docs";
    public static final String OPENAPI_PATH_PATTERN = "/v3/api-docs/**";
    public static final String SWAGGER_UI_PATH = "/swagger-ui/**";
    public static final String SWAGGER_UI_HTML_PATH = "/swagger-ui.html";
    public static final String ACTUATOR_HEALTH_PATH = "/actuator/health";

    private ApiRoutes() {
    }
}
