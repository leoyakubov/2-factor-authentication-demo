import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import os from "node:os";
import path from "node:path";

const cacheDir = path.join(os.tmpdir(), "two-factor-demo-vite-cache");
const outDir = path.join(os.tmpdir(), "two-factor-demo-dist");

export default defineConfig(({ mode }) => {
  const apiBaseUrl = process.env.REACT_APP_API_BASE_URL || "http://localhost:8081";

  return {
    plugins: [react()],
    cacheDir,
    optimizeDeps: {
      noDiscovery: true,
      include: [],
    },
    build: {
      outDir,
    },
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
