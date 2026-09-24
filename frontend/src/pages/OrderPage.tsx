import React, { useState } from 'react';
import ChatBubbleOutlineRoundedIcon from '@mui/icons-material/ChatBubbleOutlineRounded';
import AssignmentRoundedIcon from '@mui/icons-material/AssignmentRounded';
import { Snackbar, Alert } from '@mui/material';
import { ChatMessage, Order, TabMode } from '../types';
import { sendChatMessage, submitOrder } from '../services/api';
import { StoreLayout } from '../components/templates/StoreLayout';
import { StoreHeader } from '../components/organisms/StoreHeader';
import { TabButton } from '../components/atoms/TabButton';
import { ChatSection } from '../components/organisms/ChatSection';
import { ManualOrderForm } from '../components/organisms/ManualOrderForm';

const INITIAL_MESSAGES: ChatMessage[] = [
  {
    role: 'assistant',
    content:
      "Hello! 👋 Welcome to Father Retail Store. What items can I get delivered for you today? Tell me what you need, and I'll place the order for you!",
  },
];

export const OrderPage: React.FC = () => {
  const [tabMode, setTabMode] = useState<TabMode>('chat');
  const [messages, setMessages] = useState<ChatMessage[]>(INITIAL_MESSAGES);
  const [chatLoading, setChatLoading] = useState(false);
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  const handleSendMessage = async (text: string) => {
    const updatedMessages: ChatMessage[] = [...messages, { role: 'user', content: text }];
    setMessages(updatedMessages);
    setChatLoading(true);

    try {
      const response = await sendChatMessage(updatedMessages);
      if (response.success) {
        setMessages([
          ...updatedMessages,
          {
            role: 'assistant',
            content: response.content,
            placedOrder: response.placedOrder,
          },
        ]);
      } else {
        setMessages([
          ...updatedMessages,
          {
            role: 'assistant',
            content: response.error || 'Sorry, I encountered an error processing your request.',
          },
        ]);
      }
    } catch (err: any) {
      setMessages([
        ...updatedMessages,
        {
          role: 'assistant',
          content: `⚠️ Communication error: ${err.message || 'Failed to reach server'}`,
        },
      ]);
    } finally {
      setChatLoading(false);
    }
  };

  const handleResetChat = () => {
    setMessages(INITIAL_MESSAGES);
    setSnackbar({
      open: true,
      message: 'Chat conversation reset.',
      severity: 'success',
    });
  };

  const handleSubmitManualOrder = async (order: Order): Promise<Order> => {
    const saved = await submitOrder(order);
    setSnackbar({
      open: true,
      message: `Order #${saved.id} placed successfully!`,
      severity: 'success',
    });
    return saved;
  };

  return (
    <>
      <StoreLayout
        header={<StoreHeader />}
        tabs={
          <>
            <TabButton
              active={tabMode === 'chat'}
              onClick={() => setTabMode('chat')}
              icon={<ChatBubbleOutlineRoundedIcon sx={{ fontSize: '1.2rem' }} />}
              label="Chat with Grok"
              badge="AI"
            />
            <TabButton
              active={tabMode === 'form'}
              onClick={() => setTabMode('form')}
              icon={<AssignmentRoundedIcon sx={{ fontSize: '1.2rem' }} />}
              label="Standard Form"
            />
          </>
        }
      >
        {tabMode === 'chat' ? (
          <ChatSection
            messages={messages}
            loading={chatLoading}
            onSendMessage={handleSendMessage}
            onResetChat={handleResetChat}
          />
        ) : (
          <ManualOrderForm onSubmit={handleSubmitManualOrder} />
        )}
      </StoreLayout>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert
          severity={snackbar.severity}
          onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}
          sx={{ borderRadius: '12px', fontWeight: 600 }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </>
  );
};
