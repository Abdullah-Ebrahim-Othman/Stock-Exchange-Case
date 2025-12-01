'use client';

import { useState } from 'react';
import { Button } from "@/components/ui/button";
import { Pencil } from "lucide-react";
import { UpdateStockModal } from "./updateStockModal";
import { Stock } from "@/types/Stock";
import { useRouter } from 'next/navigation';

interface UpdateStockButtonProps {
  stock: Stock;
}

export function UpdateStockButton({ stock }: UpdateStockButtonProps) {
  const [isOpen, setIsOpen] = useState(false);
  const router = useRouter();

  const handleSuccess = () => {
    router.refresh();
    setIsOpen(false);
  };

  return (
    <UpdateStockModal
      stock={stock}
      onStockUpdated={handleSuccess}
    >
      <Button
        variant="ghost"
        size="icon"
        title="Edit stock"
        onClick={() => setIsOpen(true)}
      >
        <Pencil className="h-4 w-4"/>
      </Button>
    </UpdateStockModal>
  );
}
