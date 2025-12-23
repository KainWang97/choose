<script setup>
/**
 * CollectionView - 商品列表頁
 */
import { inject, computed, ref, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import CollectionSection from "../components/CollectionSection.vue";

const route = useRoute();
const router = useRouter();

// 從 App.vue 注入資料
const products = inject("products");
const categories = inject("categories");

// 從 App.vue 注入方法
const handleProductClick = inject("handleProductClick");

// 搜尋關鍵字
const searchQuery = computed(() => route.query.search || "");

// 清除搜尋
const clearSearch = () => {
  router.push("/collection");
};

// Back to Top 功能
const showBackToTop = ref(false);

const handleScroll = () => {
  showBackToTop.value = window.scrollY > 300;
};

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: "smooth" });
};

onMounted(() => {
  window.addEventListener("scroll", handleScroll);
});

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll);
});
</script>

<template>
  <div>
    <!-- 搜尋結果提示 -->
    <div
      v-if="searchQuery"
      class="max-w-7xl mx-auto px-6 pt-24 flex items-center justify-between"
    >
      <p class="text-stone-600">
        搜尋「<span class="font-medium text-sumi">{{ searchQuery }}</span
        >」的結果
      </p>
      <button
        @click="clearSearch"
        class="text-sm text-stone-500 hover:text-sumi underline"
      >
        清除搜尋
      </button>
    </div>

    <CollectionSection
      :products="products"
      :categories="categories"
      :searchQuery="searchQuery"
      @product-click="handleProductClick"
    />

    <!-- Back to Top 按鈕 -->
    <Transition name="fade">
      <button
        v-show="showBackToTop"
        @click="scrollToTop"
        class="fixed bottom-8 right-8 z-50 w-12 h-12 bg-sumi/80 hover:bg-sumi text-white rounded-full shadow-lg flex items-center justify-center transition-all duration-300 hover:scale-110"
        aria-label="回到頂部"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-5 w-5"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M5 10l7-7m0 0l7 7m-7-7v18"
          />
        </svg>
      </button>
    </Transition>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
