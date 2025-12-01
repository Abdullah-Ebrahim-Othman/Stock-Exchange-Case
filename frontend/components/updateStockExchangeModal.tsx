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
import { Textarea } from "@/components/ui/textarea"
import { Switch } from "@/components/ui/switch"
import { StockExchange } from "@/types/Stock"
import { updateStockExchange } from "@/lib/api"
import { toast } from 'react-hot-toast';

interface UpdateStockExchangeModalProps {
  exchange: StockExchange;
  onSuccess?: () => void;
  children: React.ReactNode;
}

export function UpdateStockExchangeModal({ 
  exchange, 
  onSuccess, 
  children 
}: UpdateStockExchangeModalProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [formData, setFormData] = useState({
    name: exchange.name,
    description: exchange.description || '',
    liveInMarket: exchange.liveInMarket
  });


  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validate form
    const newErrors: Record<string, string> = {};
    
    if (!formData.name.trim()) {
      newErrors.name = 'Name is required';
    }
    
    if (!formData.description.trim()) {
      newErrors.description = 'Description is required';
    }
    
    setErrors(newErrors);
    
    if (Object.keys(newErrors).length > 0) {
      return;
    }
    
    try {
      await updateStockExchange(exchange.stockExchangeId.toString(), {
        name: formData.name,
        description: formData.description,
      });
      
      toast.success('Stock exchange updated successfully');
      onSuccess?.();
      setIsOpen(false);
      setIsOpen(false);
    } catch (error) {
      console.error('Error updating stock exchange:', error);
      toast.error('Failed to update stock exchange');
    }
  };

  const handleInputChange = (field: string, value: string | boolean) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const handleOpenChange = (newOpen: boolean) => {
    if (!newOpen && !isSubmitting) {
      setFormData({
        name: exchange.name,
        description: exchange.description || '',
        liveInMarket: exchange.liveInMarket
      });
      setErrors({});
    }
    setIsOpen(newOpen);
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        {children}
      </DialogTrigger>
      <DialogContent className="sm:max-w-[500px]">
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>Update Stock Exchange</DialogTitle>
            <DialogDescription>
              Update the stock exchange information below.
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
                Description
              </Label>
              <Textarea
                id="description"
                placeholder="Brief description of the stock exchange"
                value={formData.description}
                onChange={(e) => handleInputChange("description", e.target.value)}
                maxLength={255}
                rows={3}
                className={errors.description ? "border-destructive" : ""}
              />
              {errors.description && (
                <p className="text-sm text-destructive">{errors.description}</p>
              )}
              <p className="text-xs text-muted-foreground">
                {formData.description.length}/255 characters
              </p>
            </div>

          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Updating..." : "Update Exchange"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
