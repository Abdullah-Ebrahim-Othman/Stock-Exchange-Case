export interface ResponseMessage {
  type: 'error' | 'success';
  message: string;
  status?: number;
  data?: any;
}