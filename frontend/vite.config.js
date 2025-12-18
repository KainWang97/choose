import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import tailwindcss from "@tailwindcss/vite";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue(), tailwindcss()],

  define: {
    // Polyfill process.env for the Google GenAI SDK usage pattern
    "process.env": process.env,
  },
});
