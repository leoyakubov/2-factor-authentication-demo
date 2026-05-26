import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import Signup from "./Signup";
import { signup } from "../util/ApiUtil";
import { AuthProvider } from "../auth/AuthContext";

jest.mock("../util/ApiUtil", () => ({
  signup: jest.fn(),
}));

describe("Signup", () => {
  const history = {
    push: jest.fn(),
  };

  beforeEach(() => {
    sessionStorage.clear();
    jest.clearAllMocks();
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
          <Signup history={history} />
        </MemoryRouter>
      </AuthProvider>
    );

  test("shows the success state and login button after a non-mfa signup", async () => {
    const user = userEvent.setup();
    signup.mockResolvedValueOnce({ mfa: false });

    renderComponent();
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

  test("shows a friendly error when the account already exists", async () => {
    const user = userEvent.setup();
    signup.mockRejectedValueOnce({
      status: 400,
      body: { message: "username exists" },
    });

    renderComponent();
    await fillForm(user);
    await user.click(screen.getByRole("button", { name: /signup/i }));

    expect(
      await screen.findByText(
        "That username or email is already in use. Try another one."
      )
    ).toBeInTheDocument();
  });

  test("shows a generic backend error for unexpected failures", async () => {
    const user = userEvent.setup();
    signup.mockRejectedValueOnce({ status: 500 });

    renderComponent();
    await fillForm(user);
    await user.click(screen.getByRole("button", { name: /signup/i }));

    expect(
      await screen.findByText(
        "We couldn't create your account right now. Please try again."
      )
    ).toBeInTheDocument();
  });
});
