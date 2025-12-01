'use server';

import {RegisterRequest} from '@/types/auth';
import { registerUser } from '@/lib/api';
import {ResponseMessage} from "@/types/ResponseMessage";


export async function registerAction(data: RegisterRequest): Promise<ResponseMessage> {
  try {
    const response = await registerUser(data);
    return {
      type: response.status === 200 ? 'success' : 'error',
      message: response.message
    };
  } catch (error) {
    console.error('Registration error:', error);
    const errorMessage = error instanceof Error ? error.message : 'An unexpected error occurred';
    return { type: 'error', message: errorMessage };
  }
}
