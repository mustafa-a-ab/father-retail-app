import React from 'react';
import { Box } from '@mui/material';

interface AvatarBadgeProps {
  role: 'user' | 'assistant';
}

export const AvatarBadge: React.FC<AvatarBadgeProps> = ({ role }) => {
  const isAssistant = role === 'assistant';

  return (
    <Box
      sx={{
        width: 36,
        height: 36,
        borderRadius: '10px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: '1.1rem',
        flexShrink: 0,
        backgroundColor: isAssistant ? 'rgba(99, 102, 241, 0.12)' : 'rgba(15, 23, 42, 0.08)',
        border: isAssistant ? '1px solid rgba(99, 102, 241, 0.25)' : '1px solid rgba(15, 23, 42, 0.12)',
        color: isAssistant ? '#4F46E5' : '#0F172A',
        boxShadow: '0 2px 5px rgba(0,0,0,0.04)',
      }}
    >
      {isAssistant ? '⚡' : '👤'}
    </Box>
  );
};
