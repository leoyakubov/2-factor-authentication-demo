const isDevelopment = typeof process !== "undefined" && process.env.NODE_ENV === "development";

function log(level, message, details) {
  if (!isDevelopment || typeof console === "undefined") {
    return;
  }

  const payload = details && Object.keys(details).length > 0 ? details : undefined;
  console[level](message, payload);
}

export function logApiFailure({ method, url, status, requestId, message, body }) {
  log("error", `[api] ${method} ${url} failed`, {
    status,
    requestId,
    message,
    body,
  });
}

export function logAuthEvent(message, details = {}) {
  log("info", `[auth] ${message}`, details);
}
