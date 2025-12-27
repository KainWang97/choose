<template>
  <div
    class="min-h-screen bg-gradient-to-b from-stone-50 to-stone-100 flex items-center justify-center px-4"
  >
    <div class="max-w-md w-full bg-white shadow-lg rounded-lg p-8">
      <h1 class="text-2xl font-bold text-sumi text-center mb-6">忘記密碼</h1>

      <!-- Success -->
      <div v-if="sent" class="text-center space-y-4">
        <div
          class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto"
        >
          <svg
            class="w-8 h-8 text-green-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
            ></path>
          </svg>
        </div>
        <p class="text-stone-600">
          如果該信箱已註冊，您將會收到重設密碼的連結。
        </p>
        <p class="text-stone-500 text-sm">
          請查收您的信箱，並點擊連結重設密碼。
        </p>
        <router-link to="/" class="inline-block text-sumi hover:underline mt-4">
          返回首頁
        </router-link>
      </div>

      <!-- Form -->
      <form v-else @submit.prevent="handleSubmit" class="space-y-6">
        <p class="text-stone-600 text-sm">
          請輸入您的註冊信箱，我們將發送重設密碼的連結給您。
        </p>

        <div>
          <label class="block text-sm font-medium text-stone-700 mb-1"
            >Email</label
          >
          <input
            v-model="email"
            type="email"
            required
            placeholder="請輸入您的 Email"
            class="w-full border border-stone-300 px-4 py-3 focus:outline-none focus:border-sumi"
          />
        </div>

        <button
          type="submit"
          :disabled="loading"
          class="w-full bg-sumi text-white py-3 hover:bg-stone-700 transition disabled:opacity-50"
        >
          {{ loading ? "發送中..." : "發送重設連結" }}
        </button>

        <div class="text-center">
          <router-link to="/" class="text-stone-500 hover:text-sumi text-sm">
            返回首頁
          </router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { api } from "../services/api.js";

const email = ref("");
const loading = ref(false);
const sent = ref(false);

const handleSubmit = async () => {
  loading.value = true;
  try {
    await api.auth.forgotPassword(email.value);
    sent.value = true;
  } catch (error) {
    // 不顯示錯誤，避免洩漏帳號是否存在
    sent.value = true;
  } finally {
    loading.value = false;
  }
};
</script>
