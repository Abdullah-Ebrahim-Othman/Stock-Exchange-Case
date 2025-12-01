"use client"
import {useState} from "react"
import {
	Dialog,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
	DialogTrigger
} from "@/components/ui/dialog"
import {Button} from "@/components/ui/button"
import {Input} from "@/components/ui/input"
import {Label} from "@/components/ui/label"
import {Textarea} from "@/components/ui/textarea"
import {Plus} from "lucide-react"
import {createStock} from "@/lib/api"

import {StockFormData, validateStockForm} from "@/lib/validation/stockValidation"

interface CreateStockModalProps {
	onStockCreated: () => void
}

export function CreateStockModal({onStockCreated}: CreateStockModalProps) {
	const [open, setOpen] = useState(false)
	const [isSubmitting, setIsSubmitting] = useState(false)
	const [errors, setErrors] = useState<{
		name?: string
		description?: string
		currentPrice?: string
		general?: string
	}>({})

	const [formData, setFormData] = useState<StockFormData>({
		name: "",
		description: "",
		currentPrice: ""
	})

	const validateForm = () => {
		const { isValid, errors: validationErrors } = validateStockForm(formData);
		setErrors(validationErrors);
		return isValid;
	}

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault()

		if (!validateForm()) {
			return
		}

		setIsSubmitting(true)
		setErrors({})

		try {
			const stockData = {
				name: formData.name.trim(),
				description: formData.description.trim(),
				currentPrice: parseFloat(formData.currentPrice)
			}
			const createdStock = await createStock(stockData);

			if (!createdStock) {
				throw new Error('Failed to create stock: No data returned');
			}

			// Reset form and close modal
			setFormData({name: "", description: "", currentPrice: ""});
			setOpen(false);

			// Notify parent component to refresh the table
			onStockCreated();
		} catch (error: any) {
			console.error("Error creating stock:", error)

			// Handle validation errors from backend
			if (error.response?.data?.errors) {
				const backendErrors: typeof errors = {}
				error.response.data.errors.forEach((err: any) => {
					backendErrors[err.field as keyof typeof errors] = err.message
				})
				setErrors(backendErrors)
			} else {
				setErrors({
					general: error.response?.data?.message || "Failed to create stock. Please try again."
				})
			}
		} finally {
			setIsSubmitting(false)
		}
	}

	const handleInputChange = (field: keyof typeof formData, value: string) => {
		setFormData(prev => ({...prev, [field]: value}))
		// Clear error for this field when user starts typing
		if (errors[field]) {
			setErrors(prev => ({...prev, [field]: undefined}))
		}
	}

	const handleOpenChange = (newOpen: boolean) => {
		if (!newOpen && !isSubmitting) {
			// Only reset form when closing and not submitting
			setFormData({name: "", description: "", currentPrice: ""})
			setErrors({})
		}
		setOpen(newOpen)
	}

	return (
		<Dialog open={open} onOpenChange={handleOpenChange}>
			<DialogTrigger asChild>
				<Button className="gap-2">
					<Plus className="h-4 w-4"/>
					Create Stock
				</Button>
			</DialogTrigger>
			<DialogContent className="sm:max-w-[500px]">
				<form onSubmit={handleSubmit}>
					<DialogHeader>
						<DialogTitle>Create New Stock</DialogTitle>
						<DialogDescription>
							Add a new stock to your portfolio. Fill in all the required information below.
						</DialogDescription>
					</DialogHeader>

					<div className="grid gap-4 py-4">
						{errors.general && (
							<div className="rounded-md bg-destructive/15 p-3 text-sm text-destructive">
								{errors.general}
							</div>
						)}

						<div className="grid gap-2">
							<Label htmlFor="name">
								Stock Name <span className="text-destructive">*</span>
							</Label>
							<Input
								id="name"
								placeholder="e.g., Apple Inc."
								value={formData.name}
								onChange={(e) => handleInputChange("name", e.target.value)}
								maxLength={30}
								className={errors.name ? "border-destructive" : ""}
							/>
							{errors.name && (
								<p className="text-sm text-destructive">{errors.name}</p>
							)}
							<p className="text-xs text-muted-foreground">
								{formData.name.length}/30 characters
							</p>
						</div>

						<div className="grid gap-2">
							<Label htmlFor="description">
								Description <span className="text-destructive">*</span>
							</Label>
							<Textarea
								id="description"
								placeholder="Brief description of the stock"
								value={formData.description}
								onChange={(e) => handleInputChange("description", e.target.value)}
								maxLength={30}
								rows={3}
								className={errors.description ? "border-destructive" : ""}
							/>
							{errors.description && (
								<p className="text-sm text-destructive">{errors.description}</p>
							)}
							<p className="text-xs text-muted-foreground">
								{formData.description.length}/30 characters
							</p>
						</div>

						<div className="grid gap-2">
							<Label htmlFor="currentPrice">
								Current Price (USD) <span className="text-destructive">*</span>
							</Label>
							<div className="relative">
                <span className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground">
                  $
                </span>
								<Input
									id="currentPrice"
									type="number"
									step="0.01"
									min="0"
									placeholder="0.00"
									value={formData.currentPrice}
									onChange={(e) => handleInputChange("currentPrice", e.target.value)}
									className={`pl-7 ${errors.currentPrice ? "border-destructive" : ""}`}
								/>
							</div>
							{errors.currentPrice && (
								<p className="text-sm text-destructive">{errors.currentPrice}</p>
							)}
						</div>
					</div>

					<DialogFooter>
						<Button
							type="button"
							variant="outline"
							onClick={() => setOpen(false)}
							disabled={isSubmitting}
						>
							Cancel
						</Button>
						<Button type="submit" disabled={isSubmitting}>
							{isSubmitting ? (
								<>
									<div
										className="mr-2 h-4 w-4 animate-spin rounded-full border-2 border-background border-t-transparent"/>
									Creating...
								</>
							) : (
								"Create Stock"
							)}
						</Button>
					</DialogFooter>
				</form>
			</DialogContent>
		</Dialog>
	)
}