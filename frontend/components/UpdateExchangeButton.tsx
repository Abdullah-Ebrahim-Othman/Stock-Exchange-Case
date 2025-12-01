'use client';

import { useState } from 'react';
import { Button } from "@/components/ui/button";
import { Pencil } from "lucide-react";
import { UpdateStockExchangeModal } from "./updateStockExchangeModal";
import { StockExchange } from "@/types/Stock";
import { useRouter } from 'next/navigation';

interface UpdateExchangeButtonProps {
  exchange: StockExchange;
}

export function UpdateExchangeButton({ exchange }: UpdateExchangeButtonProps) {
  const [isOpen, setIsOpen] = useState(false);
  const router = useRouter();

  const handleSuccess = () => {
    router.refresh();
    setIsOpen(false);
  };

  return (
    <UpdateStockExchangeModal
      exchange={exchange}
      onSuccess={handleSuccess}
    >
      <Button
        variant="ghost"
        size="icon"
        title="Edit exchange"
        onClick={() => setIsOpen(true)}
      >
        <Pencil className="h-4 w-4"/>
      </Button>
    </UpdateStockExchangeModal>
  );
}
