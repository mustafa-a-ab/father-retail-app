import React, { useRef, useEffect } from 'react';
import { Box, Typography, Button } from '@mui/material';
import RestartAltRoundedIcon from '@mui/icons-material/RestartAltRounded';
import { ChatMessage, QuickPrompt } from '../../types';
import { AvatarBadge } from '../atoms/AvatarBadge';
import { StatusIndicator } from '../atoms/StatusIndicator';
import { SuggestionChip } from '../molecules/SuggestionChip';
import { ChatBubble } from '../molecules/ChatBubble';
import { TypingIndicator } from '../molecules/TypingIndicator';
import { ChatInputBar } from '../molecules/ChatInputBar';

interface ChatSectionProps {
  messages: ChatMessage[];
  loading: boolean;
  onSendMessage: (text: string) => void;
  onResetChat: () => void;
}

const DEFAULT_QUICK_PROMPTS: QuickPrompt[] = [
  { label: '5kg Rice', prompt: 'I want to order 5kg of Rice', icon: '🌾' },
  { label: '2 Chickens', prompt: 'I need 2 fresh Chickens', icon: '🍗' },
  { label: '3 Bottles Oil', prompt: 'Order 3 bottles of Cooking Oil', icon: '🌻' },
  { label: 'Flour & Sugar', prompt: '10kg Flour and 2kg Sugar', icon: '🍞' },
];

export const ChatSection: React.FC<ChatSectionProps> = ({
  messages,
  loading,
  onSendMessage,
  onResetChat,
}) => {
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages, loading]);

  const showSuggestions = messages.length <= 1;

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: 600 }}>
      {/* Assistant Header */}
      <Box
        sx={{
          p: 2,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          borderBottom: '1px solid #E2E8F0',
          backgroundColor: '#FFFFFF',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <AvatarBadge role="assistant" />
          <Box>
            <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#0F172A', lineHeight: 1.2 }}>
              Grok Order Assistant
            </Typography>
            <StatusIndicator />
          </Box>
        </Box>

        <Button
          size="small"
          onClick={onResetChat}
          startIcon={<RestartAltRoundedIcon sx={{ fontSize: '1.1rem' }} />}
          sx={{
            color: 'text.secondary',
            fontSize: '0.8rem',
            backgroundColor: '#F8FAFC',
            border: '1px solid #E2E8F0',
            borderRadius: '10px',
            '&:hover': {
              backgroundColor: '#F1F5F9',
              color: 'text.primary',
            },
          }}
        >
          New Chat
        </Button>
      </Box>

      {/* Suggested Quick Prompts */}
      {showSuggestions && (
        <Box
          sx={{
            display: 'flex',
            gap: 1,
            p: 1.5,
            px: 2,
            overflowX: 'auto',
            backgroundColor: 'rgba(241, 245, 249, 0.6)',
            borderBottom: '1px solid #E2E8F0',
            '&::-webkit-scrollbar': { display: 'none' },
          }}
        >
          {DEFAULT_QUICK_PROMPTS.map((p, idx) => (
            <SuggestionChip
              key={idx}
              label={`${p.icon} ${p.label}`}
              onClick={() => onSendMessage(p.prompt)}
            />
          ))}
        </Box>
      )}

      {/* Message List */}
      <Box
        ref={scrollRef}
        sx={{
          flex: 1,
          overflowY: 'auto',
          p: 2.5,
          backgroundColor: '#FFFFFF',
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        {messages.map((msg, index) => (
          <ChatBubble key={index} message={msg} />
        ))}

        {loading && <TypingIndicator />}
      </Box>

      {/* Bottom Input */}
      <ChatInputBar onSend={onSendMessage} disabled={loading} />
    </Box>
  );
};
