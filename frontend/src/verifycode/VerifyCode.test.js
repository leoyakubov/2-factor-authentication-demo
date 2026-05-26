import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { notification } from "antd";
import VerifyCode from "./VerifyCode";
import { getCurrentUser, verify } from "../util/ApiUtil";
import { AuthProvider } from "../auth/AuthContext";

jest.mock("../util/ApiUtil", () => ({
  verify: jest.fn(),
  getCurrentUser: jest.fn(),
}));

jest.mock("react-router-dom", () => {
  const actual = jest.requireActual("react-router-dom");

  return {
    ...actual,
    Redirect: ({ to }) => {
      const target = typeof to === "string" ? { pathname: to } : to;

      return (
        <div
          data-testid="redirect"
          data-path={target.pathname}
          data-state={JSON.stringify(target.state || {})}
        />
      );
    },
  };
});

describe("VerifyCode", () => {
  const history = {
    push: jest.fn(),
    replace: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    getCurrentUser.mockRejectedValue({ status: 401 });
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  const renderComponent = (state = { username: "demo" }) =>
    render(
      <AuthProvider>
        <MemoryRouter>
          <VerifyCode history={history} location={{ state }} />
        </MemoryRouter>
      </AuthProvider>
    );

  test("navigates home after a valid code", async () => {
    const user = userEvent.setup();
    verify.mockResolvedValueOnce({ mfa: false });

    renderComponent();

    await screen.findByPlaceholderText("Enter code");

    await user.type(screen.getByPlaceholderText("Enter code"), "123456");
    await user.click(screen.getByRole("button", { name: /verify/i }));

    await waitFor(() =>
      expect(verify).toHaveBeenCalledWith({
        username: "demo",
        code: "123456",
      })
    );
    expect(history.push).toHaveBeenCalledWith("/");
  });

  test("shows a friendly error for an invalid code", async () => {
    const user = userEvent.setup();
    jest.spyOn(notification, "error").mockImplementation(() => {});
    verify.mockRejectedValueOnce({ status: 400 });

    renderComponent();

    await screen.findByPlaceholderText("Enter code");

    await user.type(screen.getByPlaceholderText("Enter code"), "000000");
    await user.click(screen.getByRole("button", { name: /verify/i }));

    expect(
      await screen.findByText(
        "The verification code is incorrect. Please try again."
      )
    ).toBeInTheDocument();
  });

  test("redirects to login when the username is missing", () => {
    renderComponent({});

    return waitFor(() => {
      expect(screen.getByTestId("redirect")).toHaveAttribute("data-path", "/login");
      expect(getCurrentUser).toHaveBeenCalled();
    });
  });
});
