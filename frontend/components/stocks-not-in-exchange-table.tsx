"use client"
import {useEffect, useState} from "react"
import {Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table"
import {Button} from "@/components/ui/button"
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select"
import {Checkbox} from "@/components/ui/checkbox"
import {fetchStocksNotInExchange, addStocksToExchange} from "@/lib/api"
import {Stock} from "@/types/Stock"
import {Eye, Plus} from "lucide-react"
import {useRouter} from "next/navigation"
import {toast} from "sonner"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle
} from "@/components/ui/alert-dialog"

export function StocksNotInExchangeTable({ exchangeId }: { exchangeId: string }) {
  const router = useRouter()
  const [stocks, setStocks] = useState<Stock[]>([])
  const [selectedStockIds, setSelectedStockIds] = useState<string[]>([])
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [isAddingStocks, setIsAddingStocks] = useState(false)
  const [isConfirmDialogOpen, setIsConfirmDialogOpen] = useState(false)
  const [pageSize, setPageSize] = useState(10)
  const numericExchangeId = Number(exchangeId)

  const loadStocks = async () => {
    if (isNaN(numericExchangeId)) return
    
    setIsLoading(true)
    try {
      const response = await fetchStocksNotInExchange(numericExchangeId, currentPage, pageSize)
      setStocks(Array.isArray(response.content) ? response.content : [])
      setTotalPages(response.totalPages ?? 0)
      setTotalElements(response.totalElements ?? 0)

      if (currentPage >= response.totalPages && response.totalPages > 0) {
        setCurrentPage(response.totalPages - 1)
      }
    } catch (error) {
      console.error("Error loading stocks not in exchange:", error)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadStocks()
  }, [currentPage, pageSize, exchangeId])

  const formatMarketCap = (value?: number) => {
    if (value === undefined || value === null || isNaN(value) || value <= 0) return "N/A"

    if (value >= 1e12) return `$${(value / 1e12).toFixed(2)}T`
    if (value >= 1e9) return `$${(value / 1e9).toFixed(2)}B`
    if (value >= 1e6) return `$${(value / 1e6).toFixed(2)}M`
    return `$${value.toFixed(2)}`
  }

  const handlePageSizeChange = (value: string) => {
    setPageSize(Number(value))
    setCurrentPage(0)
    setSelectedStockIds([])
  }

  const handleSelectStock = (stockId: string, checked: boolean) => {
    if (checked) {
      setSelectedStockIds(prev => [...prev, stockId])
    } else {
      setSelectedStockIds(prev => prev.filter(id => id !== stockId))
    }
  }

  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      setSelectedStockIds(stocks.map(stock => stock.stockId || ''))
    } else {
      setSelectedStockIds([])
    }
  }

  const handleAddStocks = async () => {
    if (selectedStockIds.length === 0) return
    
    try {
      setIsAddingStocks(true)
      await addStocksToExchange(numericExchangeId, selectedStockIds)
      toast.success('Successfully added stocks to exchange')
      setSelectedStockIds([])
      loadStocks() // Refresh the list
    } catch (error) {
      console.error('Error adding stocks to exchange:', error)
      toast.error('Failed to add stocks to exchange')
    } finally {
      setIsAddingStocks(false)
      setIsConfirmDialogOpen(false)
    }
  }

  return (
    <div className="w-full space-y-4">
      <div className="flex justify-between items-center">
        <h3 className="text-lg font-medium">Available Stocks</h3>
        <Button 
          onClick={() => selectedStockIds.length > 0 && setIsConfirmDialogOpen(true)}
          disabled={selectedStockIds.length === 0 || isAddingStocks}
          className="gap-2"
        >
          <Plus className="h-4 w-4" />
          {isAddingStocks ? 'Adding...' : `Add ${selectedStockIds.length} selected stocks`}
        </Button>
      </div>
      
      <div className="rounded-lg border bg-card">
        <Table>
          <TableCaption className="py-4">
            Showing page {currentPage + 1} of {Math.max(totalPages, 1)}
          </TableCaption>
          <TableHeader>
            <TableRow className="hover:bg-transparent">
              <TableHead className="w-12">
                <Checkbox 
                  checked={stocks.length > 0 && selectedStockIds.length === stocks.length}
                  onCheckedChange={handleSelectAll}
                  aria-label="Select all"
                />
              </TableHead>
              <TableHead className="font-semibold">Stock ID</TableHead>
              <TableHead className="font-semibold">Name</TableHead>
              <TableHead className="font-semibold">Description</TableHead>
              <TableHead className="text-right font-semibold">Price</TableHead>
              <TableHead className="text-right font-semibold">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={5} className="h-32 text-center">
                  <div className="flex items-center justify-center space-x-2">
                    <div className="h-4 w-4 animate-spin rounded-full border-2 border-primary border-t-transparent"></div>
                    <span className="text-muted-foreground">Loading stocks not in exchange...</span>
                  </div>
                </TableCell>
              </TableRow>
            ) : stocks.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="h-32 text-center">
                  <div className="flex flex-col items-center justify-center space-y-2">
                    <p className="text-muted-foreground">No stocks found</p>
                    <p className="text-sm text-muted-foreground">All available stocks are listed in this exchange</p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              stocks.map((stock) => (
                <TableRow key={stock.stockId} className="hover:bg-muted/50">
                  <TableCell>
                    <Checkbox 
                      checked={selectedStockIds.includes(stock.stockId || '')}
                      onCheckedChange={(checked) => handleSelectStock(stock.stockId || '', checked === true)}
                      aria-label={`Select ${stock.name}`}
                    />
                  </TableCell>
                  <TableCell className="font-mono text-sm">
                    {stock.stockId || "N/A"}
                  </TableCell>
                  <TableCell className="font-medium">
                    {stock.name || "Unnamed Stock"}
                  </TableCell>
                  <TableCell className="max-w-xs truncate text-muted-foreground">
                    {stock.description || "No description available"}
                  </TableCell>
                  <TableCell className="text-right font-semibold">
                    {formatMarketCap(stock.currentPrice)}
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end space-x-2">
                      <Button
                        variant="ghost"
                        size="icon"
                        title="View details"
                        onClick={(e) => {
                          e.stopPropagation()
                          router.push(`/dashboard/stocks/${stock.stockId}`)
                        }}
                      >
                        <Eye className="h-4 w-4 text-blue-500" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <div className="flex items-center justify-between px-6 py-4 border-t">
        <div className="flex items-center space-x-2">
          <p className="text-sm text-muted-foreground">
            {stocks.length > 0 && totalElements > 0 ? (
              <>
                Showing <span className="font-medium">{(currentPage * pageSize) + 1}</span> to{' '}
                <span className="font-medium">
                  {Math.min((currentPage + 1) * pageSize, totalElements)}
                </span>{' '}
                of <span className="font-medium">{totalElements}</span> stocks
              </>
            ) : (
              'No stocks found'
            )}
          </p>
        </div>

        <div className="flex items-center space-x-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
            disabled={currentPage === 0 || isLoading}
          >
            Previous
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={() => setCurrentPage(prev => prev + 1)}
            disabled={currentPage >= totalPages - 1 || isLoading}
          >
            Next
          </Button>
          <div className="flex items-center space-x-2 ml-4">
            <p className="text-sm text-muted-foreground">Rows per page</p>
            <Select
              value={pageSize.toString()}
              onValueChange={handlePageSizeChange}
              disabled={isLoading}
            >
              <SelectTrigger className="h-8 w-[70px] bg-background">
                <SelectValue placeholder={pageSize} />
              </SelectTrigger>
              <SelectContent className="bg-background">
                {[5, 10, 20, 50].map(size => (
                  <SelectItem key={size} value={size.toString()} className="hover:bg-accent">
                    {size}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
      </div>

      <AlertDialog open={isConfirmDialogOpen} onOpenChange={setIsConfirmDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Add {selectedStockIds.length} stocks to exchange?</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to add {selectedStockIds.length} selected {selectedStockIds.length === 1 ? 'stock' : 'stocks'} to this exchange?
              This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isAddingStocks}>Cancel</AlertDialogCancel>
            <AlertDialogAction 
              onClick={handleAddStocks}
              disabled={isAddingStocks}
            >
              {isAddingStocks ? 'Adding...' : 'Add Stocks'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
