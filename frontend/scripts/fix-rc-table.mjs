import { mkdir, writeFile, access } from "node:fs/promises";
import { constants as fsConstants } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const frontendRoot = fileURLToPath(new URL("..", import.meta.url));
const targetFile = path.join(frontendRoot, "node_modules", "rc-table", "es", "index.js");

async function main() {
  try {
    await access(targetFile, fsConstants.F_OK);
    return;
  } catch (error) {
    // File is missing, create the shim below.
  }

  await mkdir(path.dirname(targetFile), { recursive: true });
  await writeFile(
    targetFile,
    'export { default } from "../lib/index.js";\nexport * from "../lib/index.js";\n'
  );
}

await main();
