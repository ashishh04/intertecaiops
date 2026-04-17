/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        background: '#0a0a0f', // Very dark blue/black
        foreground: '#ffffff',
        accentTheme: '#853694', // Deep purple green
        accentLogoRed: '#dc2626', // Small red dot for the logo
        surface: 'rgba(255, 255, 255, 0.05)', // Glassmorphism surface
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
        outfit: ['Outfit', 'sans-serif'],
      },
      animation: {
        'wave-slow': 'wave 15s linear infinite',
        'wave-fast': 'wave 10s linear infinite reverse',
      },
      keyframes: {
        wave: {
          '0%': { transform: 'translateX(0) translateZ(0) scaleY(1)' },
          '50%': { transform: 'translateX(-25%) translateZ(0) scaleY(0.8)' },
          '100%': { transform: 'translateX(-50%) translateZ(0) scaleY(1)' },
        }
      }
    },
  },
  plugins: [],
}

