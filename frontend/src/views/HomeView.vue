<script setup>
/**
 * HomeView - 首頁
 * 包含 Hero 和 New Arrivals 區塊
 */
import { inject } from "vue";
import { useRouter } from "vue-router";
import Hero from "../components/Hero.vue";
import NewArrivalsSection from "../components/NewArrivalsSection.vue";

const router = useRouter();

// 從 App.vue 注入資料
const newArrivalsProducts = inject("newArrivalsProducts");

// 從 App.vue 注入方法
const handleProductClick = inject("handleProductClick");

// 點擊 Hero 跳轉到 Collection
const handleExplore = () => {
  router.push("/collection");
};
</script>

<template>
  <div>
    <Hero @explore="handleExplore" />
    <NewArrivalsSection
      :products="newArrivalsProducts"
      @product-click="handleProductClick"
    />
  </div>
</template>
