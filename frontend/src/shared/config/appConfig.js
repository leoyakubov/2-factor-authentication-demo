const DEFAULT_API_BASE_URL = "http://localhost:8081";
const configuredApiBaseUrl =
  typeof __API_BASE_URL__ !== "undefined" ? __API_BASE_URL__ : undefined;

export const API_BASE_URL =
  configuredApiBaseUrl ||
  DEFAULT_API_BASE_URL;
