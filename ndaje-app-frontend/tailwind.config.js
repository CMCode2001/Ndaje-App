/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#1ba3ef',
          hover: '#ffffff',
          dark: '#0e153a', // Darker shade for contrast
        },
        brand: {
          dark: '#111b42',
          light: '#2f61bd',
        }
      },
      fontFamily: {
        sans: ['Poppins', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
