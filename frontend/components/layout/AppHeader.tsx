'use client';

import { ThemeToggle } from '@/components/theme-toggle';
import LogoutButton from '@/components/auth/LogoutButton';

export function AppHeader() {
  return (
    <header className="sticky top-0 z-10 backdrop-blur-md bg-white/70 dark:bg-gray-900/70 border-b border-white/10 dark:border-white/10">
      <div className="container mx-auto px-4 py-3 flex justify-between items-center">
        <h1 className="text-2xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-indigo-600 dark:from-blue-400 dark:to-indigo-400">
          Stock Exchange
        </h1>
        <div className="flex items-center gap-4">
          <ThemeToggle />
          <LogoutButton />
        </div>
      </div>
    </header>
  );
}