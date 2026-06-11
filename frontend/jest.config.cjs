const os = require("node:os");
const path = require("node:path");

module.exports = {
  testEnvironment: "jsdom",
  testMatch: ["<rootDir>/src/**/*.test.js"],
  transform: {
    "^.+\\.[jt]sx?$": "babel-jest",
  },
  moduleNameMapper: {
    "^antd$": "<rootDir>/test/mocks/antd.cjs",
    "^.+\\.(css|less|sass|scss)$": "<rootDir>/test/styleMock.cjs",
  },
  collectCoverageFrom: [
    "src/**/*.{js,jsx}",
    "!src/**/index.js",
    "!src/testSetup.js",
    "!src/main.jsx",
  ],
  coverageDirectory: path.join(os.tmpdir(), "two-factor-authentication-demo-frontend-coverage"),
  coverageProvider: "v8",
  coverageReporters: ["text", "lcov", "html"],
};
