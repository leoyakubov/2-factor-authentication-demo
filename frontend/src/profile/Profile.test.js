import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import Profile from "./Profile";
import { getCurrentUser } from "../util/ApiUtil";
import { AuthProvider } from "../auth/AuthContext";

jest.mock("../util/ApiUtil", () => ({
  getCurrentUser: jest.fn(),
}));

describe("Profile", () => {
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
          <Profile history={history} />
        </MemoryRouter>
      </AuthProvider>
    );

  test("redirects to login when there is no access token", async () => {
    renderComponent();

    await waitFor(() => {
      expect(history.push).toHaveBeenCalledWith("/login");
    });
  });

  test("renders the current user and logs out cleanly", async () => {
    const user = userEvent.setup();
    sessionStorage.setItem("accessToken", "token-123");
    getCurrentUser.mockResolvedValueOnce({
      name: "Galileo Fin",
      username: "galileo",
      profilePicture: undefined,
    });

    renderComponent();

    expect(await screen.findByText("Galileo Fin")).toBeInTheDocument();
    expect(screen.getByText("@galileo")).toBeInTheDocument();
    expect(screen.getByText("GF")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /logout/i }));

    expect(sessionStorage.getItem("accessToken")).toBeNull();
    expect(history.push).toHaveBeenCalledWith("/login");
  });

  test("clears the token when the current session is unauthorized", async () => {
    sessionStorage.setItem("accessToken", "token-123");
    getCurrentUser.mockRejectedValueOnce({ status: 401 });

    renderComponent();

    await waitFor(() => {
      expect(sessionStorage.getItem("accessToken")).toBeNull();
      expect(history.push).toHaveBeenCalledWith("/login");
    });
  });
});
