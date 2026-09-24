import React from 'react';
import { Box, Typography } from '@mui/material';

interface BrandBadgeProps {
  label?: string;
}

export const BrandBadge: React.FC<BrandBadgeProps> = ({ label = '🛒 Father Retail Store' }) => {
  return (
    <Box
      sx={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: 1,
        px: 2,
        py: 0.75,
        borderRadius: '9999px',
        backgroundColor: 'rgba(13, 148, 136, 0.1)',
        border: '1px solid rgba(13, 148, 136, 0.25)',
        color: 'primary.dark',
      }}
    >
      <Typography variant="caption" sx={{ fontWeight: 700, fontSize: '0.8rem', letterSpacing: '0.02em' }}>
        {label}
      </Typography>
    </Box>
  );
};
