import "../testSetup";
import { render, screen } from "@testing-library/react";
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

  test("renders the current user and logs out cleanly", async () => {
    const user = userEvent.setup();
    getCurrentUser
      .mockResolvedValueOnce({
        name: "Galileo Fin",
        username: "galileo",
        email: "galileo.fin@example.com",
        mfaEnabled: true,
        profilePicture: undefined,
      });
    logout.mockResolvedValueOnce({});

    renderComponent();

    expect(await screen.findByRole("heading", { name: "Galileo Fin" })).toBeInTheDocument();
    expect(getCurrentUser).toHaveBeenCalledTimes(1);
    expect(screen.getByText("@galileo", { selector: ".profile-handle" })).toBeInTheDocument();
    expect(
      screen.getByText("galileo.fin@example.com", { selector: ".profile-email" })
    ).toBeInTheDocument();
    expect(
      screen.getAllByText("Enabled", { selector: ".profile-detail-value, .mfa-status-tag" }).length
    ).toBeGreaterThan(0);
    expect(screen.getByText("GF")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /logout/i }));

    expect(logout).toHaveBeenCalled();
    expect(mockNavigate).toHaveBeenCalledWith("/login", { replace: true });
  });
});
