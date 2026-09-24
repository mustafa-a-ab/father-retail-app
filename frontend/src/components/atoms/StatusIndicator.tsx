import React from 'react';
import { Box, Typography } from '@mui/material';

interface StatusIndicatorProps {
  statusText?: string;
}

export const StatusIndicator: React.FC<StatusIndicatorProps> = ({
  statusText = 'Online & Ready to order',
}) => {
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <Box
        sx={{
          width: 8,
          height: 8,
          borderRadius: '50%',
          backgroundColor: '#10B981',
          position: 'relative',
          '&::after': {
            content: '""',
            position: 'absolute',
            width: '100%',
            height: '100%',
            borderRadius: '50%',
            backgroundColor: '#10B981',
            animation: 'pulse 1.8s infinite cubic-bezier(0.45, 0, 0.55, 1)',
          },
          '@keyframes pulse': {
            '0%': { transform: 'scale(1)', opacity: 0.8 },
            '70%': { transform: 'scale(2.5)', opacity: 0 },
            '100%': { transform: 'scale(2.5)', opacity: 0 },
          },
        }}
      />
      <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 500 }}>
        {statusText}
      </Typography>
    </Box>
  );
};
