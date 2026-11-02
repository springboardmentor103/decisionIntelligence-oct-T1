export const validateEmail = (email) => /\S+@\S+\.\S+/.test(String(email).trim())

export const validateName = (name) => String(name).trim().length >= 2

export const validatePassword = (password) => String(password).length >= 6

export const validatePasswordsMatch = (password, confirmPassword) =>
  String(password).length > 0 && String(password) === String(confirmPassword)
