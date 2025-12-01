import { Inter } from 'next/font/google';
import { ReactNode } from 'react';
import { ThemeProvider } from 'next-themes';
import { Toaster } from 'react-hot-toast';
import './globals.css';

const inter = Inter({ subsets: ['latin'] });

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en" className={inter.className} suppressHydrationWarning>
      <body className="min-h-screen bg-white text-gray-700 dark:bg-gray-500 dark:text-gray-100 transition-colors duration-200">
        <ThemeProvider
          attribute="class"
          defaultTheme="system"
          enableSystem
          disableTransitionOnChange
        >
          {children}
          <header className="fixed top-4 right-4 z-50">
          </header>
          <Toaster
            position="top-center"
            toastOptions={{
              duration: 2000,
              className: '!bg-white dark:!bg-gray-800 !text-gray-900 dark:!text-gray-100 !border !border-gray-200 dark:!border-gray-700 !shadow-lg',
              success: {
                duration: 2000,
                className: '!bg-green-50 dark:!bg-green-900/20 !text-green-700 dark:!text-green-400 !border-green-200 dark:!border-green-800',
                iconTheme: {
                  primary: '#10B981',
                  secondary: 'white',
                },
              },
              error: {
                duration: 2000,
                className: '!bg-red-50 dark:!bg-red-900/20 !text-red-700 dark:!text-red-400 !border-red-200 dark:!border-red-800',
                iconTheme: {
                  primary: '#EF4444',
                  secondary: 'white',
                },
              },
              loading: {
                duration: 2000,
                className: '!bg-blue-50 dark:!bg-blue-900/20 !text-blue-700 dark:!text-blue-400 !border-blue-200 dark:!border-blue-800',
              },
            }}
          />
        </ThemeProvider>
      </body>
    </html>
  );
}