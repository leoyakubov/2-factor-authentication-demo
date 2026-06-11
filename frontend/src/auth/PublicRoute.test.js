import "../testSetup";
import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import PublicRoute from "./PublicRoute";

const mockUseAuth = jest.fn();

jest.mock("./AuthContext", () => ({
  useAuth: () => mockUseAuth(),
}));

describe("PublicRoute", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderComponent = () =>
    render(
      <MemoryRouter initialEntries={["/login"]}>
        <Routes>
          <Route
            path="/login"
            element={
              <PublicRoute>
                <div>Login form</div>
              </PublicRoute>
            }
          />
          <Route path="/" element={<div>Profile page</div>} />
        </Routes>
      </MemoryRouter>
    );

  test("renders the public content when the session is not authenticated", () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isChecking: false,
    });

    renderComponent();

    expect(screen.getByText("Login form")).toBeInTheDocument();
  });

  test("redirects to the profile page when the session is already authenticated", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isChecking: false,
    });

    renderComponent();

    expect(await screen.findByText("Profile page")).toBeInTheDocument();
    expect(screen.queryByText("Login form")).not.toBeInTheDocument();
  });
});
