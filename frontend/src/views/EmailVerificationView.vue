<template>
  <div
    class="min-h-screen bg-gradient-to-b from-stone-50 to-stone-100 flex items-center justify-center px-4"
  >
    <div class="max-w-md w-full bg-white shadow-lg rounded-lg p-8 text-center">
      <!-- Loading -->
      <div v-if="loading" class="space-y-4">
        <div
          class="animate-spin w-12 h-12 border-4 border-sumi border-t-transparent rounded-full mx-auto"
        ></div>
        <p class="text-stone-600">驗證中...</p>
      </div>

      <!-- Success -->
      <div v-else-if="verified" class="space-y-6">
        <!-- 成功動畫 -->
        <div class="relative w-20 h-20 mx-auto">
          <!-- 進度圈動畫（底層） -->
          <svg
            class="absolute inset-0 w-20 h-20 -rotate-90"
            viewBox="0 0 100 100"
          >
            <circle
              cx="50"
              cy="50"
              r="46"
              fill="none"
              stroke="#d6d3d1"
              stroke-width="4"
            />
            <circle
              cx="50"
              cy="50"
              r="46"
              fill="none"
              stroke="#22c55e"
              stroke-width="4"
              stroke-dasharray="289"
              class="animate-countdown"
            />
          </svg>
          <!-- 打勾圖示（上層） -->
          <div class="absolute inset-0 flex items-center justify-center">
            <div
              class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center"
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
          </div>
        </div>

        <h1 class="text-2xl font-bold text-sumi">信箱驗證成功！</h1>

        <div class="space-y-2">
          <p class="text-stone-600">您的帳號已啟用，歡迎加入！</p>
          <p
            class="text-sm text-stone-500 flex items-center justify-center gap-2"
          >
            <span
              class="inline-block w-4 h-4 border-2 border-stone-400 border-t-transparent rounded-full animate-spin"
            ></span>
            <span v-if="redirectPath === '/checkout'"
              >2 秒後自動跳轉至<strong class="text-sumi">結帳頁面</strong
              >...</span
            >
            <span v-else
              >2 秒後自動跳轉至<strong class="text-sumi">首頁</strong>...</span
            >
          </p>

          <!-- 密碼設定提醒（僅直接註冊用戶） -->
          <p v-if="!redirectPath" class="text-xs text-amber-600 mt-4">
            ✨ 提醒：您可以在會員中心設定密碼，方便日後快速登入
          </p>
        </div>
      </div>

      <!-- Error -->
      <div v-else class="space-y-6">
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
        <h1 class="text-2xl font-bold text-sumi">驗證失敗</h1>
        <p class="text-stone-600">{{ errorMessage }}</p>
        <div class="space-y-3">
          <button
            @click="resendVerification"
            :disabled="resending"
            class="w-full bg-sumi text-white py-3 hover:bg-stone-700 transition disabled:opacity-50"
          >
            {{ resending ? "發送中..." : "重新發送驗證信" }}
          </button>
          <router-link
            to="/"
            class="block text-stone-500 hover:text-sumi transition"
          >
            返回首頁
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from "vue";
import { useRoute, useRouter } from "vue-router";
import { api } from "../services/api.js";

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const verified = ref(false);
const errorMessage = ref("驗證連結無效或已過期");
const resending = ref(false);
const redirectPath = ref(null);

// 注入全局 setUser 函數
const setUser = inject("setUser");

const verifyEmail = async () => {
  const token = route.query.token;

  if (!token) {
    loading.value = false;
    errorMessage.value = "缺少驗證 Token";
    return;
  }

  try {
    const result = await api.auth.verifyEmail(token);
    if (result && result.user) {
      verified.value = true;
      // 使用 inject 的 setUser 更新全局登入狀態
      if (setUser) {
        setUser(result.user);
      }

      // 讀取並清除 localStorage 中的跳轉目標
      redirectPath.value = localStorage.getItem("postVerifyRedirect");
      localStorage.removeItem("postVerifyRedirect");

      // 延遲跳轉，讓用戶看到成功訊息
      setTimeout(() => {
        if (redirectPath.value) {
          // 從結帳跳轉的註冊，返回結帳頁面
          router.push(redirectPath.value);
        } else {
          // 直接註冊的用戶，跳轉首頁
          router.push("/");
        }
      }, 2000);
    } else {
      verified.value = true;
    }
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message || "驗證連結無效或已過期";
  } finally {
    loading.value = false;
  }
};

const resendVerification = async () => {
  resending.value = true;
  try {
    await api.auth.resendVerification();
    alert("驗證信已重新發送，請查收您的信箱");
  } catch (error) {
    alert(error.response?.data?.message || "發送失敗，請先登入");
  } finally {
    resending.value = false;
  }
};

onMounted(() => {
  verifyEmail();
});
</script>

<style scoped>
/* 倒數環動畫 - 2秒從滿到空 */
@keyframes countdown {
  from {
    stroke-dashoffset: 0;
  }
  to {
    stroke-dashoffset: 289;
  }
}

.animate-countdown {
  animation: countdown 2s linear forwards;
}
</style>
