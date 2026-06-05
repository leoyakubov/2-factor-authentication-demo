import "../testSetup";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { notification } from "antd";
import VerifyCode from "./VerifyCode";
import { getCurrentUser, verify } from "../shared/api/apiClient";
import { AuthProvider } from "../auth/AuthContext";

jest.mock("../shared/api/apiClient", () => ({
  verify: jest.fn(),
  getCurrentUser: jest.fn(),
}));

const mockNavigate = jest.fn();
let mockLocation = { state: { username: "demo" } };

jest.mock("react-router-dom", () => {
  const actual = jest.requireActual("react-router-dom");

  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useLocation: () => mockLocation,
    Navigate: ({ to }) => {
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
  beforeEach(() => {
    jest.clearAllMocks();
    getCurrentUser.mockRejectedValue({ status: 401 });
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  const renderComponent = () =>
    render(
      <AuthProvider>
        <MemoryRouter>
          <VerifyCode />
        </MemoryRouter>
      </AuthProvider>
    );

  test("navigates home after a valid code", async () => {
    const user = userEvent.setup();
    verify.mockResolvedValueOnce({ mfa: false });
    mockLocation = { state: { username: "demo" } };

    renderComponent();

    await screen.findByPlaceholderText("Enter code");
    expect(
      screen.getByText(
        "Enter the 6-digit code from your authenticator app or one of your recovery codes."
      )
    ).toBeInTheDocument();

    await user.type(screen.getByPlaceholderText("Enter code"), "123456");
    await user.click(screen.getByRole("button", { name: /verify/i }));

    await waitFor(() =>
      expect(verify).toHaveBeenCalledWith({
        username: "demo",
        code: "123456",
      })
    );
    expect(mockNavigate).toHaveBeenCalledWith("/", { replace: true });
  });

  test("shows a friendly error for an invalid code", async () => {
    const user = userEvent.setup();
    jest.spyOn(notification, "error").mockImplementation(() => {});
    verify.mockRejectedValueOnce({ status: 400 });
    mockLocation = { state: { username: "demo" } };

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
    mockLocation = { state: {} };
    renderComponent({});

    return waitFor(() => {
      expect(screen.getByTestId("redirect")).toHaveAttribute("data-path", "/login");
    });
  });
});
