import React from 'react';
import { Box, Typography } from '@mui/material';
import { BrandBadge } from '../atoms/BrandBadge';

export const StoreHeader: React.FC = () => {
  return (
    <Box sx={{ textAlign: 'center', mb: 3 }}>
      <Box sx={{ mb: 1.5 }}>
        <BrandBadge />
      </Box>
      <Typography
        variant="h4"
        component="h1"
        sx={{
          fontWeight: 800,
          color: '#0F172A',
          mb: 0.75,
          fontSize: { xs: '1.75rem', sm: '2.25rem' },
        }}
      >
        Fast Grocery Delivery
      </Typography>
      <Typography
        variant="body1"
        sx={{
          color: 'text.secondary',
          maxWidth: 520,
          mx: 'auto',
          fontSize: { xs: '0.9rem', sm: '1rem' },
        }}
      >
        Order fresh essentials directly through our AI assistant or standard form
      </Typography>
    </Box>
  );
};
