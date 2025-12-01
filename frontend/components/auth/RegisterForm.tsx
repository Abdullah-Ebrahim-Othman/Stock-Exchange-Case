'use client';

import { useState } from 'react';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

export interface RegisterFormProps {
  onSubmit: (data: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    confirmPassword: string;
  }) => Promise<void>;
  loading: boolean;
}

export function RegisterForm({ onSubmit, loading}: RegisterFormProps) {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [passwordError, setPasswordError] = useState<string | null>(null);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    // Validate passwords match on the fly
    if ((name === 'password' || name === 'confirmPassword') && formData.password && formData.confirmPassword) {
      if (name === 'password' && formData.confirmPassword !== value) {
        setPasswordError('Passwords do not match');
      } else if (name === 'confirmPassword' && formData.password !== value) {
        setPasswordError('Passwords do not match');
      } else {
        setPasswordError(null);
      }
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await onSubmit(formData);
  };

  return (
    <Card className="w-full max-w-md shadow-2xl backdrop-blur-xl bg-white/10 dark:bg-black/20 border border-white/10 dark:border-white/10">
      <CardHeader>
        <CardTitle className="text-center text-2xl font-bold text-slate-900 dark:text-white">
          Create an Account
        </CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-gray-300">First Name</label>
              <Input
                name="firstName"
                type="text"
                placeholder="First name"
                value={formData.firstName}
                onChange={handleChange}
                className="mt-1"
                required
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700 dark:text-gray-300">Last Name</label>
              <Input
                name="lastName"
                type="text"
                placeholder="Last name"
                value={formData.lastName}
                onChange={handleChange}
                className="mt-1"
                required
              />
            </div>
          </div>
          <div>
            <label className="text-sm font-medium text-slate-700 dark:text-gray-300">Email</label>
            <Input
              name="email"
              type="email"
              placeholder="Enter your email"
              value={formData.email}
              onChange={handleChange}
              className="mt-1"
              required
            />
          </div>
          <div>
            <label className="text-sm font-medium text-slate-700 dark:text-gray-300">Password</label>
            <Input
              name="password"
              type="password"
              placeholder="Enter your password"
              value={formData.password}
              onChange={handleChange}
              className="mt-1"
              required
            />
          </div>
          <div>
            <label className="text-sm font-medium text-slate-700 dark:text-gray-300">Confirm Password</label>
            <Input
              name="confirmPassword"
              type="password"
              placeholder="Confirm your password"
              value={formData.confirmPassword}
              onChange={handleChange}
              className={`mt-1 ${passwordError ? 'border-red-500' : ''}`}
              required
            />
            {passwordError && <p className="text-red-500 text-xs mt-1">{passwordError}</p>}
          </div>

          <Button
            type="submit"
            disabled={loading || !!passwordError}
            className="w-full py-2 text-lg rounded-xl bg-blue-600 hover:bg-blue-700 dark:bg-blue-500 dark:hover:bg-blue-600 disabled:opacity-50"
          >
            {loading ? "Creating account..." : "Register"}
          </Button>

          <div className="text-center mt-4">
            <span className="text-sm text-slate-600 dark:text-slate-300">
              Already have an account?{' '}
              <Link 
                href="/login" 
                className="text-blue-600 hover:underline dark:text-blue-400"
              >
                Sign in
              </Link>
            </span>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}
