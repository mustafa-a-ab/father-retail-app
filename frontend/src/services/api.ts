import { ChatMessage, ChatResponse, Order } from '../types';

const API_BASE = (import.meta.env.VITE_API_URL || '').replace(/\/$/, '');

export async function sendChatMessage(messages: ChatMessage[]): Promise<ChatResponse> {
  const response = await fetch(`${API_BASE}/api/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      messages: messages.map(m => ({
        role: m.role,
        content: m.content,
      })),
    }),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Server returned ${response.status}: ${errorText || response.statusText}`);
  }

  return response.json();
}

export async function submitOrder(order: Order): Promise<Order> {
  const response = await fetch(`${API_BASE}/api/orders`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(order),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Failed to submit order: ${errorText || response.statusText}`);
  }

  return response.json();
}
