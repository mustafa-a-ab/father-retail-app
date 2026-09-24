import React from 'react';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { theme } from './theme/theme';
import { OrderPage } from './pages/OrderPage';

export const App: React.FC = () => {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <OrderPage />
    </ThemeProvider>
  );
};

export default App;
