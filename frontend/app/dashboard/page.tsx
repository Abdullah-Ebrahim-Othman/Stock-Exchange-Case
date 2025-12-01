

"use client";

import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs"
import {StockTable} from "@/components/stock-table";
import {StockExchangeTable} from "@/components/stock-exchange-table";

export default async function DashboardPage() {

	return (
		<div className="p-12">
			<Tabs defaultValue="stocks">
				<TabsList>
					<TabsTrigger value="stocks">Stocks</TabsTrigger>
					<TabsTrigger value="exchanges">Stock Exchanges</TabsTrigger>
				</TabsList>
				<TabsContent value="stocks" className="mt-4">
					<StockTable/>
				</TabsContent>
				<TabsContent value="exchanges" className="mt-4">
					<StockExchangeTable />
				</TabsContent>
			</Tabs>
		</div>
	)
}