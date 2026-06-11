import "../testSetup";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { AuthProvider, useAuth } from "./AuthContext";
import { getCurrentUser, logout } from "../shared/api/apiClient";

jest.mock("../shared/api/apiClient", () => ({
  getCurrentUser: jest.fn(),
  logout: jest.fn(),
}));

function AuthStateProbe() {
  const auth = useAuth();

  return (
    <div>
      <span>{auth.isChecking ? "checking" : "ready"}</span>
      <span>{auth.isAuthenticated ? "authenticated" : "anonymous"}</span>
      <button onClick={() => auth.login()}>Login</button>
      <button onClick={() => auth.logout()}>Logout</button>
    </div>
  );
}

describe("AuthContext", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("marks the session as authenticated when the profile request succeeds", async () => {
    getCurrentUser.mockResolvedValueOnce({
      username: "demo",
      email: "demo@example.com",
    });

    render(
      <AuthProvider>
        <AuthStateProbe />
      </AuthProvider>
    );

    expect(await screen.findByText("ready")).toBeInTheDocument();
    expect(screen.getByText("authenticated")).toBeInTheDocument();
  });

  test("marks the session as anonymous when the profile request fails", async () => {
    getCurrentUser.mockRejectedValueOnce({ status: 401 });

    render(
      <AuthProvider>
        <AuthStateProbe />
      </AuthProvider>
    );

    expect(await screen.findByText("ready")).toBeInTheDocument();
    expect(screen.getByText("anonymous")).toBeInTheDocument();
  });

  test("clears the local session when logout fails", async () => {
    getCurrentUser.mockResolvedValueOnce({
      username: "demo",
      email: "demo@example.com",
    });
    logout.mockRejectedValueOnce({ status: 500 });

    render(
      <AuthProvider>
        <AuthStateProbe />
      </AuthProvider>
    );

    await screen.findByText("authenticated");
    await screen.findByText("ready");

    fireEvent.click(screen.getByRole("button", { name: /logout/i }));

    await waitFor(() => {
      expect(screen.getByText("anonymous")).toBeInTheDocument();
    });
  });

  test("ignores a stale session check that finishes after login", async () => {
    let resolveSessionCheck;
    const sessionCheck = new Promise((resolve, reject) => {
      resolveSessionCheck = () => reject({ status: 401 });
    });
    getCurrentUser.mockReturnValueOnce(sessionCheck);

    render(
      <AuthProvider>
        <AuthStateProbe />
      </AuthProvider>
    );

    await screen.findByText("checking");
    fireEvent.click(screen.getByRole("button", { name: /login/i }));

    resolveSessionCheck();

    await waitFor(() => {
      expect(screen.getByText("authenticated")).toBeInTheDocument();
    });
  });
});
