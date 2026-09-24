import React from 'react';
import { Box, Container, Card } from '@mui/material';

interface StoreLayoutProps {
  header: React.ReactNode;
  tabs: React.ReactNode;
  children: React.ReactNode;
}

export const StoreLayout: React.FC<StoreLayoutProps> = ({ header, tabs, children }) => {
  return (
    <Box
      sx={{
        minHeight: '100vh',
        py: { xs: 4, sm: 6 },
        px: 2,
        background: 'radial-gradient(ellipse at 50% 0%, #E2E8F0 0%, #F1F5F9 50%, #E2E8F0 100%)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
      }}
    >
      <Container maxWidth="sm" sx={{ p: 0 }}>
        {header}

        {/* Navigation Tabs Bar */}
        <Box
          sx={{
            display: 'flex',
            backgroundColor: 'rgba(226, 232, 240, 0.75)',
            backdropFilter: 'blur(8px)',
            borderRadius: '16px',
            p: 0.75,
            mb: 2.5,
            gap: 1,
            border: '1px solid rgba(255, 255, 255, 0.8)',
          }}
        >
          {tabs}
        </Box>

        {/* Main Card Container */}
        <Card
          sx={{
            overflow: 'hidden',
            borderRadius: '24px',
            boxShadow: '0 20px 40px -15px rgba(15, 23, 42, 0.08), 0 0 1px 1px rgba(15, 23, 42, 0.05)',
            border: '1px solid rgba(255, 255, 255, 0.8)',
            backgroundColor: '#FFFFFF',
          }}
        >
          {children}
        </Card>
      </Container>
    </Box>
  );
};
