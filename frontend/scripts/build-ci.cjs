const { spawnSync } = require("node:child_process");
const os = require("node:os");
const path = require("node:path");

const viteBin = path.join(__dirname, "..", "node_modules", "vite", "bin", "vite.js");
const buildOutDir = path.join(os.tmpdir(), "two-factor-authentication-demo-frontend-dist");

const result = spawnSync(process.execPath, [viteBin, "build"], {
  stdio: "inherit",
  env: {
    ...process.env,
    VITE_BUILD_OUT_DIR: buildOutDir,
  },
});

process.exit(result.status ?? 1);
