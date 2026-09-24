import React from 'react';
import { Box, Typography } from '@mui/material';
import { ChatMessage } from '../../types';
import { AvatarBadge } from '../atoms/AvatarBadge';
import { ReceiptCard } from './ReceiptCard';

interface ChatBubbleProps {
  message: ChatMessage;
}

export const ChatBubble: React.FC<ChatBubbleProps> = ({ message }) => {
  const isUser = message.role === 'user';

  return (
    <Box
      sx={{
        display: 'flex',
        gap: 1.5,
        alignItems: 'flex-start',
        justifyContent: isUser ? 'flex-end' : 'flex-start',
        mb: 2.5,
      }}
    >
      {!isUser && <AvatarBadge role="assistant" />}

      <Box sx={{ maxWidth: { xs: '85%', sm: '75%' } }}>
        <Box
          sx={{
            py: 1.5,
            px: 2.25,
            borderRadius: isUser ? '18px 18px 4px 18px' : '18px 18px 18px 4px',
            backgroundColor: isUser ? '#0F172A' : '#F8FAFC',
            color: isUser ? '#FFFFFF' : '#0F172A',
            border: isUser ? 'none' : '1px solid #E2E8F0',
            boxShadow: isUser
              ? '0 4px 12px rgba(15, 23, 42, 0.15)'
              : '0 2px 6px rgba(0, 0, 0, 0.03)',
            whiteSpace: 'pre-wrap',
            wordBreak: 'break-word',
          }}
        >
          <Typography
            variant="body1"
            sx={{
              fontSize: '0.95rem',
              lineHeight: 1.6,
              fontWeight: isUser ? 400 : 450,
            }}
          >
            {message.content}
          </Typography>

          {message.placedOrder && <ReceiptCard order={message.placedOrder} />}
        </Box>
      </Box>

      {isUser && <AvatarBadge role="user" />}
    </Box>
  );
};
