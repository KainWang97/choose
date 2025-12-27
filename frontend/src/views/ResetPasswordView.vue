<template>
  <div
    class="min-h-screen bg-gradient-to-b from-stone-50 to-stone-100 flex items-center justify-center px-4"
  >
    <div class="max-w-md w-full bg-white shadow-lg rounded-lg p-8">
      <h1 class="text-2xl font-bold text-sumi text-center mb-6">重設密碼</h1>

      <!-- Token Invalid -->
      <div v-if="tokenInvalid" class="text-center space-y-4">
        <div
          class="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto"
        >
          <svg
            class="w-8 h-8 text-red-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            ></path>
          </svg>
        </div>
        <p class="text-stone-600">重設連結無效或已過期</p>
        <router-link
          to="/forgot-password"
          class="inline-block bg-sumi text-white px-6 py-3 hover:bg-stone-700 transition"
        >
          重新申請
        </router-link>
      </div>

      <!-- Success -->
      <div v-else-if="success" class="text-center space-y-4">
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
              d="M5 13l4 4L19 7"
            ></path>
          </svg>
        </div>
        <p class="text-stone-600">密碼重設成功！</p>
        <router-link
          to="/"
          class="inline-block bg-sumi text-white px-6 py-3 hover:bg-stone-700 transition"
        >
          返回首頁登入
        </router-link>
      </div>

      <!-- Form -->
      <form v-else @submit.prevent="handleSubmit" class="space-y-6">
        <div>
          <label class="block text-sm font-medium text-stone-700 mb-1"
            >新密碼</label
          >
          <input
            v-model="newPassword"
            type="password"
            required
            minlength="6"
            placeholder="請輸入新密碼（至少 6 個字元）"
            class="w-full border border-stone-300 px-4 py-3 focus:outline-none focus:border-sumi"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-stone-700 mb-1"
            >確認密碼</label
          >
          <input
            v-model="confirmPassword"
            type="password"
            required
            placeholder="請再次輸入新密碼"
            class="w-full border border-stone-300 px-4 py-3 focus:outline-none focus:border-sumi"
          />
        </div>

        <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>

        <button
          type="submit"
          :disabled="loading"
          class="w-full bg-sumi text-white py-3 hover:bg-stone-700 transition disabled:opacity-50"
        >
          {{ loading ? "重設中..." : "重設密碼" }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { api } from "../services/api.js";

const route = useRoute();
const newPassword = ref("");
const confirmPassword = ref("");
const loading = ref(false);
const success = ref(false);
const tokenInvalid = ref(false);
const error = ref("");

const token = ref("");

onMounted(() => {
  token.value = route.query.token;
  if (!token.value) {
    tokenInvalid.value = true;
  }
});

const handleSubmit = async () => {
  error.value = "";

  if (newPassword.value !== confirmPassword.value) {
    error.value = "兩次輸入的密碼不一致";
    return;
  }

  if (newPassword.value.length < 6) {
    error.value = "密碼長度至少需要 6 個字元";
    return;
  }

  loading.value = true;
  try {
    await api.auth.resetPassword(token.value, newPassword.value);
    success.value = true;
  } catch (err) {
    if (err.response?.status === 400) {
      tokenInvalid.value = true;
    } else {
      error.value = err.response?.data?.message || "重設失敗，請稍後再試";
    }
  } finally {
    loading.value = false;
  }
};
</script>
