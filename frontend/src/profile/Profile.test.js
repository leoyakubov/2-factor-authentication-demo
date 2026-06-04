import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import Profile from "./Profile";
import { getCurrentUser, logout } from "../shared/api/apiClient";
import { AuthProvider } from "../auth/AuthContext";

jest.mock("../shared/api/apiClient", () => ({
  getCurrentUser: jest.fn(),
  logout: jest.fn(),
}));

const mockNavigate = jest.fn();

jest.mock("react-router-dom", () => {
  const actual = jest.requireActual("react-router-dom");

  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe("Profile", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderComponent = () =>
    render(
      <AuthProvider>
        <MemoryRouter>
          <Profile />
        </MemoryRouter>
      </AuthProvider>
    );

  test("redirects to login when the session check fails", async () => {
    getCurrentUser.mockRejectedValueOnce({ status: 401 });

    renderComponent();

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith("/login", { replace: true });
    });
  });

  test("renders the current user and logs out cleanly", async () => {
    const user = userEvent.setup();
    getCurrentUser
      .mockResolvedValueOnce({})
      .mockResolvedValueOnce({
        name: "Galileo Fin",
        username: "galileo",
        profilePicture: undefined,
      });
    logout.mockResolvedValueOnce({});

    renderComponent();

    expect(await screen.findByText("Galileo Fin")).toBeInTheDocument();
    expect(screen.getByText("@galileo")).toBeInTheDocument();
    expect(screen.getByText("GF")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /logout/i }));

    expect(logout).toHaveBeenCalled();
    expect(mockNavigate).toHaveBeenCalledWith("/login", { replace: true });
  });

  test("shows an error message when the profile request fails", async () => {
    getCurrentUser
      .mockResolvedValueOnce({})
      .mockRejectedValueOnce({ status: 500 });

    renderComponent();

    expect(await screen.findByText("Profile load failed")).toBeInTheDocument();
    expect(
      screen.getByText("We couldn't load your profile right now. Please try again.")
    ).toBeInTheDocument();
  });
});
