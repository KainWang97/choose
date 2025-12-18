<script setup>
/**
 * CollectionView - 商品列表頁
 */
import { inject, computed, watch } from "vue";
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
  </div>
</template>
