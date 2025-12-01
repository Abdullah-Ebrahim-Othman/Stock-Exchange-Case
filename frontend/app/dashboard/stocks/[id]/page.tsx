import { notFound, redirect } from 'next/navigation';
import { fetchStock } from '@/lib/api';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { TrendingUp, Clock, DollarSign, Hash, Building2 } from 'lucide-react';
import { Suspense } from 'react';
import { StockExchangesForStockTable } from "@/components/stock-exchanges-for-stock-table";
import { UpdateStockButton } from "@/components/UpdateStockButton";
import { DeleteStockButton } from "@/components/DeleteStockButton";
import { Stock } from "@/types/Stock";

interface StockDetailsProps {
  params: Promise<{ id: string }>;
}

function LoadingState({ message }: { message: string }) {
  return (
    <div className="h-64 flex items-center justify-center">
      <div className="flex flex-col items-center space-y-2">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
        <p className="text-sm text-muted-foreground">{message}</p>
      </div>
    </div>
  );
}

export default async function StockDetailsPage({ params }: StockDetailsProps) {

  const resolvedParams = await params;

  if (!resolvedParams?.id) {
    console.error('No ID provided in params');
    notFound();
  }

  const id = resolvedParams.id;
  let stock;

  try {
    console.log('Fetching stock with ID:', id);
    stock = await fetchStock(id);

    if (!stock) {
      console.error('Stock not found for ID:', id);
      notFound();
    }

    console.log('Successfully fetched stock:', stock.name);
  } catch (error: unknown) {
    console.error('Error fetching stock:', error);

    if (error instanceof Error) {
      if (error.message === 'Unauthorized') {
        console.log('User unauthorized, redirecting to login');
        redirect('/login');
      }
      console.error('Error message:', error.message);
    }

    notFound();
  }

  const formatPrice = (price: number | undefined) => {
    if (price === undefined || price === null) return 'N/A';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(price);
  };

  const formatDate = (dateString: string | undefined) => {
    if (!dateString) return 'N/A';
    try {
      return new Date(dateString).toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return 'Invalid Date';
    }
  };

  return (
    <div className="container mx-auto py-8 space-y-8">
      {/* Header Section */}
      <Card>
        <CardHeader>
          <div className="flex items-start justify-between">
            <div className="space-y-2">
              <div className="flex items-center gap-4">
                <div className="flex items-center gap-3">
                  <TrendingUp className="h-8 w-8 text-primary" />
                  <CardTitle className="text-3xl">{stock.name}</CardTitle>
                </div>
                <div className="flex items-center gap-1 border-l border-gray-200 dark:border-gray-700 pl-3">
                  <UpdateStockButton stock={stock as Stock} />
                  <DeleteStockButton 
                    stockId={stock.stockId.toString()} 
                    stockName={stock.name}
                  />
                </div>
              </div>
              <CardDescription className="text-base">
                {stock.description || 'No description available'}
              </CardDescription>
            </div>
            <Badge variant="outline" className="text-lg px-4 py-2">
              ID: {stock.stockId}
            </Badge>
          </div>
        </CardHeader>
      </Card>

      {/* Stock Details */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Current Price Card */}
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground flex items-center gap-2">
              <DollarSign className="h-4 w-4" />
              Current Price
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">
              {formatPrice(stock.currentPrice)}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Real-time market price
            </p>
          </CardContent>
        </Card>

        {/* Stock ID Card */}
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground flex items-center gap-2">
              <Hash className="h-4 w-4" />
              Stock ID
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-mono">{stock.stockId}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Unique identifier
            </p>
          </CardContent>
        </Card>

        {/* Last Updated Card */}
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground flex items-center gap-2">
              <Clock className="h-4 w-4" />
              Last Updated
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-lg">
              {stock.updatedAt ? formatDate(stock.updatedAt) : 'N/A'}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Last price update
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Additional Information Section */}
      <Card>
        <CardHeader>
          <CardTitle>Stock Information</CardTitle>
          <CardDescription>Detailed information about this stock</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-2">
              <p className="text-sm text-muted-foreground">Name</p>
              <p className="text-base font-medium">{stock.name}</p>
            </div>
            <div className="space-y-2">
              <p className="text-sm text-muted-foreground">Current Price</p>
              <p className="text-base font-medium">{formatPrice(stock.currentPrice)}</p>
            </div>
            <div className="space-y-2">
              <p className="text-sm text-muted-foreground">Description</p>
              <p className="text-base">{stock.description || 'No description available'}</p>
            </div>
            {stock.updatedAt && (
              <div className="space-y-2">
                <p className="text-sm text-muted-foreground">Last Updated</p>
                <p className="text-base">{formatDate(stock.updatedAt)}</p>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Stock Exchanges Section */}
      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <Building2 className="h-5 w-5 text-primary" />
            <CardTitle>Listed on Stock Exchanges</CardTitle>
          </div>
          <CardDescription>
            All exchanges where {stock.name} is currently listed
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Suspense fallback={<LoadingState message="Loading stock exchanges..." />}>
            <StockExchangesForStockTable stockId={id} />
          </Suspense>
        </CardContent>
      </Card>
    </div>
  );
}

export async function generateMetadata({ params }: StockDetailsProps) {
  try {
    const resolvedParams = await params;
    const stock = await fetchStock(resolvedParams.id);

    return {
      title: `${stock.name} - Stock Details`,
      description: stock.description || `View details for ${stock.name}`,
    };
  } catch {
    return {
      title: 'Stock Details',
      description: 'View stock information',
    };
  }
}