export interface StockExchangeFormData {
  name: string;
  description: string;
}

export interface ValidationErrors {
  name?: string;
  description?: string;
  general?: string;
}

export const validateStockExchangeForm = (formData: StockExchangeFormData): { isValid: boolean; errors: ValidationErrors } => {
  const errors: ValidationErrors = {};

  // Name validation
  if (!formData.name.trim()) {
    errors.name = "Name is mandatory";
  } else if (formData.name.trim().length < 1 || formData.name.trim().length > 30) {
    errors.name = "Name must be between 1 and 30 characters";
  }

  // Description validation
  if (!formData.description.trim()) {
    errors.description = "Description is mandatory";
  } else if (formData.description.trim().length < 3 || formData.description.trim().length > 30) {
    errors.description = "Description must be between 3 and 30 characters";
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors
  };
};
