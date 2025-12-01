"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { toast } from "react-hot-toast";
import { RegisterForm } from "@/components/auth/RegisterForm";
import { validateRequiredFields, validatePasswords } from "@/lib/validators";
import { registerAction } from "./actions";

export default function RegisterPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);

  const handleRegister = async (formData: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    confirmPassword: string;
  }) => {
    try {
      const requiredFields = {
        'First Name': formData.firstName,
        'Last Name': formData.lastName,
        'Email': formData.email,
        'Password': formData.password,
        'Confirm Password': formData.confirmPassword
      };

      const requiredValidation = validateRequiredFields(requiredFields);
      if (!requiredValidation.isValid) {
        throw new Error(requiredValidation.message);
      }

      const passwordValidation = validatePasswords(formData.password, formData.confirmPassword);
      if (!passwordValidation.isValid) {
        throw new Error(passwordValidation.message);
      }

      setLoading(true);
      
      const result = await registerAction({
        firstName: formData.firstName,
        lastName: formData.lastName,
        email: formData.email,
        password: formData.password
      });

      if (result.type === 'success') {
        toast.success(result.message || 'Registration successful!');
        setTimeout(() => {
          router.push('/login');
        }, 1500);
      } else {
        throw new Error(result.message);
      }
    } catch (error) {
      console.error('Registration error:', error);
      const errorMessage = error instanceof Error ? error.message : 'An unexpected error occurred';
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-gradient-to-br
     from-slate-100 via-slate-200 to-slate-300
     dark:from-black dark:via-neutral-900 dark:to-neutral-800
      transition-colors duration-300">
      <RegisterForm
        onSubmit={handleRegister} 
        loading={loading} 
      />
    </div>
  );

}
