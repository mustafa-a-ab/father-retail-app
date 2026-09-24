import React from 'react';
import { Box, Card, Typography, Chip } from '@mui/material';
import CheckCircleRoundedIcon from '@mui/icons-material/CheckCircleRounded';
import { Order } from '../../types';

interface ReceiptCardProps {
  order: Order;
}

export const ReceiptCard: React.FC<ReceiptCardProps> = ({ order }) => {
  return (
    <Card
      sx={{
        mt: 2,
        borderRadius: '16px',
        overflow: 'hidden',
        border: '1px solid rgba(16, 185, 129, 0.3)',
        backgroundColor: '#FFFFFF',
        boxShadow: '0 8px 24px -4px rgba(16, 185, 129, 0.12), 0 2px 6px rgba(0, 0, 0, 0.04)',
      }}
    >
      {/* Header */}
      <Box
        sx={{
          background: 'linear-gradient(135deg, #059669 0%, #10B981 100%)',
          color: '#FFFFFF',
          p: 2,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <CheckCircleRoundedIcon sx={{ fontSize: '1.4rem' }} />
          <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
            Order Placed Successfully!
          </Typography>
        </Box>
        {order.id && (
          <Chip
            label={`#${order.id}`}
            size="small"
            sx={{
              backgroundColor: 'rgba(255, 255, 255, 0.25)',
              color: '#FFFFFF',
              fontWeight: 800,
              fontSize: '0.75rem',
            }}
          />
        )}
      </Box>

      {/* Details Grid */}
      <Box sx={{ p: 2.5 }}>
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' },
            gap: 2,
          }}
        >
          <Box>
            <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, display: 'block', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
              Customer
            </Typography>
            <Typography variant="body2" sx={{ fontWeight: 600, color: 'text.primary', mt: 0.25 }}>
              {order.customerName || 'N/A'}
            </Typography>
          </Box>

          <Box>
            <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, display: 'block', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
              Phone Number
            </Typography>
            <Typography variant="body2" sx={{ fontWeight: 600, color: 'text.primary', mt: 0.25 }}>
              {order.customerPhone || 'N/A'}
            </Typography>
          </Box>

          <Box>
            <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, display: 'block', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
              Items Ordered
            </Typography>
            <Typography variant="body2" sx={{ fontWeight: 600, color: 'primary.dark', mt: 0.25 }}>
              {order.itemsOrdered || 'N/A'}
            </Typography>
          </Box>

          <Box>
            <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, display: 'block', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
              Quantity
            </Typography>
            <Typography variant="body2" sx={{ fontWeight: 600, color: 'text.primary', mt: 0.25 }}>
              {order.quantity || 'N/A'}
            </Typography>
          </Box>

          <Box sx={{ gridColumn: { xs: '1', sm: '1 / -1' } }}>
            <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, display: 'block', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
              Delivery Address
            </Typography>
            <Typography variant="body2" sx={{ fontWeight: 500, color: 'text.primary', mt: 0.25 }}>
              {order.deliveryAddress || 'N/A'}
            </Typography>
          </Box>

          {order.orderDate && (
            <Box sx={{ gridColumn: { xs: '1', sm: '1 / -1' } }}>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 500, fontStyle: 'italic' }}>
                Placed on {order.orderDate}
              </Typography>
            </Box>
          )}
        </Box>
      </Box>
    </Card>
  );
};
