import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import Signin from "./Signin";
import { getCurrentUser, login } from "../shared/api/apiClient";
import { AuthProvider } from "../auth/AuthContext";

jest.mock("../shared/api/apiClient", () => ({
  login: jest.fn(),
  getCurrentUser: jest.fn(),
}));

const mockNavigate = jest.fn();

jest.mock("react-router-dom", () => {
  const actual = jest.requireActual("react-router-dom");

  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe("Signin", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    getCurrentUser.mockRejectedValue({ status: 401 });
  });

  const renderComponent = () =>
    render(
      <AuthProvider>
        <MemoryRouter>
          <Signin />
        </MemoryRouter>
      </AuthProvider>
    );

  test("logs in and navigates home for a regular account", async () => {
    const user = userEvent.setup();
    login.mockResolvedValueOnce({ mfa: false });

    renderComponent();

    await screen.findByPlaceholderText("Username or email");

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
    expect(mockNavigate).toHaveBeenCalledWith("/", { replace: true });
  });

  test("redirects to verify when mfa is required", async () => {
    const user = userEvent.setup();
    login.mockResolvedValueOnce({ mfa: true });

    renderComponent();

    await screen.findByPlaceholderText("Username or email");

    await user.type(
      screen.getByPlaceholderText("Username or email"),
      "demo"
    );
    await user.type(screen.getByPlaceholderText("Password"), "secret");
    await user.click(screen.getByRole("button", { name: /log in/i }));

    await waitFor(() =>
      expect(mockNavigate).toHaveBeenCalledWith("/verify", {
        state: { username: "demo" },
      })
    );
  });

  test("shows a friendly message for invalid credentials", async () => {
    const user = userEvent.setup();
    login.mockRejectedValueOnce({ status: 401 });

    renderComponent();

    await screen.findByPlaceholderText("Username or email");

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

    await screen.findByPlaceholderText("Username or email");

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
