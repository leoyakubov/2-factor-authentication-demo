const DEFAULT_API_BASE_URL = "http://localhost:8081";

export const API_BASE_URL =
  process.env.VITE_API_BASE_URL ||
  DEFAULT_API_BASE_URL;
