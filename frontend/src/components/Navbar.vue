<script setup>
import { computed, ref, onMounted, onUnmounted } from "vue";
import { useConfirm } from "../composables/useConfirm.js";

const props = defineProps({
  isScrolled: Boolean,
  user: Object,
  cartItems: Array,
});

const emit = defineEmits([
  "open-auth",
  "open-cart",
  "open-account",
  "open-admin",
  "home",
  "collection",
  "logout",
  "search",
]);

const { confirm } = useConfirm();
const isLoggingOut = ref(false);
const isSearchOpen = ref(false);
const searchQuery = ref("");
const searchInput = ref(null);
const searchContainer = ref(null);

// 點擊外部區域收回搜尋框
const handleClickOutside = (event) => {
  if (
    isSearchOpen.value &&
    searchContainer.value &&
    !searchContainer.value.contains(event.target)
  ) {
    isSearchOpen.value = false;
    searchQuery.value = "";
  }
};

onMounted(() => {
  document.addEventListener("click", handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
});

const handleLogout = async () => {
  const confirmed = await confirm({
    title: "登出確認",
    message: "確定要登出嗎？",
    confirmText: "登出",
    cancelText: "取消",
    variant: "warning",
  });

  if (!confirmed) return;

  isLoggingOut.value = true;
  setTimeout(() => {
    emit("logout");
    isLoggingOut.value = false;
  }, 800);
};

const toggleSearch = () => {
  isSearchOpen.value = !isSearchOpen.value;
  if (isSearchOpen.value) {
    setTimeout(() => searchInput.value?.focus(), 100);
  } else {
    searchQuery.value = "";
  }
};

const handleSearch = () => {
  const query = searchQuery.value.trim();
  if (query) {
    emit("search", query);
    isSearchOpen.value = false;
    searchQuery.value = "";
  }
};

const cartCount = computed(() =>
  props.cartItems.reduce((acc, item) => acc + item.quantity, 0)
);
const isAdmin = computed(() => props.user?.role === "ADMIN");
</script>

<template>
  <nav
    class="fixed top-0 left-0 w-full z-50 transition-all duration-700 ease-in-out border-b"
    :class="[
      isScrolled
        ? 'bg-washi/95 backdrop-blur-sm py-4 border-stone-200/100 shadow-sm'
        : 'bg-white/70 backdrop-blur-sm py-6 border-stone-200/50',
    ]"
  >
    <div class="max-w-7xl mx-auto px-6 flex justify-between items-center">
      <button
        @click="emit('home')"
        class="text-2xl tracking-[0.2em] font-serif text-sumi hover:opacity-70 transition-opacity uppercase cursor-pointer"
      >
        Choose
      </button>

      <div
        class="flex items-center gap-6 text-sm tracking-widest text-stone-600 font-light"
      >
        <!-- Admin Link -->
        <button
          v-if="isAdmin"
          @click="emit('open-admin')"
          class="text-red-900 font-medium hover:text-red-700 transition-colors uppercase border-b border-red-900/20"
        >
          Dashboard
        </button>

        <!-- Collection (非管理員顯示) -->
        <button
          v-if="!isAdmin"
          @click="emit('collection')"
          class="hover:text-sumi transition-all duration-300 group relative"
          title="Collection"
        >
          <!-- 大螢幕顯示文字 -->
          <span class="hidden md:block relative cursor-pointer">
            COLLECTION
            <!-- 底線動畫效果 -->
            <span
              class="absolute left-0 -bottom-1 w-0 h-[1px] bg-sumi transition-all duration-300 group-hover:w-full"
            ></span>
          </span>
          <!-- 小螢幕顯示圖示 -->
          <svg
            class="w-6 h-6 stroke-current stroke-1 group-hover:stroke-2 group-hover:scale-110 transition-all duration-300 md:hidden"
            fill="none"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M15.75 10.5V6a3.75 3.75 0 10-7.5 0v4.5m11.356-1.993l1.263 12c.07.665-.45 1.243-1.119 1.243H4.25a1.125 1.125 0 01-1.12-1.243l1.264-12A1.125 1.125 0 015.513 7.5h12.974c.576 0 1.059.435 1.119 1.007zM8.625 10.5a.375.375 0 11-.75 0 .375.375 0 01.75 0zm7.5 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z"
            />
          </svg>
        </button>
        <!-- Search (非管理員顯示) -->
        <div
          v-if="!isAdmin"
          ref="searchContainer"
          class="flex items-center gap-2"
        >
          <input
            v-show="isSearchOpen"
            ref="searchInput"
            v-model="searchQuery"
            @keydown.enter="handleSearch"
            @keydown.escape="toggleSearch"
            type="text"
            placeholder="搜尋商品..."
            class="w-40 md:w-56 px-3 py-1.5 text-sm border border-stone-300 rounded-sm bg-white/80 focus:outline-none focus:border-stone-500 transition-all"
          />
          <button
            @click="isSearchOpen ? handleSearch() : toggleSearch()"
            class="hover:text-sumi transition-colors group"
            title="搜尋"
          >
            <svg
              class="w-5 h-5 stroke-current stroke-1 group-hover:stroke-2 transition-all"
              fill="none"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z"
              />
            </svg>
          </button>
        </div>
        <!-- Cart Icon (非管理員顯示) -->
        <button
          v-if="!isAdmin"
          @click="emit('open-cart')"
          class="hover:text-sumi transition-colors flex items-center gap-1 group"
        >
          <svg
            class="w-6 h-6 stroke-current stroke-1 group-hover:stroke-2 transition-all"
            fill="none"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M2.25 3h1.386c.51 0 .955.343 1.087.835l.383 1.437M7.5 14.25a3 3 0 00-3 3h15.75m-12.75-3h11.218c1.121-2.3 2.1-4.684 2.924-7.138a60.114 60.114 0 00-16.536-1.84M7.5 14.25L5.106 5.272M6 20.25a.75.75 0 11-1.5 0 .75.75 0 011.5 0zm12.75 0a.75.75 0 11-1.5 0 .75.75 0 011.5 0z"
            />
          </svg>
          <span
            v-if="cartCount > 0"
            class="text-sm font-light text-stone-600 group-hover:text-sumi transition-colors translate-y-[2px]"
          >
            {{ cartCount }}
          </span>
        </button>
        <!-- Account Icon (非管理員顯示) -->
        <button
          v-if="!isAdmin"
          @click="user ? emit('open-account') : emit('open-auth')"
          class="hover:text-sumi transition-colors group relative"
          :title="user ? 'My Account' : 'Login'"
        >
          <svg
            class="w-6 h-6 stroke-current stroke-1 group-hover:stroke-2 transition-all"
            fill="none"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
            />
          </svg>
        </button>

        <!-- Logout -->
        <button
          v-if="user"
          @click="handleLogout"
          :disabled="isLoggingOut"
          class="hover:text-sumi transition-colors uppercase text-xs tracking-widest flex items-center gap-2 disabled:opacity-50 cursor-pointer"
        >
          <span
            v-if="isLoggingOut"
            class="w-4 h-4 border-2 border-stone-400 border-t-transparent rounded-full animate-spin"
          ></span>
          {{ isLoggingOut ? "..." : "LOGOUT" }}
        </button>
      </div>
    </div>
  </nav>
</template>
