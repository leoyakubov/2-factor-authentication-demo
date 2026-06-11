import { API_BASE_URL } from "../config/appConfig";
import { logApiFailure } from "../logging/logger";

const XSRF_COOKIE_NAME = "XSRF-TOKEN";
let csrfTokenRequestPromise;
let cachedCsrfToken;

const generateRequestId = () => {
  if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
    return crypto.randomUUID();
  }

  return `req-${Date.now()}-${Math.random().toString(16).slice(2)}`;
};

const getCookieValue = (name) => {
  if (typeof document === "undefined") {
    return null;
  }

  return document.cookie
    .split("; ")
    .find((cookie) => cookie.startsWith(`${name}=`))
    ?.split("=")
    .slice(1)
    .join("=") || null;
};

const ensureCsrfToken = async ({ forceRefresh = false } = {}) => {
  if (!forceRefresh) {
    const cachedToken = cachedCsrfToken || getCookieValue(XSRF_COOKIE_NAME);
    if (cachedToken) {
      cachedCsrfToken = cachedToken;
      return cachedToken;
    }
  }

  if (!csrfTokenRequestPromise) {
    csrfTokenRequestPromise = fetch(`${API_BASE_URL}/csrf`, {
      method: "GET",
      credentials: "include",
    })
      .then(parseBody)
      .then((body) => {
        cachedCsrfToken = body.token || getCookieValue(XSRF_COOKIE_NAME);
        return cachedCsrfToken;
      })
      .finally(() => {
        csrfTokenRequestPromise = undefined;
      });
  }

  await csrfTokenRequestPromise;
  return cachedCsrfToken;
};

const parseBody = async (response) => {
  const text = await response.text();

  if (!text) {
    return {};
  }

  try {
    return JSON.parse(text);
  } catch {
    return { message: text };
  }
};

const isCsrfFailure = (error) =>
  error.status === 403 &&
  typeof error.body?.message === "string" &&
  (error.body.message.includes("security token") ||
    error.body.message.includes("browser security checks"));

const request = async (options, attempt = 0) => {
  const headers = new Headers(options.headers || {});
  const method = (options.method || "GET").toUpperCase();
  const requestId = options.requestId || generateRequestId();

  headers.set("Accept", "application/json");
  if (options.setContentType !== false && options.body) {
    headers.append("Content-Type", "application/json");
  }

  if (!["GET", "HEAD", "OPTIONS"].includes(method) && !options.skipCsrf) {
    const csrfToken = await ensureCsrfToken({ forceRefresh: true });
    if (csrfToken) {
      headers.set("X-XSRF-TOKEN", csrfToken);
    }
  }

  headers.set("X-Request-Id", requestId);

  const response = await fetch(options.url, {
    ...options,
    method,
    headers,
    credentials: "include",
  });
  const body = await parseBody(response);
  const responseRequestId = response.headers?.get?.("X-Request-Id") || requestId;

  if (!response.ok) {
    const error = new Error(body.message || response.statusText || "Request failed");
    error.status = response.status;
    error.body = body;
    error.requestId = responseRequestId;
    error.method = method;
    error.url = options.url;
    logApiFailure({
      method,
      url: options.url,
      status: response.status,
      requestId: responseRequestId,
      message: error.message,
      body,
    });

    if (
      attempt === 0 &&
      !["GET", "HEAD", "OPTIONS"].includes(method) &&
      !options.skipCsrf &&
      isCsrfFailure(error)
    ) {
      csrfTokenRequestPromise = undefined;
      cachedCsrfToken = undefined;
      await ensureCsrfToken({ forceRefresh: true });
      return request(options, attempt + 1);
    }

    throw error;
  }

  return body;
};

export function login(loginRequest) {
  return request({
    url: `${API_BASE_URL}/signin`,
    method: "POST",
    body: JSON.stringify(loginRequest),
  });
}

export function verify(verifyRequest) {
  return request({
    url: `${API_BASE_URL}/verify`,
    method: "POST",
    body: JSON.stringify(verifyRequest),
  });
}

export function signup(signupRequest) {
  return request({
    url: `${API_BASE_URL}/users`,
    method: "POST",
    body: JSON.stringify(signupRequest),
  });
}

export function getCurrentUser() {
  return request({
    url: `${API_BASE_URL}/users/me`,
    method: "GET",
  });
}

export function logout() {
  return request({
    url: `${API_BASE_URL}/logout`,
    method: "POST",
  });
}

export function __resetCsrfTokenCacheForTests() {
  csrfTokenRequestPromise = undefined;
  cachedCsrfToken = undefined;
}
