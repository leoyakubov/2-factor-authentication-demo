import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import Signin from "./Signin";
import { login } from "../util/ApiUtil";
import { AuthProvider } from "../auth/AuthContext";

jest.mock("../util/ApiUtil", () => ({
  login: jest.fn(),
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

describe("Signin", () => {
  const history = {
    push: jest.fn(),
  };

  beforeEach(() => {
    sessionStorage.clear();
    jest.clearAllMocks();
  });

  const renderComponent = () =>
    render(
      <AuthProvider>
        <MemoryRouter>
          <Signin history={history} />
        </MemoryRouter>
      </AuthProvider>
    );

  test("logs in and stores the token for a regular account", async () => {
    const user = userEvent.setup();
    login.mockResolvedValueOnce({ accessToken: "token-123", mfa: false });

    renderComponent();

    await user.type(
      screen.getByPlaceholderText("Username or email"),
      "demo"
    );
    await user.type(screen.getByPlaceholderText("Password"), "secret");
    await user.click(screen.getByRole("button", { name: /log in/i }));

    await waitFor(() =>
      expect(login).toHaveBeenCalledWith({
        username: "demo",
        password: "secret",
      })
    );
    expect(sessionStorage.getItem("accessToken")).toBe("token-123");
    expect(history.push).toHaveBeenCalledWith("/");
  });

  test("redirects to verify when mfa is required", async () => {
    const user = userEvent.setup();
    login.mockResolvedValueOnce({ mfa: true });

    renderComponent();

    await user.type(
      screen.getByPlaceholderText("Username or email"),
      "demo"
    );
    await user.type(screen.getByPlaceholderText("Password"), "secret");
    await user.click(screen.getByRole("button", { name: /log in/i }));

    await waitFor(() =>
      expect(screen.getByTestId("redirect")).toHaveAttribute("data-path", "/verify")
    );
    expect(screen.getByTestId("redirect")).toHaveAttribute(
      "data-state",
      JSON.stringify({ username: "demo" })
    );
  });

  test("shows a friendly message for invalid credentials", async () => {
    const user = userEvent.setup();
    login.mockRejectedValueOnce({ status: 401 });

    renderComponent();

    await user.type(
      screen.getByPlaceholderText("Username or email"),
      "demo"
    );
    await user.type(screen.getByPlaceholderText("Password"), "wrong");
    await user.click(screen.getByRole("button", { name: /log in/i }));

    expect(
      await screen.findByText(
        "We couldn't log you in. Check your username or email and password."
      )
    ).toBeInTheDocument();
  });

  test("shows a friendly message when the account cannot be found", async () => {
    const user = userEvent.setup();
    login.mockRejectedValueOnce({ status: 404 });

    renderComponent();

    await user.type(
      screen.getByPlaceholderText("Username or email"),
      "missing-user"
    );
    await user.type(screen.getByPlaceholderText("Password"), "secret");
    await user.click(screen.getByRole("button", { name: /log in/i }));

    expect(
      await screen.findByText(
        "We couldn't find an account with that username or email."
      )
    ).toBeInTheDocument();
  });
});
