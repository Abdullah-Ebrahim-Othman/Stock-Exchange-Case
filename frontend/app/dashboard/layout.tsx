'use client';

import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { AppHeader } from '@/components/layout/AppHeader';

export default function Layout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-gradient-to-br  from-slate-100 via-slate-200 to-slate-300
     dark:from-black dark:via-neutral-900 dark:to-neutral-800">
      <AppHeader />

      <main className="container mx-auto px-4 py-8">
        <Card className="backdrop-blur-xl bg-white/10
         dark:bg-gray-900/10 border border-white/10 dark:border-white/10
         shadow-2xl  overflow-hidden">
          <CardHeader className="border-b border-white/10 dark:border-white/10">
            <div className="flex justify-between items-center">
              <div className="flex items-center gap-2">
                <a href="/dashboard" className="text-slate-900 dark:text-white">
                  <CardTitle className="text-2xl font-bold">
                    Dashboard
                  </CardTitle>
                </a>
              </div>
            </div>
          </CardHeader>
          <CardContent className="p-1">
            {children}
          </CardContent>
        </Card>
      </main>
    </div>
  );
}