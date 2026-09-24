import React, { useState } from 'react';
import { Box, TextField, IconButton } from '@mui/material';
import SendRoundedIcon from '@mui/icons-material/SendRounded';

interface ChatInputBarProps {
  onSend: (text: string) => void;
  disabled: boolean;
}

export const ChatInputBar: React.FC<ChatInputBarProps> = ({ onSend, disabled }) => {
  const [text, setText] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!text.trim() || disabled) return;
    onSend(text.trim());
    setText('');
  };

  return (
    <Box
      component="form"
      onSubmit={handleSubmit}
      sx={{
        display: 'flex',
        alignItems: 'center',
        gap: 1.5,
        p: 1.5,
        backgroundColor: '#FFFFFF',
        borderTop: '1px solid #E2E8F0',
      }}
    >
      <TextField
        fullWidth
        size="small"
        placeholder="Type your message to Grok (e.g. 5kg Rice delivered to 123 Main St...)"
        value={text}
        onChange={(e) => setText(e.target.value)}
        disabled={disabled}
        autoComplete="off"
        sx={{
          '& .MuiOutlinedInput-root': {
            borderRadius: '12px',
            backgroundColor: '#F8FAFC',
            '&:hover': {
              backgroundColor: '#FFFFFF',
            },
          },
        }}
      />
      <IconButton
        type="submit"
        disabled={disabled || !text.trim()}
        color="primary"
        sx={{
          width: 42,
          height: 42,
          borderRadius: '12px',
          backgroundColor: text.trim() && !disabled ? 'primary.main' : 'rgba(0, 0, 0, 0.05)',
          color: text.trim() && !disabled ? '#FFFFFF' : 'text.disabled',
          transition: 'all 0.2s',
          '&:hover': {
            backgroundColor: text.trim() && !disabled ? 'primary.dark' : 'rgba(0, 0, 0, 0.08)',
            transform: text.trim() && !disabled ? 'scale(1.05)' : 'none',
          },
        }}
      >
        <SendRoundedIcon sx={{ fontSize: '1.2rem' }} />
      </IconButton>
    </Box>
  );
};
