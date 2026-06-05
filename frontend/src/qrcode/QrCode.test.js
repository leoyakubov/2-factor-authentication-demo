import "../testSetup";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import QrCode from "./QrCode";

const mockNavigate = jest.fn();
let mockLocation = { state: {} };

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

describe("QrCode", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("renders the qr code and continues to login", async () => {
    const user = userEvent.setup();
    mockLocation = { state: { imageUrl: "data:image/png;base64,qr-code" } };

    render(
      <MemoryRouter>
        <QrCode />
      </MemoryRouter>
    );

    expect(
      screen.getByText("Scan the QR code using an authenticator app")
    ).toBeInTheDocument();
    expect(
      screen.getByAltText("Two-factor authentication QR code")
    ).toHaveAttribute("src", "data:image/png;base64,qr-code");

    await user.click(
      screen.getByRole("button", { name: /continue to login/i })
    );

    expect(mockNavigate).toHaveBeenCalledWith("/login", { replace: true });
  });

  test("redirects to signup when there is no qr image", () => {
    mockLocation = { state: {} };

    render(
      <MemoryRouter>
        <QrCode />
      </MemoryRouter>
    );

    expect(screen.getByTestId("redirect")).toHaveAttribute("data-path", "/signup");
  });
});
