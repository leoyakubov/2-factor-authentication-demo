import path from "node:path";
import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const buildOutDir = env.VITE_BUILD_OUT_DIR || "dist";

  return {
    plugins: [react()],
    publicDir: false,
    build: {
      outDir: path.isAbsolute(buildOutDir) ? buildOutDir : path.resolve(buildOutDir),
      emptyOutDir: false,
    },
    define: {
      __API_BASE_URL__: JSON.stringify(
        env.VITE_API_BASE_URL || "http://localhost:8081"
      ),
    },
    server: {
      port: 3000,
      strictPort: true,
    },
    preview: {
      port: 3000,
      strictPort: true,
    },
  };
});
