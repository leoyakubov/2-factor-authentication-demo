import { getCurrentUser, login } from "./ApiUtil";

describe("ApiUtil", () => {
  beforeEach(() => {
    global.fetch = jest.fn();
    localStorage.clear();
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  test("login posts credentials to the signin endpoint", async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      status: 200,
      text: async () =>
        JSON.stringify({ accessToken: "token-123", mfa: false }),
    });

    const response = await login({ username: "demo", password: "secret" });

    expect(fetch).toHaveBeenCalledWith(
      "http://localhost:8081/signin",
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({ username: "demo", password: "secret" }),
      })
    );
    expect(response).toEqual({ accessToken: "token-123", mfa: false });
  });

  test("getCurrentUser rejects when there is no access token", async () => {
    await expect(getCurrentUser()).rejects.toThrow("No access token set.");
  });
});
