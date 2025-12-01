'use client';

import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { logoutUser } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { LogOut } from 'lucide-react';

export default function LogoutButton() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);

  const handleLogout = async () => {
    setLoading(true);
    try {
      const { success } = await logoutUser();
      if (success) {
        router.push('/login');
        router.refresh();
      }
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Button
      variant="outline"
      size="sm"
      onClick={handleLogout}
      disabled={loading}
      className="gap-2"
    >
      {loading ? (
        <div className="flex items-center gap-2">
          <div className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
          <span>Logging out</span>
        </div>
      ) : (
        <>
          <LogOut className="h-4 w-4" />
          <span className="sr-only sm:not-sr-only">Logout</span>
        </>
      )}
    </Button>
  );
}