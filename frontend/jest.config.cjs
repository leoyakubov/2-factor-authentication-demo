module.exports = {
  testEnvironment: "jsdom",
  setupFilesAfterEnv: [require.resolve("./test/setupTests.cjs").replace(/\\/g, "/")],
  transform: {
    "^.+\\.[jt]sx?$": "babel-jest",
  },
  moduleNameMapper: {
    "\\.(css|less|sass|scss)$": "<rootDir>/test/styleMock.cjs",
  },
  testPathIgnorePatterns: ["/node_modules/", "/build/"],
};
