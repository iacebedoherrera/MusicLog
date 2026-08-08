import type { Config } from 'tailwindcss';
import forms from '@tailwindcss/forms';

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#161616',
        paper: '#f7f5ef',
        signal: '#e44d26',
        moss: '#264438',
      },
      fontFamily: {
        display: ['DM Serif Display', 'Georgia', 'serif'],
      },
    },
  },
  plugins: [forms],
} satisfies Config;
