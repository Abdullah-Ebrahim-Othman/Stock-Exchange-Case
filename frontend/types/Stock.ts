export interface Stock {
  stockId: string;
  name: string;
  description: string;
  currentPrice: number;
  updatedAt: string;
}

export interface StockExchange {
  stockExchangeId: string
  name: string;
  description: string;
  liveInMarket:boolean;

}