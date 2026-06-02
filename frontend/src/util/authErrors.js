export function getSignInErrorMessage(error) {
  if (error.status === 401) {
    return "We couldn't log you in. Check your username or email and password.";
  }

  if (error.status === 404) {
    return "We couldn't find an account with that username or email.";
  }

  if (error.status === 400) {
    return "Please check your login details and try again.";
  }

  if (error.status === 429) {
    return "Too many login attempts were made. Please wait a moment and try again.";
  }

  return "We couldn't reach the server. Please try again in a moment.";
}

export function getSignUpErrorMessage(error) {
  if (error.status === 400) {
    return error.body?.message?.includes("exists")
      ? "That username or email is already in use. Try another one."
      : "Please check the form fields and try again.";
  }

  if (error.status === 429) {
    return "Too many sign-up attempts were made. Please wait a moment and try again.";
  }

  return "We couldn't create your account right now. Please try again.";
}

export function getVerifyErrorMessage(error) {
  if (error.status === 400) {
    return "The verification code is incorrect. Please try again.";
  }

  if (error.status === 429) {
    return "Too many verification attempts were made. Please wait a moment and try again.";
  }

  return "We couldn't verify the code right now. Please try again.";
}

export function getProfileErrorMessage(error) {
  return error.status === 401
    ? "Your session expired. Please log in again."
    : "We couldn't load your profile right now. Please try again.";
}
