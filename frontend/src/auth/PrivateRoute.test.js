import "../testSetup";
import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import PrivateRoute from "./PrivateRoute";

const mockUseAuth = jest.fn();

jest.mock("./AuthContext", () => ({
  useAuth: () => mockUseAuth(),
}));

describe("PrivateRoute", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderComponent = () =>
    render(
      <MemoryRouter initialEntries={["/profile"]}>
        <Routes>
          <Route
            path="/profile"
            element={
              <PrivateRoute>
                <div>Secret profile area</div>
              </PrivateRoute>
            }
          />
          <Route path="/login" element={<div>Login page</div>} />
        </Routes>
      </MemoryRouter>
    );

  test("renders the protected content when the session is authenticated", () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isChecking: false,
    });

    renderComponent();

    expect(screen.getByText("Secret profile area")).toBeInTheDocument();
  });

  test("redirects to login when the session is not authenticated", async () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isChecking: false,
    });

    renderComponent();

    expect(await screen.findByText("Login page")).toBeInTheDocument();
    expect(screen.queryByText("Secret profile area")).not.toBeInTheDocument();
  });
});
