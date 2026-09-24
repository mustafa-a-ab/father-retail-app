import React from 'react';
import { Button, Box, Chip } from '@mui/material';

interface TabButtonProps {
  active: boolean;
  onClick: () => void;
  icon: React.ReactNode;
  label: string;
  badge?: string;
}

export const TabButton: React.FC<TabButtonProps> = ({
  active,
  onClick,
  icon,
  label,
  badge,
}) => {
  return (
    <Button
      onClick={onClick}
      startIcon={icon}
      sx={{
        flex: 1,
        py: 1.25,
        px: 3,
        borderRadius: '12px',
        fontWeight: active ? 700 : 500,
        color: active ? '#0F172A' : '#64748B',
        backgroundColor: active ? '#FFFFFF' : 'transparent',
        boxShadow: active
          ? '0 4px 12px rgba(15, 23, 42, 0.08), 0 1px 3px rgba(15, 23, 42, 0.05)'
          : 'none',
        transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
        '&:hover': {
          backgroundColor: active ? '#FFFFFF' : 'rgba(255, 255, 255, 0.5)',
          color: '#0F172A',
        },
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <span>{label}</span>
        {badge && (
          <Chip
            label={badge}
            size="small"
            sx={{
              height: 20,
              fontSize: '0.65rem',
              fontWeight: 800,
              backgroundColor: active ? 'secondary.main' : 'rgba(99, 102, 241, 0.15)',
              color: active ? '#FFFFFF' : 'secondary.dark',
              letterSpacing: '0.05em',
            }}
          />
        )}
      </Box>
    </Button>
  );
};
