import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
  const apiBaseUrl = process.env.REACT_APP_API_BASE_URL || "http://localhost:8081";

  return {
    plugins: [react()],
    server: {
      port: 3000,
      strictPort: true,
    },
    preview: {
      port: 3000,
      strictPort: true,
    },
    define: {
      "process.env.REACT_APP_API_BASE_URL": JSON.stringify(apiBaseUrl),
    },
  };
});
