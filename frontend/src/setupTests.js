// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import "@testing-library/jest-dom";

const createMatchMedia = (query) => {
  const mediaQueryList = {
    matches: false,
    media: query,
    onchange: null,
    addListener: (listener) => listener(mediaQueryList),
    removeListener: jest.fn(),
    addEventListener: (_eventName, listener) => listener(mediaQueryList),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  };

  return mediaQueryList;
};

Object.defineProperty(window, "matchMedia", {
  writable: true,
  value: jest.fn().mockImplementation(createMatchMedia),
});
