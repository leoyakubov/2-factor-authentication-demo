import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import Signup from "./Signup";
import { getCurrentUser, signup } from "../shared/api/apiClient";
import { AuthProvider } from "../auth/AuthContext";

jest.mock("../shared/api/apiClient", () => ({
  signup: jest.fn(),
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

describe("Signup", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    getCurrentUser.mockRejectedValue({ status: 401 });
  });

  const fillForm = async (user) => {
    await user.type(screen.getByPlaceholderText("Name"), "Galileo Fin");
    await user.type(screen.getByPlaceholderText("Username"), "galileo");
    await user.type(screen.getByPlaceholderText("Email"), "galileo@example.com");
    await user.type(screen.getByPlaceholderText("Password"), "secret");
  };

  const renderComponent = () =>
    render(
      <AuthProvider>
        <MemoryRouter>
          <Signup />
        </MemoryRouter>
      </AuthProvider>
    );

  test("shows the success state and login button after a non-mfa signup", async () => {
    const user = userEvent.setup();
    signup.mockResolvedValueOnce({ mfa: false });

    renderComponent();
    await screen.findByPlaceholderText("Name");
    await fillForm(user);
    await user.click(screen.getByRole("button", { name: /signup/i }));

    expect(await screen.findByText("Account created")).toBeInTheDocument();
    expect(
      screen.getByText("Your account was created successfully. You can log in now.")
    ).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /login/i })).toBeInTheDocument();
    expect(screen.queryByAltText("Two-factor authentication QR code")).not.toBeInTheDocument();
  });

  test("shows the qr code after an mfa signup", async () => {
    const user = userEvent.setup();
    signup.mockResolvedValueOnce({
      mfa: true,
      secretImageUri: "data:image/png;base64,qr-code",
    });

    renderComponent();
    await screen.findByPlaceholderText("Name");
    await fillForm(user);
    await user.click(screen.getByRole("button", { name: /signup/i }));

    expect(await screen.findByText("Account created")).toBeInTheDocument();
    expect(
      screen.getByText("Scan this QR code with your authenticator app before logging in.")
    ).toBeInTheDocument();
    expect(
      screen.getByAltText("Two-factor authentication QR code")
    ).toHaveAttribute("src", "data:image/png;base64,qr-code");
  });

  test("navigates to login from the success screen", async () => {
    const user = userEvent.setup();
    signup.mockResolvedValueOnce({ mfa: false });

    renderComponent();
    await screen.findByPlaceholderText("Name");
    await fillForm(user);
    await user.click(screen.getByRole("button", { name: /signup/i }));

    await screen.findByText("Account created");
    await user.click(screen.getByRole("button", { name: /login/i }));

    expect(mockNavigate).toHaveBeenCalledWith("/login");
  });

  test("shows a friendly error when the account already exists", async () => {
    const user = userEvent.setup();
    signup.mockRejectedValueOnce({
      status: 400,
      body: { message: "username exists" },
    });

    renderComponent();
    await screen.findByPlaceholderText("Name");
    await fillForm(user);
    await user.click(screen.getByRole("button", { name: /signup/i }));

    expect(
      await screen.findByText(
        "That username or email is already in use. Try another one."
      )
    ).toBeInTheDocument();
  });

  test("shows field-specific validation errors from the backend", async () => {
    const user = userEvent.setup();
    signup.mockRejectedValueOnce({
      status: 400,
      body: {
        message: "Please review the highlighted fields and try again.",
        errors: {
          password: "Your password must be between 6 and 20 characters long.",
        },
      },
    });

    renderComponent();
    await screen.findByPlaceholderText("Name");
    await fillForm(user);
    await user.clear(screen.getByPlaceholderText("Password"));
    await user.type(screen.getByPlaceholderText("Password"), "user2");
    await user.click(screen.getByRole("button", { name: /signup/i }));

    expect(
      await screen.findByText(
        "Your password must be between 6 and 20 characters long."
      )
    ).toBeInTheDocument();
    expect(
      await screen.findByText("Please review the highlighted fields and try again.")
    ).toBeInTheDocument();
  });

  test("shows a generic backend error for unexpected failures", async () => {
    const user = userEvent.setup();
    signup.mockRejectedValueOnce({ status: 500 });

    renderComponent();
    await screen.findByPlaceholderText("Name");
    await fillForm(user);
    await user.click(screen.getByRole("button", { name: /signup/i }));

    expect(
      await screen.findByText(
        "We couldn't create your account right now. Please try again."
      )
    ).toBeInTheDocument();
  });
});
