import React, { useState } from 'react';
import {
  Box,
  TextField,
  MenuItem,
  Button,
  CircularProgress,
  Alert,
} from '@mui/material';
import ShoppingBagRoundedIcon from '@mui/icons-material/ShoppingBagRounded';
import { Order } from '../../types';
import { ReceiptCard } from '../molecules/ReceiptCard';

interface ManualOrderFormProps {
  onSubmit: (order: Order) => Promise<Order>;
}

const GROCERY_ITEMS = [
  'Rice',
  'Flour',
  'Meat',
  'Tuna',
  'Chicken',
  'Oil',
  'Sugar',
  'Milk',
  'Eggs',
];

export const ManualOrderForm: React.FC<ManualOrderFormProps> = ({ onSubmit }) => {
  const [formData, setFormData] = useState<Order>({
    customerName: '',
    customerPhone: '',
    itemsOrdered: '',
    quantity: '',
    deliveryAddress: '',
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [placedOrder, setPlacedOrder] = useState<Order | null>(null);

  const handleChange = (field: keyof Order, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setPlacedOrder(null);

    try {
      const saved = await onSubmit(formData);
      setPlacedOrder(saved);
      // Reset form
      setFormData({
        customerName: '',
        customerPhone: '',
        itemsOrdered: '',
        quantity: '',
        deliveryAddress: '',
      });
    } catch (err: any) {
      setError(err.message || 'Failed to submit order. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ p: { xs: 2.5, sm: 4 }, backgroundColor: '#FFFFFF' }}>
      {error && (
        <Alert severity="error" sx={{ mb: 3, borderRadius: '12px' }}>
          {error}
        </Alert>
      )}

      {placedOrder && (
        <Box sx={{ mb: 4 }}>
          <ReceiptCard order={placedOrder} />
        </Box>
      )}

      <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
        <TextField
          label="Full Name"
          required
          fullWidth
          placeholder="e.g. Mustafa Abdu"
          value={formData.customerName}
          onChange={(e) => handleChange('customerName', e.target.value)}
        />

        <TextField
          label="Phone Number"
          required
          fullWidth
          placeholder="e.g. 0501234567"
          value={formData.customerPhone}
          onChange={(e) => handleChange('customerPhone', e.target.value)}
        />

        <TextField
          select
          label="Select Item"
          required
          fullWidth
          value={formData.itemsOrdered}
          onChange={(e) => handleChange('itemsOrdered', e.target.value)}
        >
          <MenuItem value="" disabled>
            -- Choose Grocery Item --
          </MenuItem>
          {GROCERY_ITEMS.map((item) => (
            <MenuItem key={item} value={item}>
              {item}
            </MenuItem>
          ))}
        </TextField>

        <TextField
          label="Quantity"
          required
          fullWidth
          placeholder="e.g. 5 kg, 2 bottles, 1 pack"
          value={formData.quantity}
          onChange={(e) => handleChange('quantity', e.target.value)}
        />

        <TextField
          label="Delivery Address"
          required
          multiline
          rows={3}
          fullWidth
          placeholder="Street name, building number, apartment/villa..."
          value={formData.deliveryAddress}
          onChange={(e) => handleChange('deliveryAddress', e.target.value)}
        />

        <Button
          type="submit"
          variant="contained"
          size="large"
          disabled={loading}
          startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <ShoppingBagRoundedIcon />}
          sx={{
            py: 1.5,
            fontSize: '1rem',
            fontWeight: 700,
            borderRadius: '12px',
            background: 'linear-gradient(135deg, #0D9488 0%, #059669 100%)',
            boxShadow: '0 4px 14px 0 rgba(13, 148, 136, 0.35)',
            '&:hover': {
              background: 'linear-gradient(135deg, #0F766E 0%, #047857 100%)',
              boxShadow: '0 6px 20px 0 rgba(13, 148, 136, 0.45)',
            },
          }}
        >
          {loading ? 'Submitting Order...' : 'Submit Order'}
        </Button>
      </Box>
    </Box>
  );
};
