export interface StockFormData {
  name: string;
  description: string;
  currentPrice: string;
}

export interface ValidationErrors {
  name?: string;
  description?: string;
  currentPrice?: string;
  general?: string;
}

export const validateStockForm = (formData: StockFormData): { isValid: boolean; errors: ValidationErrors } => {
  const errors: ValidationErrors = {};

  // Name validation
  if (!formData.name.trim()) {
    errors.name = "Name is mandatory";
  } else if (formData.name.length < 1 || formData.name.length > 30) {
    errors.name = "Name must be between 1 and 30 characters";
  }

  // Description validation
  if (!formData.description.trim()) {
    errors.description = "Description is mandatory";
  } else if (formData.description.length < 1 || formData.description.length > 30) {
    errors.description = "Description must be between 1 and 30 characters";
  }

  // Price validation
  if (!formData.currentPrice) {
    errors.currentPrice = "Price is mandatory";
  } else {
    const price = parseFloat(formData.currentPrice);
    if (isNaN(price)) {
      errors.currentPrice = "Price must be a valid number";
    } else if (price < 0) {
      errors.currentPrice = "Price must be equal to or greater than zero";
    }
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors
  };
};
