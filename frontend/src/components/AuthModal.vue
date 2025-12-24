<script setup>
import { ref, computed } from "vue";
import { api } from "../services/api.js";

const props = defineProps({
  isOpen: Boolean,
});

const emit = defineEmits(["close", "login"]);

// Form state
const isLogin = ref(true);
const email = ref("");
const password = ref("");
const name = ref("");
const errorMessage = ref("");
const isLoading = ref(false);
const showPassword = ref(false);
const passwordFocused = ref(false);

// 欄位驗證狀態
const fieldErrors = ref({});

// Email 格式驗證
const isValidEmail = (emailValue) => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailValue);
};

// 驗證登入表單
const validateLoginForm = () => {
  const errors = {};

  if (!email.value?.trim()) {
    errors.email = "請輸入 Email";
  } else if (!isValidEmail(email.value)) {
    errors.email = "請輸入有效的 Email 格式";
  }

  if (!password.value) {
    errors.password = "請輸入密碼";
  } else if (password.value.length < 6) {
    errors.password = "密碼至少需要 6 個字元";
  }

  fieldErrors.value = errors;
  return Object.keys(errors).length === 0;
};

// 驗證註冊表單
const validateRegisterForm = () => {
  const errors = {};

  if (!name.value?.trim()) {
    errors.name = "請輸入姓名";
  }

  if (!email.value?.trim()) {
    errors.email = "請輸入 Email";
  } else if (!isValidEmail(email.value)) {
    errors.email = "請輸入有效的 Email 格式";
  }

  if (!password.value) {
    errors.password = "請輸入密碼";
  } else if (password.value.length < 6) {
    errors.password = "密碼至少需要 6 個字元";
  }

  fieldErrors.value = errors;
  return Object.keys(errors).length === 0;
};

// Reset form
const resetForm = () => {
  email.value = "";
  password.value = "";
  name.value = "";
  errorMessage.value = "";
  fieldErrors.value = {};
};

// Handle login
const handleLogin = async () => {
  errorMessage.value = "";

  if (!validateLoginForm()) {
    return;
  }

  isLoading.value = true;
  try {
    const loggedInUser = await api.auth.login({
      email: email.value,
      password: password.value,
    });

    emit("login", loggedInUser);
    emit("close");
    resetForm();
  } catch (error) {
    // 根據後端錯誤訊息顯示對應提示
    const msg = error.message?.toLowerCase() || "";
    if (
      msg.includes("invalid") ||
      msg.includes("credentials") ||
      msg.includes("password")
    ) {
      errorMessage.value = "帳號或密碼錯誤";
    } else if (msg.includes("not found") || msg.includes("user")) {
      errorMessage.value = "此帳號不存在";
    } else if (msg.includes("network") || msg.includes("fetch")) {
      errorMessage.value = "網路連線異常，請檢查網路後再試";
    } else {
      errorMessage.value = "登入失敗，請稍後再試";
    }
  } finally {
    isLoading.value = false;
  }
};

// Handle register
const handleRegister = async () => {
  errorMessage.value = "";

  if (!validateRegisterForm()) {
    return;
  }

  isLoading.value = true;
  try {
    const newUser = await api.auth.register({
      name: name.value,
      email: email.value,
      password: password.value,
    });

    emit("login", newUser);
    emit("close");
    resetForm();
  } catch (error) {
    const msg = error.message?.toLowerCase() || "";
    if (
      msg.includes("email") &&
      (msg.includes("exist") || msg.includes("registered"))
    ) {
      errorMessage.value = "此 Email 已被註冊";
    } else if (msg.includes("network") || msg.includes("fetch")) {
      errorMessage.value = "網路連線異常，請檢查網路後再試";
    } else {
      errorMessage.value = "註冊失敗，請稍後再試";
    }
  } finally {
    isLoading.value = false;
  }
};

const handleSubmit = () => {
  if (isLogin.value) {
    handleLogin();
  } else {
    handleRegister();
  }
};

// Toggle mode and reset error
const toggleMode = () => {
  isLogin.value = !isLogin.value;
  errorMessage.value = "";
  fieldErrors.value = {};
};
</script>

<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 z-[100] flex items-center justify-center p-4"
  >
    <!-- Backdrop -->
    <div
      class="absolute inset-0 bg-stone-900/40 backdrop-blur-sm transition-opacity"
      @click="emit('close')"
    />

    <!-- Modal -->
    <div
      class="relative bg-washi w-full max-w-md p-12 shadow-xl animate-fade-in-up rounded-sm"
    >
      <button
        @click="emit('close')"
        class="absolute top-4 right-4 text-stone-400 hover:text-sumi"
      >
        ✕
      </button>

      <h2 class="text-2xl font-serif text-center mb-8 text-sumi">
        {{ isLogin ? "登入" : "註冊會員" }}
      </h2>

      <form class="space-y-6" @submit.prevent="handleSubmit">
        <!-- Name field (register only) -->
        <div v-if="!isLogin">
          <label
            class="block text-xs uppercase tracking-widest text-stone-500 mb-2"
            >姓名</label
          >
          <input
            type="text"
            v-model="name"
            placeholder="請輸入姓名"
            class="w-full bg-stone-50 border p-3 text-sm focus:outline-none rounded-none transition-colors"
            :class="
              fieldErrors.name
                ? 'border-red-500'
                : 'border-stone-200 focus:border-stone-400'
            "
          />
          <p v-if="fieldErrors.name" class="text-xs text-red-600 mt-1">
            {{ fieldErrors.name }}
          </p>
        </div>

        <!-- Email field -->
        <div>
          <label
            class="block text-xs uppercase tracking-widest text-stone-500 mb-2"
            >Email</label
          >
          <input
            type="text"
            v-model="email"
            :placeholder="isLogin ? '' : '請輸入 Email'"
            class="w-full bg-stone-50 border p-3 text-sm focus:outline-none rounded-none transition-colors"
            :class="
              fieldErrors.email
                ? 'border-red-500'
                : 'border-stone-200 focus:border-stone-400'
            "
          />
          <p v-if="fieldErrors.email" class="text-xs text-red-600 mt-1">
            {{ fieldErrors.email }}
          </p>
        </div>

        <!-- Password field -->
        <div>
          <label
            class="block text-xs uppercase tracking-widest text-stone-500 mb-2"
            >密碼</label
          >
          <div class="relative">
            <input
              :type="showPassword ? 'text' : 'password'"
              v-model="password"
              :placeholder="isLogin ? '' : '設定密碼 (至少 6 字元)'"
              @focus="passwordFocused = true"
              @blur="passwordFocused = false"
              class="w-full bg-stone-50 border p-3 pr-10 text-sm focus:outline-none rounded-none transition-colors"
              :class="
                fieldErrors.password
                  ? 'border-red-500'
                  : 'border-stone-200 focus:border-stone-400'
              "
            />
            <button
              v-if="passwordFocused && password.length > 0"
              type="button"
              @mousedown.prevent="showPassword = !showPassword"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-stone-400 hover:text-stone-600 transition-colors"
            >
              <!-- Eye Open -->
              <svg
                v-if="!showPassword"
                class="w-5 h-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="1.5"
                  d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                />
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="1.5"
                  d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                />
              </svg>
              <!-- Eye Closed -->
              <svg
                v-else
                class="w-5 h-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="1.5"
                  d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"
                />
              </svg>
            </button>
          </div>
          <p v-if="fieldErrors.password" class="text-xs text-red-600 mt-1">
            {{ fieldErrors.password }}
          </p>
        </div>

        <!-- Error message -->
        <p
          v-if="errorMessage"
          class="text-red-600 text-xs text-center animate-pulse"
        >
          {{ errorMessage }}
        </p>

        <!-- Submit button -->
        <button
          type="submit"
          :disabled="isLoading"
          class="w-full bg-sumi text-washi py-3 uppercase tracking-[0.2em] text-xs hover:bg-stone-800 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
        >
          <span
            v-if="isLoading"
            class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"
          ></span>
          {{ isLogin ? "登入" : "註冊" }}
        </button>
      </form>

      <!-- Toggle login/register -->
      <div class="mt-8 text-center">
        <button
          @click="toggleMode"
          class="text-xs text-stone-500 border-b border-transparent hover:border-stone-500 transition-colors"
        >
          {{ isLogin ? "還沒有帳號？立即註冊" : "已有帳號？立即登入" }}
        </button>
      </div>

      <!-- Test credentials hint (for development) -->
      <div
        v-if="isLogin"
        class="mt-6 p-4 bg-stone-100/50 text-xs text-stone-500"
      ></div>
    </div>
  </div>
</template>
