import React from 'react';
import { Box } from '@mui/material';
import { AvatarBadge } from '../atoms/AvatarBadge';

export const TypingIndicator: React.FC = () => {
  return (
    <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'flex-end', mb: 2 }}>
      <AvatarBadge role="assistant" />
      <Box
        sx={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: 0.75,
          py: 1.5,
          px: 2,
          borderRadius: '16px 16px 16px 4px',
          backgroundColor: '#F1F5F9',
          border: '1px solid #E2E8F0',
        }}
      >
        {[0, 1, 2].map((i) => (
          <Box
            key={i}
            sx={{
              width: 7,
              height: 7,
              borderRadius: '50%',
              backgroundColor: '#6366F1',
              animation: 'bounceDot 1.4s infinite ease-in-out',
              animationDelay: `${i * 0.2}s`,
              '@keyframes bounceDot': {
                '0%, 80%, 100%': { transform: 'scale(0.6)', opacity: 0.4 },
                '40%': { transform: 'scale(1)', opacity: 1 },
              },
            }}
          />
        ))}
      </Box>
    </Box>
  );
};
