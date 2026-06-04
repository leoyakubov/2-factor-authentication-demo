module.exports = {
  testEnvironment: "jsdom",
  setupFilesAfterEnv: [require.resolve("./test/setupTests.cjs").replace(/\\/g, "/")],
  transform: {
    "^.+\\.[jt]sx?$": "babel-jest",
  },
  moduleNameMapper: {
    "^antd$": "<rootDir>/test/mocks/antd.cjs",
    "\\.(css|less|sass|scss)$": "<rootDir>/test/styleMock.cjs",
  },
  testPathIgnorePatterns: ["/node_modules/", "/dist/"],
};
