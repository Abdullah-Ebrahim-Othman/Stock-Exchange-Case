"use client"

import { useState } from 'react';
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Stock } from "@/types/Stock"
import { updateStock } from "@/lib/api"
import { toast } from 'react-hot-toast';
import { Pencil } from "lucide-react";

interface UpdateStockModalProps {
  stock: Stock;
  onStockUpdated: () => void;
  children?: React.ReactNode;
}

export function UpdateStockModal({ stock, onStockUpdated, children }: UpdateStockModalProps) {
  const [open, setOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [formData, setFormData] = useState({
    currentPrice: stock.currentPrice.toString()
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setErrors({});

    const price = parseFloat(formData.currentPrice);
    if (isNaN(price) || price <= 0) {
      setErrors({ currentPrice: 'Please enter a valid price greater than 0' });
      setIsSubmitting(false);
      return;
    }

    try {
      await updateStock(stock.stockId.toString(), {
        currentPrice: price
      });
      
      toast.success('Stock price updated successfully');
      setOpen(false);
      onStockUpdated();
    } catch (error: any) {
      console.error("Error updating stock price:", error);
      
      if (error.response?.data?.errors) {
        const backendErrors: Record<string, string> = {};
        error.response.data.errors.forEach((err: any) => {
          backendErrors[err.field] = err.message;
        });
        setErrors(backendErrors);
      } else {
        setErrors({
          general: error.response?.data?.message || "Failed to update stock price. Please try again."
        });
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    // Clear error for this field when user starts typing
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const handleOpenChange = (newOpen: boolean) => {
    if (!newOpen && !isSubmitting) {
      // Reset form when closing and not submitting
      setFormData({
        currentPrice: stock.currentPrice.toString()
      });
      setErrors({});
    }
    setOpen(newOpen);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        {children || (
          <Button variant="ghost" size="icon" title="Update price">
            <Pencil className="h-4 w-4" />
          </Button>
        )}
      </DialogTrigger>
      <DialogContent className="sm:max-w-[500px]">
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>Update Stock Price</DialogTitle>
            <DialogDescription>
              Update the price for {stock.name}
            </DialogDescription>
          </DialogHeader>

          <div className="grid gap-4 py-4">
            {errors.general && (
              <div className="rounded-md bg-destructive/15 p-3 text-sm text-destructive">
                {errors.general}
              </div>
            )}

            <div className="grid gap-2">
              <Label htmlFor="currentPrice">
                New Price (USD) <span className="text-destructive">*</span>
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
                  onChange={(e) => handleInputChange('currentPrice', e.target.value)}
                  className={`pl-7 w-full [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none ${errors.currentPrice ? 'border-destructive' : ''}`}
                  required
                />
                {errors.currentPrice && (
                  <p className="text-sm text-destructive">{errors.currentPrice}</p>
                )}
              </div>
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
              {isSubmitting ? 'Updating...' : 'Update Price'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
