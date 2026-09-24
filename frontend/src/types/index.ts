export interface Order {
  id?: number;
  customerName: string;
  customerPhone: string;
  itemsOrdered: string;
  quantity: string;
  deliveryAddress: string;
  orderDate?: string;
}

export interface ChatMessage {
  role: 'user' | 'assistant' | 'system';
  content: string;
  placedOrder?: Order;
}

export interface ChatResponse {
  role: string;
  content: string;
  placedOrder?: Order;
  success: boolean;
  error?: string;
}

export interface QuickPrompt {
  label: string;
  prompt: string;
  icon: string;
}

export type TabMode = 'chat' | 'form';
