
import {notFound, redirect} from 'next/navigation';
import {fetchStockExchange} from '@/lib/api';
import {Suspense} from 'react';
import {StocksInExchangeTable} from '@/components/stocks-in-exchange-table';
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '@/components/ui/card';
import {Badge} from '@/components/ui/badge';
import {Building2, TrendingUp} from 'lucide-react';
import {StocksNotInExchangeTable} from "@/components/stocks-not-in-exchange-table";
import {UpdateExchangeButton} from "@/components/UpdateExchangeButton";
import {DeleteExchangeButton} from "@/components/DeleteExchangeButton";

interface StockExchangeDetailsProps {
	params: Promise<{
		id: string;
	}>;
}

export default async function StockExchangeDetails({params}: StockExchangeDetailsProps) {
	console.log(params)
	const resolvedParams = await params;
	if (!resolvedParams?.id) {
		console.error('No ID provided in params');
		notFound();
	}

	const {id} = resolvedParams;

	const numericId = Number(id);
	if (isNaN(numericId) || numericId <= 0) {
		console.error('Invalid ID format:', id);
		notFound();
	}

	let stockExchange;


	try {
		stockExchange = await fetchStockExchange(numericId);

		if (!stockExchange) {
			notFound();
		}

	} catch (error: unknown) {

		if (error instanceof Error) {
			if (error.message === 'Unauthorized') {
				redirect('/login');
			}
			console.error('Error message:', error.message);
		}

		notFound();
	}
	return (
		<div className="container mx-auto py-8 space-y-8">
			{/* Header Section */}
			<Card>
				<CardHeader>
					<div className="flex items-start justify-between">
						<div className="space-y-2">
							<div className="flex items-center gap-4">
								<div className="flex items-center gap-3">
									<Building2 className="h-8 w-8 text-primary"/>
									<CardTitle className="text-3xl">{stockExchange.name}</CardTitle>
								</div>
								<div className="flex items-center gap-1 border-l border-gray-200 dark:border-gray-700 pl-3">
									<UpdateExchangeButton exchange={stockExchange}/>
									<DeleteExchangeButton
										exchangeId={stockExchange.stockExchangeId}
										exchangeName={stockExchange.name}
									/>
								</div>
							</div>
							<CardDescription className="text-base">
								{stockExchange.description || 'No description available'}
							</CardDescription>
						</div>
						<Badge
							variant={stockExchange.liveInMarket ? "default" : "secondary"}
							className={stockExchange.liveInMarket ? "bg-green-500" : ""}
						>
							{stockExchange.liveInMarket ? 'Live Market' : 'Market Closed'}
						</Badge>
					</div>
				</CardHeader>
				<CardContent>
					<div className="grid grid-cols-1 md:grid-cols-3 gap-4">
						<div className="flex flex-col space-y-1">
							<span className="text-sm text-muted-foreground">Exchange ID</span>
							<span className="text-lg font-semibold">{stockExchange.stockExchangeId}</span>
						</div>
						<div className="flex flex-col space-y-1">
							<span className="text-sm text-muted-foreground">Status</span>
							<span className="text-lg font-semibold">
                {stockExchange.liveInMarket ? 'Active Trading' : 'Inactive'}
              </span>
						</div>
					</div>
				</CardContent>
			</Card>

			{/* Stocks in Exchange Section */}
			<Card>
				<CardHeader>
					<div className="flex items-center gap-2">
						<TrendingUp className="h-5 w-5 text-primary"/>
						<CardTitle>Stocks Listed in {stockExchange.name}</CardTitle>
					</div>
					<CardDescription>
						View all stocks currently listed on this exchange
					</CardDescription>
				</CardHeader>
				<CardContent>
					<Suspense fallback={<LoadingState message="Loading exchange stocks..."/>}>
						<StocksInExchangeTable exchangeId={id}/>
					</Suspense>
				</CardContent>
			</Card>
			{/* Stocks Not in This Exchange Section */}
			<Card>
				<CardHeader>
					<CardTitle>Available Stocks to Add</CardTitle>
					<CardDescription>
						Stocks not currently listed on this exchange
					</CardDescription>
				</CardHeader>
				<CardContent>
					<Suspense fallback={<LoadingState message="Loading available stocks..."/>}>
						<StocksNotInExchangeTable exchangeId={id}/>
					</Suspense>
				</CardContent>
			</Card>
		</div>
	);
}

function LoadingState({message}: { message: string }) {
	return (
		<div className="h-32 flex items-center justify-center">
			<div className="flex flex-col items-center space-y-2">
				<div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
				<p className="text-sm text-muted-foreground">{message}</p>
			</div>
		</div>
	);
}
