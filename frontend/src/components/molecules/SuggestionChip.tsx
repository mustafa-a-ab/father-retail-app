import React from 'react';
import { Chip } from '@mui/material';

interface SuggestionChipProps {
  label: string;
  onClick: () => void;
}

export const SuggestionChip: React.FC<SuggestionChipProps> = ({ label, onClick }) => {
  return (
    <Chip
      label={label}
      onClick={onClick}
      clickable
      sx={{
        fontWeight: 600,
        fontSize: '0.82rem',
        py: 2,
        px: 1,
        borderRadius: '10px',
        backgroundColor: '#FFFFFF',
        color: '#334155',
        border: '1px solid #E2E8F0',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.04)',
        transition: 'all 0.18s ease',
        '&:hover': {
          backgroundColor: 'rgba(13, 148, 136, 0.08)',
          borderColor: 'primary.light',
          color: 'primary.dark',
          transform: 'translateY(-1px)',
          boxShadow: '0 4px 8px rgba(13, 148, 136, 0.12)',
        },
      }}
    />
  );
};
