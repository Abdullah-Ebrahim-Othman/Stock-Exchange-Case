'use client';

import {useState} from 'react';
import Link from 'next/link';
import {Button} from '@/components/ui/button';
import {Input} from '@/components/ui/input';
import {Card, CardContent, CardHeader, CardTitle} from '@/components/ui/card';

export interface LoginFormProps {
	onSubmit: (data: {
		email: string;
		password: string;
	}) => Promise<void>;
	loading: boolean;
}

export function LoginForm({onSubmit, loading}: LoginFormProps) {
	const [formData, setFormData] = useState({
		email: '',
		password: '',
	});

	const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
		const {name, value} = e.target;
		setFormData(prev => ({
			...prev,
			[name]: value
		}));

	}
	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();
		await onSubmit(formData);
	};

	return (
		<Card
			className="w-full max-w-md shadow-2xl backdrop-blur-xl bg-white/10 dark:bg-black/20 border border-white/10 dark:border-white/10">
			<CardHeader>
				<CardTitle className="text-center text-2xl font-bold text-slate-900 dark:text-white">
					Login
				</CardTitle>
			</CardHeader>
			<CardContent>
				<form onSubmit={handleSubmit} className="space-y-4">
					<div className="grid grid-cols-1 gap-4">
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


						<Button
							type="submit"
							disabled={loading}
							className="w-full py-2 text-lg rounded-xl bg-blue-600 hover:bg-blue-700 dark:bg-blue-500 dark:hover:bg-blue-600 disabled:opacity-50"
						>
							{loading ? "Logging in..." : "Login"}
						</Button>

						<div className="text-center mt-0">
							<Link 
								href="/register" 
								className="text-blue-600 hover:underline dark:text-blue-400 text-sm"
							>
								Don't have an account? Sign up
							</Link>
						</div>

						{/*<div className="text-center mt-0">*/}
						{/*	<Link */}
						{/*		href="/forgot-password" */}
						{/*		className="text-blue-600 hover:underline dark:text-blue-400 text-sm"*/}
						{/*	>*/}
						{/*		Forgot your password?*/}
						{/*	</Link>*/}
						{/*</div>*/}
					</div>
				</form>
			</CardContent>
		</Card>
	);
}
