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
import {createStockExchange} from "@/lib/api"
import {StockExchangeFormData, ValidationErrors, validateStockExchangeForm} from "@/lib/validation/stockExchangeValidation"

interface CreateStockExchangeModalProps {
	onExchangeCreated: () => void
}

export function CreateStockExchangeModal({onExchangeCreated}: CreateStockExchangeModalProps) {
	const [open, setOpen] = useState(false)
	const [isSubmitting, setIsSubmitting] = useState(false)
	const [errors, setErrors] = useState<ValidationErrors>({})

	const [formData, setFormData] = useState<StockExchangeFormData>({
		name: "",
		description: ""
	})

	const validateForm = () => {
		const {isValid, errors: validationErrors} = validateStockExchangeForm(formData);
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
			const exchangeData = {
				name: formData.name.trim(),
				description: formData.description.trim()
			}

			const createdStockExchange = await createStockExchange(exchangeData);


			if (!createdStockExchange) {
				throw new Error('Failed to create stock: No data returned');
			}

			// Reset form and close modal
			setFormData({name: "", description: ""});
			setOpen(false);

			// Notify parent component to refresh the table
			onExchangeCreated();
		} catch (error: any) {
			console.error("Error creating stock exchange:", error)
			setErrors({
				general: error.message || "Failed to create stock exchange. Please try again."
			});
		} finally {
			setIsSubmitting(false)
		}
	}

	const handleInputChange = (field: keyof typeof formData, value: string) => {
		setFormData(prev => ({...prev, [field]: value}))
		// Clear error for this field when user starts typing
		if (errors[field as keyof typeof errors]) {
			setErrors(prev => ({...prev, [field]: undefined}))
		}
	}

	const handleOpenChange = (newOpen: boolean) => {
		if (!newOpen && !isSubmitting) {
			// Only reset form when closing and not submitting
			setFormData({name: "", description: ""})
			setErrors({})
		}
		setOpen(newOpen)
	}

	return (
		<Dialog open={open} onOpenChange={handleOpenChange}>
			<DialogTrigger asChild>
				<Button className="gap-2">
					<Plus className="h-4 w-4"/>
					Create Stock Exchange
				</Button>
			</DialogTrigger>
			<DialogContent className="sm:max-w-[500px]">
				<form onSubmit={handleSubmit}>
					<DialogHeader>
						<DialogTitle>Create New Stock Exchange</DialogTitle>
						<DialogDescription>
							Add a new stock exchange to the system. Fill in all the required information below.
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
								Exchange Name <span className="text-destructive">*</span>
							</Label>
							<Input
								id="name"
								placeholder="e.g., New York Stock Exchange"
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
								placeholder="Brief description of the stock exchange"
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
					</div>

					<DialogFooter>
						<Button
							type="button"
							variant="outline"
							onClick={() => handleOpenChange(false)}
							disabled={isSubmitting}
						>
							Cancel
						</Button>
						<Button type="submit" disabled={isSubmitting}>
							{isSubmitting ? "Creating..." : "Create Exchange"}
						</Button>
					</DialogFooter>
				</form>
			</DialogContent>
		</Dialog>
	)
}
