import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import os from "node:os";
import path from "node:path";

const cacheDir = path.join(os.tmpdir(), "two-factor-demo-vite-cache");
const outDir = path.join(os.tmpdir(), "two-factor-demo-dist");

export default defineConfig({
  plugins: [react()],
  cacheDir,
  define: {
    "process.env.VITE_API_BASE_URL": JSON.stringify(
      process.env.VITE_API_BASE_URL || "http://localhost:8081"
    ),
  },
  optimizeDeps: {
    force: true,
    noDiscovery: true,
    include: [
      "react",
      "react-dom",
      "react-dom/client",
      "antd",
      "@ant-design/icons",
      "@ant-design/cssinjs",
      "rc-util",
      "rc-table",
      "react-is",
      "react-router-dom",
      "prop-types",
    ],
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
});
