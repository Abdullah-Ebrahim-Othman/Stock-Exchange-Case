interface ValidationResult {
  isValid: boolean;
  message: string;
}

export const validateRequiredFields = (fields: Record<string, string>): ValidationResult => {
  for (const [key, value] of Object.entries(fields)) {
    if (!value.trim()) {
      return {
        isValid: false,
        message: `${key.charAt(0).toUpperCase() + key.slice(1)} is required`
      };
    }
  }
  return { isValid: true, message: '' };
};

export const validatePasswords = (password: string, confirmPassword: string): ValidationResult => {
  if (password !== confirmPassword) {
    return {
      isValid: false,
      message: 'Passwords do not match'
    };
  }
  
  // Add more password validation rules here if needed
  if (password.length < 3) {
    return {
      isValid: false,
      message: 'Password must be at least 8 characters long'
    };
  }

  return { isValid: true, message: '' };
};
