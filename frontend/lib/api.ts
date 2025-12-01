import {RegisterRequest} from '@/types/auth';
import {ResponseMessage} from "@/types/ResponseMessage";
import {toast} from 'react-hot-toast';
import {Stock} from "@/types/Stock";
import {StockExchange} from "@/types/Stock";

const getApiUrl = (): string => {
  if (typeof window === 'undefined') {
    const url = process.env.NEXT_INTERNAL_API_URL || 'http://backend:8080';
    console.log('🔧 [SERVER-SIDE] API URL:', url);
    return url;
  }

  const url = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
  console.log('🌐 [CLIENT-SIDE] API URL:', url);
  return url;
};

const API_BASE_URL = `${getApiUrl()}/api/v1`; // ← Fixed: added /

const fetchWithAuth = async (url: string, options: RequestInit = {}) => {
  const isServer = typeof window === 'undefined';

  let cookieHeader = '';
  if (isServer) {
    try {
      const {cookies} = await import('next/headers');
      const store = await cookies();
      const all = store.getAll();
      if (all.length > 0) {
        cookieHeader = all.map((c) => `${c.name}=${c.value}`).join('; ');
        console.log('🍪 [SERVER] Forwarding cookies:', cookieHeader);
      } else {
        console.log('⚠️ [SERVER] No cookies found to forward');
      }
    } catch (error) {
      console.error('❌ [SERVER] Error reading cookies:', error);
    }
  }

  const fullUrl = `${API_BASE_URL}${url}`;
  console.log(`[${isServer ? 'SERVER' : 'CLIENT'}] Fetching: ${fullUrl}`);

  const response = await fetch(fullUrl, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(cookieHeader ? { Cookie: cookieHeader } : {}),
      ...options.headers,
    },
    credentials: 'include',
  });

  console.log(`[${isServer ? 'SERVER' : 'CLIENT'}] Response status: ${response.status}`);

  // Only redirect if we're in the browser
  if (!isServer && response.status === 401) {
    console.log('🔒 [CLIENT] Unauthorized - redirecting to login');
    window.location.href = '/login';
    throw new Error('Unauthorized');
  }

  // On server-side, just throw the error - the page component will handle it
  if (isServer && response.status === 401) {
    console.log('🔒 [SERVER] Unauthorized - page will handle redirect');
    throw new Error('Unauthorized');
  }

  return response;
};
export const registerUser = async (data: RegisterRequest): Promise<ResponseMessage> => {
	try {
		const response = await fetchWithAuth('/auth/register', {
			method: 'POST',
			body: JSON.stringify(data),
		});

		return await response.json();
	} catch (error) {
		console.error('Registration failed:', error);
		throw error;
	}
};

export const logoutUser = async (): Promise<{ success: boolean }> => {
	try {
		const response = await fetchWithAuth('/auth/logout', {
			method: 'POST',
		});

		if (response.ok) {
			toast.success('Successfully logged out', {duration: 2000});
		} else {
			throw new Error('Logout failed');
		}

		return {success: response.ok};
	} catch (error) {
		console.error('Logout failed:', error);
		toast.error('Failed to log out. Please try again.');
		return {success: false};
	}
};


export interface PaginatedResponse<T> {
	content: T[];
	totalElements: number;
	totalPages: number;
	size: number;
	number: number;
}


export const fetchStocks = async (page: number = 0, size: number = 5):
	Promise<PaginatedResponse<Stock>> => {
	try {
		const response = await fetchWithAuth(`/stock?page=${page}&size=${size}`);
		if (!response.ok) {
			throw new Error('Failed to fetch stocks');
		}

		const data = await response.json();

		return data.data; // Assuming the response has a data property with the paginated result
	} catch (error) {
		console.error('Error fetching stocks:', error);
		throw error;
	}
};

export interface CreateStockRequest {
	name: string
	description: string
	currentPrice: number
}

export interface CreateStockExchangeRequest {
	name: string;
	description: string;
}

// Update your existing createStock function in api.ts
export async function createStock(stock: CreateStockRequest): Promise<Stock> {

	const response = await fetchWithAuth('/stock', {
		method: 'POST',
		body: JSON.stringify(stock),
	});

	const responseData = await response.json();


	if (!response.ok) {

		// Check if it's a validation error from Spring Boot
		if (response.status === 400 && responseData.errors) {
			throw {
				response: {
					data: {
						errors: responseData.errors,
						message: responseData.message || 'Validation failed'
					},
					status: response.status
				}
			};
		}

		throw {
			response: {
				data: responseData,
				status: response.status
			}
		};
	}

	return responseData.data;
}

export async function deleteStock(stockId: string): Promise<void> {
	try {
		const response = await fetchWithAuth(`/stock/${stockId}`, {
			method: 'DELETE',
		});

		if (!response.ok) {
			throw new Error('Failed to delete stock');
		}
	} catch (error) {
		console.error('Error deleting stock:', error);
		throw error;
	}
}

export async function deleteStockExchange(stockExchangeId: string): Promise<void> {
	try {
		const response = await fetchWithAuth(`/stockExchange/${stockExchangeId}`, {
			method: 'DELETE',
		});

		if (!response.ok) {
			throw new Error('Failed to delete stock exchange');
		}
	} catch (error) {
		console.error('Error deleting stock exchange:', error);
		throw error;
	}
}

export async function fetchStockExchanges(page: number = 0, size: number = 5): Promise<PaginatedResponse<StockExchange>> {
	try {
		const response = await fetchWithAuth(`/stockExchange?page=${page}&size=${size}`);
		if (!response.ok) {
			throw new Error('Failed to fetch stock exchanges');
		}
		const responseData = await response.json()
		return responseData.data;
	} catch (error) {
		console.error('Error fetching stock exchanges:', error);
		throw error;
	}
}

export interface UpdateStockExchangeRequest {
	name: string;
	description: string;
}

export async function updateStockExchange(id: string, exchange: UpdateStockExchangeRequest): Promise<StockExchange> {

	const response = await fetchWithAuth(`/stockExchange/${id}`, {
		method: 'PUT',
		body: JSON.stringify(exchange),
	});

	const responseData = await response.json();
	console.log(responseData)

	if (!response.ok) {
		if (response.status === 400 && responseData.errors) {
			throw {
				response: {
					data: {
						errors: responseData.errors,
						message: responseData.message || 'Validation failed'
					},
					status: response.status
				}
			};
		}

		throw {
			response: {
				data: responseData,
				status: response.status
			}
		};
	}

	return responseData.data;
}

export const fetchStockExchangesForStock = async (
	stockId: string,
	page: number = 0,
	size: number = 10,
	sortBy: string = 'name'
): Promise<PaginatedResponse<StockExchange>> => {
	try {
		console.log(`Fetching stock exchanges for stock ID: ${stockId}`);

		const response = await fetchWithAuth(
			`/stock/stocks/${stockId}/exchanges?page=${page}&size=${size}&sortBy=${sortBy}`
		);

		if (!response.ok) {
			console.error(`Failed to fetch exchanges for stock: ${response.status}`);
			throw new Error('Failed to fetch stock exchanges for this stock');
		}

		const data = await response.json();
		console.log(`Successfully fetched ${data.data?.content?.length || 0} exchanges`);

		return data.data;
	} catch (error) {
		console.error('Error fetching stock exchanges for stock:', error);
		throw error;
	}
};

export async function createStockExchange(exchange: CreateStockExchangeRequest): Promise<StockExchange> {
	const response = await fetchWithAuth('/stockExchange', {
		method: 'POST',
		body: JSON.stringify(exchange),
	});

	const responseData = await response.json();

	if (!response.ok) {
		if (response.status === 400 && responseData.errors) {
			throw {
				response: {
					data: {
						errors: responseData.errors,
						message: responseData.message || 'Validation failed'
					},
					status: response.status
				}
			};
		}

		throw {
			response: {
				data: responseData,
				status: response.status
			}
		};
	}

	return responseData.data;
}

export interface UpdateStockRequest {
	currentPrice: number;
}

export async function updateStock(id: string, stock: UpdateStockRequest): Promise<Stock> {
	const response = await fetchWithAuth(`/stock/${id}/price`, {
		method: 'PUT',
		body: JSON.stringify({
			...stock,
			currentPrice: Number(stock.currentPrice) // Ensure it's a number
		}),
	});

	const responseData = await response.json();

	if (!response.ok) {
		if (response.status === 400 && responseData.errors) {
			throw {
				response: {
					data: {
						errors: responseData.errors,
						message: responseData.message || 'Validation failed'
					},
					status: response.status
				}
			};
		}

		throw {
			response: {
				data: responseData,
				status: response.status
			}
		};
	}

	return responseData.data;
}

export const fetchStock = async (id: string): Promise<Stock> => {
	try {
		const response = await fetchWithAuth(`/stock/${id}`);
		if (!response.ok) {
			if (response.status === 401) {
				throw new Error('Unauthorized');
			}
			throw new Error('Failed to fetch stock details');
		}
		const data = await response.json();
		return data.data;
	} catch (error) {
		console.error('Error fetching stock:', error);
		throw error;
	}
};

export interface StockExchangeDetails extends StockExchange {
	// Add any additional fields that might be returned by the API
}

export const fetchStockExchange = async (id: number): Promise<StockExchangeDetails> => {
	try {
		console.log(`Attempting to fetch stock exchange with ID: ${id}`);
		console.log(`API URL: ${API_BASE_URL}/stockExchange/${id}`);

		const response = await fetchWithAuth(`/stockExchange/${id}`);

		console.log('Response status:', response.status);
		console.log('Response ok:', response.ok);

		if (!response.ok) {
			const errorText = await response.text();
			console.error('Error response body:', errorText);

			if (response.status === 401) {
				throw new Error('Unauthorized');
			}

			if (response.status === 404) {
				throw new Error('Stock exchange not found');
			}

			throw new Error(`Failed to fetch stock exchange: ${response.status} ${response.statusText}`);
		}

		const data = await response.json();
		console.log('Received data structure:', Object.keys(data));

		// Check if the data is wrapped in a 'data' property
		if (data.data) {
			console.log('Found data in data.data property');
			return data.data;
		}

		// Otherwise return the data directly
		console.log('Returning data directly');
		return data;
	} catch (error) {
		console.error('Error in fetchStockExchange:', error);
		throw error;
	}
};

export async function fetchStocksInExchange(
	exchangeId: number,
	page: number = 0,
	size: number = 10,
	sortBy: string = 'name'
): Promise<PaginatedResponse<Stock>> {
	try {
		const response = await fetchWithAuth(
			`/stockExchange/${exchangeId}/stocks?page=${page}&size=${size}`
		);


		if (!response.ok) {
			console.log(response.json())
			throw new Error('Failed to fetch stocks in exchange');
		}

		const data = await response.json();
		return data.data;
	} catch (error) {
		console.error('Error fetching stocks in exchange:', error);
		throw error;
	}
}

export async function fetchStocksNotInExchange(
	exchangeId: number,
	page: number = 0,
	size: number = 10,
): Promise<PaginatedResponse<Stock>> {
	try {
		const response = await fetchWithAuth(
			`/stockExchange/${exchangeId}/stocks/not-listed?page=${page}&size=${size}`
		)

		if (!response.ok) {
			const errorData = await response.json().catch(() => ({}))
			throw new Error(errorData.message || 'Failed to fetch stocks not in exchange')
		}

		const responseData = await response.json()
		console.log('Fetched stocks not in exchange:', responseData)
		return responseData.data
	} catch (error) {
		console.error('Error in fetchStocksNotInExchange:', error)
		throw error
	}
}

export const addStocksToExchange = async (exchangeId: number, stockIds: string[]): Promise<void> => {
	try {
		const response = await fetchWithAuth(`/stockExchange/${exchangeId}/stocks`, {
			method: 'POST',
			body: JSON.stringify({
				stockIds
			}),
		})
		if (!response.ok) {
			const errorData = await response.json().catch(() => ({}))
			throw new Error(errorData.message || 'Failed to add stocks to exchange')
		}
	} catch (error) {
		console.error('Error in addStocksToExchange:', error)
		throw error
	}
};

export const removeStocksFromExchange = async (exchangeId: number, stockIds: string[]): Promise<void> => {
	try {
		const response = await fetchWithAuth(`/stockExchange/${exchangeId}/stocks`, {
			method: 'DELETE',
			body: JSON.stringify({
				stockIds
			}),
		});

		if (!response.ok) {
			const errorData = await response.json().catch(() => ({}));
			throw new Error(errorData.message || 'Failed to remove stocks from exchange');
		}
	} catch (error) {
		console.error('Error in removeStocksFromExchange:', error);
		throw error;
	}
};


