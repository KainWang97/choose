<script setup>
/**
 * ProductView - 商品詳情頁
 */
import { inject, computed, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import ProductDetail from "../components/ProductDetail.vue";
import { decodeProductSlug } from "../utils/productSlug.js";

const route = useRoute();
const router = useRouter();

// 從 App.vue 注入資料和方法
const products = inject("products");
const cart = inject("cart");
const handleAddToCart = inject("handleAddToCart");

// 解碼 slug 獲取產品 ID
const productId = computed(() => {
  const slug = route.params.slug;
  return decodeProductSlug(slug);
});

// 查找對應產品
const activeProduct = computed(() => {
  if (!productId.value) return null;
  return products.value.find((p) => p.id === productId.value);
});

// 計算購物車中該規格的數量
const currentQuantityForVariant = (variantId) => {
  const item = cart.value.find((i) => i.variant.id === variantId);
  return item?.quantity || 0;
};

// 返回上一頁
const goBack = () => {
  router.back();
};

// 若產品不存在，返回首頁
onMounted(() => {
  if (!activeProduct.value && products.value.length > 0) {
    router.push("/");
  }
});

watch(
  () => products.value,
  () => {
    if (!activeProduct.value && products.value.length > 0) {
      router.push("/");
    }
  }
);
</script>

<template>
  <div class="pt-24">
    <!-- 返回箭頭 -->
    <div class="max-w-6xl mx-auto px-6 mb-4">
      <button
        @click="goBack"
        class="inline-flex items-center gap-2 text-stone-500 hover:text-sumi transition-colors group"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-5 w-5 transform group-hover:-translate-x-1 transition-transform"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M10 19l-7-7m0 0l7-7m-7 7h18"
          />
        </svg>
        <span class="text-sm">返回</span>
      </button>
    </div>

    <ProductDetail
      v-if="activeProduct"
      :product="activeProduct"
      :current-quantity-for-variant="currentQuantityForVariant"
      @add-to-cart="handleAddToCart"
    />
    <div
      v-else
      class="flex items-center justify-center min-h-[50vh] text-stone-400"
    >
      載入中...
    </div>
  </div>
</template>
