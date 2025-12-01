'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Circle, Eye } from 'lucide-react';
import { fetchStockExchangesForStock } from '@/lib/api';
import { StockExchange } from '@/types/Stock';

interface StockExchangesForStockTableProps {
  stockId: string;
}

export function StockExchangesForStockTable({ stockId }: StockExchangesForStockTableProps) {
  const router = useRouter();
  const [exchanges, setExchanges] = useState<StockExchange[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [pageSize, setPageSize] = useState(10);

  const loadExchanges = async () => {
    setIsLoading(true);
    try {
      const response = await fetchStockExchangesForStock(stockId, currentPage, pageSize);
      setExchanges(Array.isArray(response.content) ? response.content : []);
      setTotalPages(response.totalPages ?? 0);
      setTotalElements(response.totalElements ?? 0);

      if (currentPage >= response.totalPages && response.totalPages > 0) {
        setCurrentPage(response.totalPages - 1);
      }
    } catch (error) {
      console.error("Error loading stock exchanges:", error);
      setExchanges([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadExchanges();
  }, [currentPage, pageSize, stockId]);

  const getRowNumber = (index: number) => {
    return currentPage * pageSize + index + 1;
  };

  return (
    <div className="space-y-4">
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>#</TableHead>
              <TableHead>Name</TableHead>
              <TableHead>Description</TableHead>
              <TableHead className="text-center">Status</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={5} className="h-32 text-center">
                  <div className="flex items-center justify-center space-x-2">
                    <div className="h-4 w-4 animate-spin rounded-full border-2 border-primary border-t-transparent"></div>
                    <span className="text-muted-foreground">Loading exchanges...</span>
                  </div>
                </TableCell>
              </TableRow>
            ) : exchanges.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="h-32 text-center">
                  <div className="flex flex-col items-center justify-center space-y-2">
                    <p className="text-muted-foreground">This stock is not listed on any exchanges yet</p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              exchanges.map((exchange, index) => (
                <TableRow key={exchange.stockExchangeId} className="hover:bg-muted/50">
                  <TableCell>{getRowNumber(index)}</TableCell>
                  <TableCell className="font-medium">{exchange.name}</TableCell>
                  <TableCell className="max-w-xs truncate text-muted-foreground">
                    {exchange.description || "No description available"}
                  </TableCell>
                  <TableCell className="text-center">
                    <div className="flex items-center justify-center">
                      <Circle
                        className={`h-3 w-3 mr-2 ${
                          exchange.liveInMarket 
                            ? 'text-green-500 fill-green-500' 
                            : 'text-gray-400 fill-gray-400'
                        }`}
                      />
                      {exchange.liveInMarket ? 'Live' : 'Closed'}
                    </div>
                  </TableCell>
                  <TableCell className="text-right">
                    <Button
                      variant="ghost"
                      size="icon"
                      title="View exchange details"
                      onClick={() => {
                        router.push(`/dashboard/stock-exchanges/${exchange.stockExchangeId}`);
                      }}
                    >
                      <Eye className="h-4 w-4 text-blue-500" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* Pagination */}
      {!isLoading && exchanges.length > 0 && (
        <div className="flex items-center justify-between px-2">
          <div className="text-sm text-muted-foreground">
            Showing <span className="font-medium">{(currentPage * pageSize) + 1}</span> to{' '}
            <span className="font-medium">
              {Math.min((currentPage + 1) * pageSize, totalElements)}
            </span>{' '}
            of <span className="font-medium">{totalElements}</span> exchanges
          </div>
          <div className="flex items-center space-x-2">
            <select
              value={pageSize}
              onChange={(e) => {
                setPageSize(Number(e.target.value));
                setCurrentPage(0);
              }}
              className="h-9 rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm"
              disabled={isLoading}
            >
              {[5, 10, 20, 50].map((size) => (
                <option key={size} value={size}>
                  Show {size}
                </option>
              ))}
            </select>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 0))}
              disabled={currentPage === 0 || isLoading}
            >
              Previous
            </Button>
            <span className="text-sm text-muted-foreground">
              Page {currentPage + 1} of {Math.max(totalPages, 1)}
            </span>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages - 1))}
              disabled={currentPage >= totalPages - 1 || isLoading}
            >
              Next
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}