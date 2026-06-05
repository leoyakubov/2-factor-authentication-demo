const fs = require("node:fs");
const os = require("node:os");
const path = require("node:path");
const Module = require("node:module");
const babel = require("@babel/core");
const { JSDOM } = require("jsdom");
const expectModule = require("expect");
const { fn, spyOn } = require("jest-mock");

const projectRoot = path.resolve(__dirname, "..");
const tempRoot = path.join(os.tmpdir(), "two-factor-authentication-demo-jest");
const tempSrcRoot = path.join(tempRoot, "src");
const tempTestRoot = path.join(tempRoot, "test");

process.env.NODE_PATH = path.join(projectRoot, "node_modules");
Module._initPaths();

const expect = expectModule.expect || expectModule.default || expectModule;

function walkFiles(dir) {
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      return walkFiles(fullPath);
    }

    return [fullPath];
  });
}

function ensureDir(filePath) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
}

function copyFile(source, target) {
  ensureDir(target);
  fs.copyFileSync(source, target);
}

function transpileJsFile(source, target) {
  const result = babel.transformFileSync(source, {
    babelrc: false,
    configFile: false,
    filename: source,
    presets: [
      [require.resolve("@babel/preset-env"), { targets: { node: "current" }, modules: "commonjs" }],
      [require.resolve("@babel/preset-react"), { runtime: "automatic" }],
      require.resolve("babel-preset-jest"),
    ],
  });

  ensureDir(target);
  fs.writeFileSync(target, result.code, "utf8");
}

function buildTempTree() {
  fs.rmSync(tempRoot, { recursive: true, force: true });
  fs.mkdirSync(path.join(tempRoot, "node_modules"), { recursive: true });
  fs.cpSync(path.join(projectRoot, "node_modules", "jest-circus"), path.join(tempRoot, "node_modules", "jest-circus"), {
    recursive: true,
  });

  for (const source of walkFiles(path.join(projectRoot, "src"))) {
    const ext = path.extname(source);
    if (ext !== ".js" && ext !== ".jsx") {
      continue;
    }

    const relative = path.relative(path.join(projectRoot, "src"), source);
    const target = path.join(tempSrcRoot, relative).replace(/\.jsx$/, ".js");
    transpileJsFile(source, target);
  }

  for (const source of walkFiles(path.join(projectRoot, "test"))) {
    if (!source.endsWith(".cjs")) {
      continue;
    }

    const relative = path.relative(path.join(projectRoot, "test"), source);
    const target = path.join(tempTestRoot, relative);
    copyFile(source, target);
  }
}

function setupDomEnvironment() {
  const dom = new JSDOM("<!doctype html><html><body></body></html>", {
    url: "http://localhost/",
  });

  globalThis.window = dom.window;
  globalThis.document = dom.window.document;
  globalThis.navigator = dom.window.navigator;
  globalThis.location = dom.window.location;
  globalThis.window = dom.window;
  globalThis.getComputedStyle = dom.window.getComputedStyle.bind(dom.window);

  const browserGlobals = [
    "Element",
    "HTMLElement",
    "HTMLAnchorElement",
    "HTMLButtonElement",
    "HTMLDivElement",
    "HTMLFormElement",
    "HTMLImageElement",
    "HTMLInputElement",
    "HTMLLabelElement",
    "HTMLLiElement",
    "HTMLOptionElement",
    "HTMLOListElement",
    "HTMLParagraphElement",
    "HTMLSelectElement",
    "HTMLSpanElement",
    "HTMLTextAreaElement",
    "Node",
    "NodeList",
    "Document",
    "DocumentFragment",
    "ShadowRoot",
    "SVGElement",
    "SVGGraphicsElement",
    "SVGSVGElement",
    "EventTarget",
    "MutationObserver",
    "CustomEvent",
    "Event",
    "KeyboardEvent",
    "MouseEvent",
    "PointerEvent",
    "FocusEvent",
    "InputEvent",
    "File",
    "FormData",
    "Blob",
    "DOMRect",
  ];

  for (const name of browserGlobals) {
    if (typeof globalThis[name] === "undefined" && typeof dom.window[name] !== "undefined") {
      globalThis[name] = dom.window[name];
    }
  }

  if (typeof globalThis.TextEncoder === "undefined" || typeof globalThis.TextDecoder === "undefined") {
    const { TextEncoder, TextDecoder } = require("node:util");
    if (typeof globalThis.TextEncoder === "undefined") {
      globalThis.TextEncoder = TextEncoder;
    }
    if (typeof globalThis.TextDecoder === "undefined") {
      globalThis.TextDecoder = TextDecoder;
    }
  }

  return dom;
}

function createMiniJest(mockRegistry, trackedMocks, trackedSpies) {
  return {
    fn: (...args) => {
      const mockFn = fn(...args);
      trackedMocks.add(mockFn);
      return mockFn;
    },
    spyOn: (...args) => {
      const spy = spyOn(...args);
      trackedSpies.add(spy);
      return spy;
    },
    mock: (request, factory) => {
      if (!activeTestFile) {
        throw new Error(`jest.mock(${request}) called outside a test file`);
      }

      const resolver = Module.createRequire(activeTestFile);
      const resolved = resolver.resolve(request);
      const mockExports = typeof factory === "function" ? factory() : {};

      if (!mockRegistry.has(activeTestFile)) {
        mockRegistry.set(activeTestFile, new Map());
      }

      mockRegistry.get(activeTestFile).set(resolved, mockExports);
      delete require.cache[resolved];
      return mockExports;
    },
    requireActual: (request) => {
      if (!activeTestFile) {
        return Module.createRequire(path.join(projectRoot, "package.json"))(request);
      }

      const prev = requireActualGuard.active;
      requireActualGuard.active = true;
      try {
        return Module.createRequire(activeTestFile)(request);
      } finally {
        requireActualGuard.active = prev;
      }
    },
    clearAllMocks: () => {
      for (const mockFn of trackedMocks) {
        mockFn.mockClear();
      }
      for (const spy of trackedSpies) {
        spy.mockClear();
      }
    },
    restoreAllMocks: () => {
      for (const spy of trackedSpies) {
        spy.mockRestore();
      }
      trackedSpies.clear();
    },
    resetAllMocks: () => {
      for (const mockFn of trackedMocks) {
        mockFn.mockReset();
      }
      for (const spy of trackedSpies) {
        spy.mockReset();
      }
    },
    setTimeout: () => {},
  };
}

function createSuite(name, parent = null) {
  return {
    name,
    parent,
    items: [],
    beforeEachHooks: [],
    afterEachHooks: [],
  };
}

function clearTempModuleCache() {
  for (const cacheKey of Object.keys(require.cache)) {
    if (cacheKey.startsWith(tempRoot)) {
      delete require.cache[cacheKey];
    }
  }
}

function collectAncestorHooks(suite, key) {
  const hooks = [];
  let cursor = suite;
  while (cursor) {
    const currentHooks = cursor[key];
    if (key === "beforeEachHooks") {
      hooks.unshift(...currentHooks);
    } else {
      hooks.push(...currentHooks);
    }
    cursor = cursor.parent;
  }
  return hooks;
}

async function runHookList(hooks) {
  for (const hook of hooks) {
    await hook();
  }
}

async function runSuite(suite, pathParts, results) {
  for (const item of suite.items) {
    if (item.type === "suite") {
      await runSuite(item.suite, pathParts.concat(item.name), results);
      continue;
    }

    const fullName = pathParts.concat(item.name).filter(Boolean).join(" > ");
    const beforeHooks = collectAncestorHooks(item.suite, "beforeEachHooks");
    const afterHooks = collectAncestorHooks(item.suite, "afterEachHooks");

    try {
      await runHookList(beforeHooks);
      activeTestFile = item.file;
      await item.fn();
      console.log(`PASS ${fullName}`);
      results.passed += 1;
    } catch (error) {
      console.error(`FAIL ${fullName}`);
      console.error(error && error.stack ? error.stack : error);
      results.failed += 1;
    } finally {
      try {
        activeTestFile = item.file;
        await runHookList(afterHooks);
      } catch (error) {
        console.error(`FAIL ${fullName} (afterEach)`);
        console.error(error && error.stack ? error.stack : error);
        results.failed += 1;
      }
      activeTestFile = null;
      cleanup();
      document.body.innerHTML = "";
    }
  }
}

buildTempTree();
setupDomEnvironment();
const { cleanup } = require("@testing-library/react");

let rootSuite = createSuite("");
let suiteStack = [rootSuite];
const mockRegistry = new Map();
const trackedMocks = new Set();
const trackedSpies = new Set();
const requireActualGuard = { active: false };
let activeTestFile = null;

const miniJest = createMiniJest(mockRegistry, trackedMocks, trackedSpies);
const originalLoad = Module._load;

Module._load = function patchedLoad(request, parent, isMain) {
  if (request === "@jest/globals") {
    return {
      jest: miniJest,
      expect,
      describe: globalThis.describe,
      test: globalThis.test,
      it: globalThis.it,
      beforeEach: globalThis.beforeEach,
      afterEach: globalThis.afterEach,
      beforeAll: globalThis.beforeAll,
      afterAll: globalThis.afterAll,
    };
  }

  if (/\.(css|less|sass|scss)$/.test(request)) {
    return {};
  }

  if (!requireActualGuard.active && activeTestFile && mockRegistry.has(activeTestFile)) {
    const resolver = Module.createRequire(activeTestFile);
    try {
      const resolved = resolver.resolve(request);
      const mockForFile = mockRegistry.get(activeTestFile);
      if (mockForFile.has(resolved)) {
        return mockForFile.get(resolved);
      }
    } catch {
      // Ignore unresolved mock lookups and fall through to the real loader.
    }
  }

  return originalLoad.call(this, request, parent, isMain);
};

globalThis.jest = miniJest;
globalThis.describe = (name, fn) => {
  const parent = suiteStack[suiteStack.length - 1];
  const suite = createSuite(name, parent);
  parent.items.push({ type: "suite", name, suite });
  suiteStack.push(suite);
  try {
    fn();
  } finally {
    suiteStack.pop();
  }
};
globalThis.it = globalThis.test = (name, fn) => {
  const suite = suiteStack[suiteStack.length - 1];
  suite.items.push({ type: "test", name, fn, suite, file: activeTestFile });
};
globalThis.beforeEach = (fn) => {
  suiteStack[suiteStack.length - 1].beforeEachHooks.push(fn);
};
globalThis.afterEach = (fn) => {
  suiteStack[suiteStack.length - 1].afterEachHooks.push(fn);
};
globalThis.beforeAll = () => {};
globalThis.afterAll = () => {};
globalThis.expect = expect;

expect.extend({
  toBeInTheDocument(received) {
    const pass = received != null && received.ownerDocument?.contains(received);
    return {
      pass,
      message: () =>
        pass ? "expected element not to be in the document" : "expected element to be in the document",
    };
  },
  toHaveAttribute(received, attributeName, expectedValue) {
    if (!received || typeof received.getAttribute !== "function") {
      return {
        pass: false,
        message: () => "received value is not an element",
      };
    }

    const actual = received.getAttribute(attributeName);
    const pass = expectedValue === undefined ? actual !== null : actual === String(expectedValue);

    return {
      pass,
      message: () =>
        pass
          ? `expected element not to have attribute ${attributeName}`
          : `expected ${attributeName} to be ${expectedValue}, got ${actual}`,
    };
  },
});

const createMatchMedia = (query) => ({
  matches: false,
  media: query,
  onchange: null,
  addListener: fn(),
  removeListener: fn(),
  addEventListener: fn(),
  removeEventListener: fn(),
  dispatchEvent: fn(),
});

Object.defineProperty(window, "matchMedia", {
  writable: true,
  value: fn().mockImplementation(createMatchMedia),
});

class ResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}

Object.defineProperty(window, "ResizeObserver", {
  writable: true,
  value: ResizeObserver,
});

Object.defineProperty(globalThis, "ResizeObserver", {
  writable: true,
  value: ResizeObserver,
});

(async () => {
  const results = { passed: 0, failed: 0 };
  const testFiles = walkFiles(tempSrcRoot)
    .filter((file) => file.endsWith(".test.js"))
    .sort();

  for (const file of testFiles) {
    rootSuite = createSuite("");
    suiteStack = [rootSuite];
    activeTestFile = file;
    require(file);
    activeTestFile = null;
    await runSuite(rootSuite, [], results);
    clearTempModuleCache();
    mockRegistry.delete(file);
  }

  console.log(`\nTest Suites: ${results.failed ? "failed" : "passed"}`);
  console.log(`Tests: ${results.passed} passed, ${results.failed} failed`);
  if (results.failed > 0) {
    process.exitCode = 1;
  }
  process.exit(process.exitCode || 0);
})().catch((error) => {
  console.error(error && error.stack ? error.stack : error);
  process.exit(1);
});
