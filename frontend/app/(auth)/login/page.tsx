"use client";

import { useState } from "react";
import { validateRequiredFields } from "@/lib/validators";
import { loginAction } from "@/app/(auth)/login/actions";
import { toast } from "react-hot-toast";
import { LoginForm } from "@/components/auth/LoginForm";

export default function LoginPage() {
  const [loading, setLoading] = useState(false);
  const handleLogin = async (formData: { email: string; password: string }) => {
    try {
      const requiredFields = {
        Email: formData.email,
        Password: formData.password,
      };

      const requiredValidation = validateRequiredFields(requiredFields);
      if (!requiredValidation.isValid) {
        toast.error(requiredValidation.message);
        return;
      }

      setLoading(true);
      const result = await loginAction({
        email: formData.email,
        password: formData.password,
      });

      console.log(result)

      if (result.type === "success") {
        toast.success(result.message || "Login successful!");
        // Redirect to dashboard after 2 seconds
        setTimeout(() => {
          window.location.href = '/dashboard';
        }, 2000);
      } else {
        toast.error(result.message || "Login failed. Please try again.");
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : "An unexpected error occurred";
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-gradient-to-br
    from-slate-200 via-slate-200 to-slate-400
    dark:from-black dark:via-neutral-900 dark:to-neutral-500 transition-colors duration-300">
      <LoginForm onSubmit={handleLogin} loading={loading} />
    </div>
  );
}
