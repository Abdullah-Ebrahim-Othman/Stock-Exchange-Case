	"use client"
	import {format} from "date-fns"
	import {useEffect, useState} from "react"
	import {Table, TableBody, TableCaption, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table"
	import {Button} from "@/components/ui/button"
	import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select"
	import {fetchStocks, deleteStock} from "@/lib/api"
	import {Stock} from "@/types/Stock"
	import {CreateStockModal} from "@/components/createStockModal"
	import {UpdateStockModal} from "@/components/updateStockModal"
	import {Pencil, Trash2, Eye} from "lucide-react"
	import {useRouter} from "next/navigation"
	import {
	  AlertDialog,
	  AlertDialogAction,
	  AlertDialogCancel,
	  AlertDialogContent,
	  AlertDialogDescription,
	  AlertDialogFooter,
	  AlertDialogHeader,
	  AlertDialogTitle,
	} from "@/components/ui/alert-dialog"
	import {toast} from "react-hot-toast";

	export function StockTable() {
		const router = useRouter()
		const [stocks, setStocks] = useState<Stock[]>([])
		const [currentPage, setCurrentPage] = useState(0)
		const [totalPages, setTotalPages] = useState(0)
		const [totalElements, setTotalElements] = useState(0)
		const [isLoading, setIsLoading] = useState(true)
		const [pageSize, setPageSize] = useState(20)
		const [stockToDelete, setStockToDelete] = useState<Stock | null>(null)
		const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false)
		const [isDeleting, setIsDeleting] = useState(false)

		const loadStocks = async () => {

			setIsLoading(true)
			try {
				const response = await fetchStocks(currentPage, pageSize)
				setStocks(
					Array.isArray(response.content) ? response.content : []
				)
				setTotalPages(response.totalPages ?? 0)
				setTotalElements(response.totalElements ?? 0)


				if (currentPage >= response.totalPages && response.totalPages > 0) {
					setCurrentPage(response.totalPages - 1)
				}
			} catch (error) {
				console.error("Error loading stocks:", error)
			} finally {
				setIsLoading(false)
			}
		}

		useEffect(() => {
			loadStocks()
		}, [currentPage, pageSize])

		const formatMarketCap = (value?: number) => {
			if (value === undefined || value === null || isNaN(value) || value <= 0) return "N/A"

			if (value >= 1e12) return `$${(value / 1e12).toFixed(2)}T`
			if (value >= 1e9) return `$${(value / 1e9).toFixed(2)}B`
			if (value >= 1e6) return `$${(value / 1e6).toFixed(2)}M`
			return `$${value.toFixed(2)}`
		}

		const formatDate = (dateString?: string) => {
			if (!dateString) return "N/A"
			try {
				const date = new Date(dateString);
				const formattedDate = format(date, "MMM d, yyyy");
				const formattedTime = format(date, "h:mm a");
				return (
					<div className="flex flex-col">
						<span>{formattedDate}</span>
						<span className="text-xs text-muted-foreground">{formattedTime}</span>
					</div>
				);
			} catch (error) {
				return "Invalid Date"
			}
		}

		const getRowNumber = (index: number) => {
			return currentPage * pageSize + index + 1
		}

		const handlePageSizeChange = (value: string) => {
			setPageSize(Number(value))
			setCurrentPage(0)
		}

		const handleStockCreated = () => {
			// Refresh the table after creating a stock
			loadStocks()
		}

		const handleDeleteClick = (stock: Stock) => {
			setStockToDelete(stock)
			setIsDeleteDialogOpen(true)
		}

		const handleConfirmDelete = async () => {
			if (!stockToDelete) return

			setIsDeleting(true)
			try {
				await deleteStock(stockToDelete.stockId)
				toast.success('Stock deleted successfully')
				loadStocks() // Refresh the table
				setIsDeleteDialogOpen(false)
			} catch (error) {
				console.error('Error deleting stock:', error)
				toast.error('Failed to delete stock')
			} finally {
				setIsDeleting(false)
			}
		}

		return (
			<div className="w-full space-y-4">
				{/* Header with Create Button */}
				<div className="flex items-center justify-between">
					<div>
						<h2 className="text-2xl font-bold tracking-tight">Stock Portfolio</h2>
						<p className="text-muted-foreground">
							Manage and view all your stocks
						</p>
					</div>
					<CreateStockModal onStockCreated={handleStockCreated}/>
				</div>

				<div className="rounded-lg border bg-card">
					<Table>
						<TableCaption className="py-4">
							Showing page {currentPage + 1} of {Math.max(totalPages, 1)}
						</TableCaption>
						<TableHeader>
							<TableRow className="hover:bg-transparent">
								<TableHead className="font-semibold">Stock ID</TableHead>
								<TableHead className="font-semibold">Name</TableHead>
								<TableHead className="font-semibold">Description</TableHead>
								<TableHead className="text-right font-semibold pr-9">Price</TableHead>
								<TableHead className="font-semibold w-48">Last Updated</TableHead>
								<TableHead className="text-right font-semibold">Actions</TableHead>
							</TableRow>
						</TableHeader>
						<TableBody>
							{isLoading ? (
								<TableRow>
									<TableCell colSpan={6} className="h-32 text-center">
										<div className="flex items-center justify-center space-x-2">
											<div
												className="h-4 w-4 animate-spin rounded-full border-2 border-primary border-t-transparent"></div>
											<span className="text-muted-foreground">Loading stocks...</span>
										</div>
									</TableCell>
								</TableRow>
							) : stocks.length === 0 ? (
								<TableRow>
									<TableCell colSpan={6} className="h-32 text-center">
										<div className="flex flex-col items-center justify-center space-y-2">
											<p className="text-muted-foreground">No stocks found</p>
											<p className="text-sm text-muted-foreground">Create your first stock to get started</p>
										</div>
									</TableCell>
								</TableRow>
							) : (
								stocks.map((stock, index) => (
									<TableRow key={stock.stockId} className="hover:bg-muted/50">
										<TableCell className="font-mono text-sm">
											{stock.stockId || "N/A"}
										</TableCell>
										<TableCell className="font-medium">
											{stock.name || "Unnamed Stock"}
										</TableCell>
										<TableCell className="max-w-xs truncate text-muted-foreground">
											{stock.description || "No description available"}
										</TableCell>
										<TableCell
											className="text-right font-mono font-medium pr-9">
											{stock.currentPrice ? `$${stock.currentPrice.toFixed(2)}` : "N/A"}
										</TableCell>
										<TableCell className="text-muted-foreground">
											{formatDate(stock.updatedAt)}
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
												<UpdateStockModal
													stock={stock}
													onStockUpdated={loadStocks}
												>
													<Button
														variant="ghost"
														size="icon"
														title="Edit stock"
													>
														<Pencil className="h-4 w-4" />
													</Button>
												</UpdateStockModal>
												<Button
													variant="ghost"
													size="icon"
													onClick={(e) => {
														e.stopPropagation()
														handleDeleteClick(stock)
													}}
													title="Delete stock"
													className="hover:bg-red-100"
												>
													<Trash2 className="h-4 w-4 text-red-500" />
												</Button>
											</div>
										</TableCell>

										{/*todo only when u think of adding create and update dates*/}
										{/*<TableCell className="text-right text-sm text-muted-foreground">
	                  {formatDate(stock.updatedAt)}*/}
										{/*</TableCell>*/}
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
				<AlertDialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
				<AlertDialogContent>
					<AlertDialogHeader>
						<AlertDialogTitle>Are you sure?</AlertDialogTitle>
						<AlertDialogDescription>
							This action cannot be undone. This will permanently delete the stock "{stockToDelete?.name}".
						</AlertDialogDescription>
					</AlertDialogHeader>
					<AlertDialogFooter>
						<AlertDialogCancel disabled={isDeleting}>Cancel</AlertDialogCancel>
						<AlertDialogAction
							onClick={handleConfirmDelete}
							disabled={isDeleting}
							className="bg-red-600 hover:bg-red-700 focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
						>
							{isDeleting ? 'Deleting...' : 'Delete'}
						</AlertDialogAction>
					</AlertDialogFooter>
				</AlertDialogContent>
			</AlertDialog>
		</div>
		)
	}