'use client';

import { useState } from 'react';
import { Button } from "@/components/ui/button";
import { Trash2 } from "lucide-react";
import { useRouter } from 'next/navigation';
import { deleteStockExchange } from "@/lib/api";
import { toast } from 'react-hot-toast';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";

interface DeleteExchangeButtonProps {
  exchangeId: string;
  exchangeName: string;
  onDeleted?: () => void;
}

export function DeleteExchangeButton({ exchangeId, exchangeName, onDeleted }: DeleteExchangeButtonProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const router = useRouter();

  const handleDelete = async () => {
    try {
      setIsDeleting(true);
      await deleteStockExchange(exchangeId);
      toast.success('Stock exchange deleted successfully');
      
      // Call the onDeleted callback if provided
      if (onDeleted) {
        onDeleted();
      }
      
      // Always redirect to dashboard after deletion
      router.push('/dashboard');
      router.refresh();
    } catch (error) {
      console.error('Error deleting stock exchange:', error);
      toast.error('Failed to delete stock exchange');
    } finally {
      setIsDeleting(false);
      setIsOpen(false);
    }
  };

  return (
    <>
      <Button
        variant="ghost"
        size="icon"
        title="Delete exchange"
        onClick={() => setIsOpen(true)}
        className="hover:bg-red-100"
      >
        <Trash2 className="h-4 w-4 text-red-500" />
      </Button>

      <AlertDialog open={isOpen} onOpenChange={setIsOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you sure?</AlertDialogTitle>
            <AlertDialogDescription>
              This action cannot be undone. This will permanently delete the stock exchange "{exchangeName}".
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isDeleting}>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDelete}
              disabled={isDeleting}
              className="bg-red-600 hover:bg-red-700 focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
            >
              {isDeleting ? 'Deleting...' : 'Delete'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
