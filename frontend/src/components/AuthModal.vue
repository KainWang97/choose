<script setup>
import { ref, computed, inject } from "vue";
import { api } from "../services/api.js";

const props = defineProps({
  isOpen: Boolean,
  redirect: String, // 註冊後驗證成功跳轉目標
});

const emit = defineEmits(["close", "login"]);

// 注入購物車以便在註冊時保存
const cart = inject("cart");

// Form state
const isLogin = ref(true);
const email = ref("");
const password = ref("");
const name = ref("");
const errorMessage = ref("");
const isLoading = ref(false);
const showPassword = ref(false);
const passwordFocused = ref(false);
const registrationSuccess = ref(false);
const magicLinkMode = ref(false);
const magicLinkSent = ref(false);

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

// 驗證註冊表單 (不需密碼)
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

  fieldErrors.value = errors;
  return Object.keys(errors).length === 0;
};

// 信箱遮罩函數：顯示首字元 + *** + @domain
const maskEmail = (emailStr) => {
  if (!emailStr || typeof emailStr !== "string") return "";
  const [localPart, domain] = emailStr.split("@");
  if (!domain) return emailStr;
  // 只顯示首字元，其餘用 * 遮罩
  const maskedLocal = localPart.charAt(0) + "***";
  return `${maskedLocal}@${domain}`;
};

// Reset form
const resetForm = () => {
  email.value = "";
  password.value = "";
  name.value = "";
  errorMessage.value = "";
  fieldErrors.value = {};
  registrationSuccess.value = false;
  magicLinkMode.value = false;
  magicLinkSent.value = false;
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

// Handle register (不需密碼)
const handleRegister = async () => {
  errorMessage.value = "";

  if (!validateRegisterForm()) {
    return;
  }

  isLoading.value = true;
  try {
    await api.auth.register({
      name: name.value,
      email: email.value,
    });

    // 儲存驗證後跳轉路徑到 localStorage
    if (props.redirect) {
      localStorage.setItem("postVerifyRedirect", props.redirect);
    } else {
      localStorage.removeItem("postVerifyRedirect");
    }

    // 儲存購物車到 localStorage（避免頁面重載後遺失）
    if (cart && cart.value && cart.value.length > 0) {
      localStorage.setItem("pendingCart", JSON.stringify(cart.value));
    }

    // 顯示成功訊息，提示用戶查收驗證信
    registrationSuccess.value = true;
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

// Handle Magic Link login
const handleMagicLink = async () => {
  errorMessage.value = "";
  fieldErrors.value = {};

  if (!email.value?.trim()) {
    fieldErrors.value.email = "請輸入 Email";
    return;
  }
  if (!isValidEmail(email.value)) {
    fieldErrors.value.email = "請輸入有效的 Email 格式";
    return;
  }

  isLoading.value = true;
  try {
    await api.auth.loginMagic(email.value);

    // 儲存驗證後跳轉路徑到 localStorage（與註冊相同邏輯）
    if (props.redirect) {
      localStorage.setItem("postVerifyRedirect", props.redirect);
    } else {
      localStorage.removeItem("postVerifyRedirect");
    }

    // 儲存購物車到 localStorage（避免頁面重載後遺失）
    if (cart && cart.value && cart.value.length > 0) {
      localStorage.setItem("pendingCart", JSON.stringify(cart.value));
    }

    magicLinkSent.value = true;
  } catch (error) {
    errorMessage.value = "發送失敗，請稍後再試";
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
  magicLinkMode.value = false; // 切換時重置 Magic Link 模式
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
        @click="
          resetForm();
          emit('close');
        "
        class="absolute top-4 right-4 text-stone-400 hover:text-sumi"
      >
        ✕
      </button>

      <h2 class="text-2xl font-serif text-center mb-8 text-sumi">
        {{ magicLinkMode ? "信箱驗證登入" : isLogin ? "登入" : "註冊會員" }}
      </h2>

      <!-- Registration Success Message -->
      <div v-if="registrationSuccess" class="text-center space-y-6">
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
        <p class="text-stone-600">
          註冊成功！請至信箱查收驗證信，<br />點擊連結後將自動登入。
        </p>
        <p class="text-sm text-stone-700 font-medium">
          {{ maskEmail(email) }}
        </p>
        <p class="text-xs text-stone-500">驗證後建議至個人資料設定專屬密碼</p>
        <button
          type="button"
          @click="
            resetForm();
            emit('close');
          "
          class="w-full bg-sumi text-washi py-3 uppercase tracking-[0.2em] text-xs hover:bg-stone-800 transition-colors"
        >
          關閉
        </button>
      </div>

      <!-- Magic Link Sent Message -->
      <div v-else-if="magicLinkSent" class="text-center space-y-6">
        <div
          class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto"
        >
          <svg
            class="w-8 h-8 text-blue-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
            ></path>
          </svg>
        </div>
        <p class="text-stone-600">
          登入連結已發送！<br />請至信箱查收並點擊連結登入。
        </p>
        <p class="text-sm text-stone-700 font-medium">
          {{ maskEmail(email) }}
        </p>
        <p class="text-xs text-stone-500">連結有效期限為 15 分鐘</p>
        <button
          type="button"
          @click="
            resetForm();
            emit('close');
          "
          class="w-full bg-sumi text-washi py-3 uppercase tracking-[0.2em] text-xs hover:bg-stone-800 transition-colors"
        >
          關閉
        </button>
      </div>

      <!-- Form -->
      <form
        v-else
        class="space-y-6"
        @submit.prevent="magicLinkMode ? handleMagicLink() : handleSubmit()"
      >
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
            autocomplete="name"
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
            type="email"
            v-model="email"
            :placeholder="isLogin ? '' : '請輸入 Email'"
            autocomplete="email"
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

        <!-- Password field (login only) -->
        <div v-if="isLogin && !magicLinkMode">
          <label
            class="block text-xs uppercase tracking-widest text-stone-500 mb-2"
            >密碼</label
          >
          <div class="relative">
            <input
              :type="showPassword ? 'text' : 'password'"
              v-model="password"
              :placeholder="isLogin ? '' : '設定密碼 (至少 6 字元)'"
              autocomplete="current-password"
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
          <!-- Forgot password link (login only) -->
          <div v-if="isLogin" class="text-right">
            <router-link
              to="/forgot-password"
              @click="emit('close')"
              class="text-xs text-stone-500 hover:text-sumi transition-colors"
            >
              忘記密碼？
            </router-link>
          </div>
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
          {{ magicLinkMode ? "發送登入連結" : isLogin ? "登入" : "註冊" }}
        </button>

        <!-- Magic Link toggle (login mode only) -->
        <div v-if="isLogin" class="text-center pt-2">
          <button
            type="button"
            @click="magicLinkMode = !magicLinkMode"
            class="auth-toggle-btn"
          >
            <template v-if="magicLinkMode">
              使用<span class="auth-keyword">密碼登入</span>
            </template>
            <template v-else>
              使用<span class="auth-keyword">信箱驗證登入</span>
            </template>
          </button>
        </div>
      </form>

      <!-- Toggle login/register -->
      <div class="mt-8 text-center">
        <button @click="toggleMode" class="auth-toggle-btn">
          <template v-if="isLogin">
            還沒有帳號？<span class="auth-keyword">立即註冊</span>
          </template>
          <template v-else>
            已有帳號？<span class="auth-keyword">立即登入</span>
          </template>
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

<style scoped>
/* 切換按鈕基礎樣式 */
.auth-toggle-btn {
  font-size: 0.875rem;
  color: #78716c;
  transition: color 0.2s;
}

.auth-toggle-btn:hover {
  color: #3e3a36;
}

/* 關鍵字強調樣式 */
.auth-keyword {
  font-weight: 500;
  color: #3e1e04;
  text-underline-offset: 1px;
  transition: color ease-in-out 0.3s;
}
.auth-keyword:hover {
  font-weight: 700;
  font-size: 0.9rem;
  color: #000000;
  text-decoration: underline;
}
</style>
