<script setup>
import { ref, computed, onMounted, watch } from "vue";
import ProductCard from "./ProductCard.vue";
import FilterBar from "./FilterBar.vue";
import { api } from "../services/api.js";

const props = defineProps({
  products: Array,
  categories: Array,
  searchQuery: {
    type: String,
    default: "",
  },
});

const emit = defineEmits(["product-click"]);

const activeCategory = ref("All");
const searchResults = ref(null);
const isSearching = ref(false);

onMounted(() => {
  window.scrollTo({ top: 0, behavior: "smooth" });
});

// 監聽搜尋關鍵字變化
watch(
  () => props.searchQuery,
  async (keyword) => {
    if (keyword) {
      isSearching.value = true;
      try {
        searchResults.value = await api.products.search(keyword);
      } catch (error) {
        console.error("Search failed:", error);
        searchResults.value = [];
      } finally {
        isSearching.value = false;
      }
    } else {
      searchResults.value = null;
    }
  },
  { immediate: true }
);

// 只顯示有上架商品且有庫存的種類
const categoryOptions = computed(() => {
  // 取得所有有商品的種類名稱
  const categoriesWithProducts = props.categories.filter((cat) => {
    return props.products.some(
      (product) =>
        product.category === cat.name &&
        product.isListed !== false &&
        (product.totalStock || 0) > 0
    );
  });
  const names = categoriesWithProducts.map((c) => c.name);
  return ["All", ...names];
});

// 決定要顯示的商品（搜尋結果或全部）
const displayProducts = computed(() => {
  const source =
    searchResults.value !== null ? searchResults.value : props.products;
  return source.filter((product) => {
    const isListed = product.isListed !== false;
    const hasStock = (product.totalStock || 0) > 0;
    const matchesCategory =
      searchResults.value !== null ||
      activeCategory.value === "All" ||
      product.category === activeCategory.value;
    return isListed && hasStock && matchesCategory;
  });
});
</script>

<template>
  <div class="min-h-screen bg-washi pt-32 pb-24">
    <div class="max-w-7xl mx-auto px-6">
      <div class="flex flex-col items-center mb-16 space-y-6 text-center">
        <h2 class="text-4xl font-serif font-semibold text-sumi tracking-wider">
          {{ searchQuery ? "Search Results" : "All Items" }}
        </h2>
        <p class="text-xs tracking-[0.2em] text-stone-500 uppercase">
          {{
            searchQuery
              ? `Found ${displayProducts.length} items`
              : "Life is what you choose"
          }}
        </p>
      </div>

      <!-- 只在非搜尋模式顯示分類篩選 -->
      <FilterBar
        v-if="!searchQuery"
        :categories="categoryOptions"
        :activeCategory="activeCategory"
        @select-category="activeCategory = $event"
      />

      <!-- 搜尋中 -->
      <div v-if="isSearching" class="text-center py-20 text-stone-500">
        搜尋中...
      </div>

      <!-- 商品列表 -->
      <div
        v-else-if="displayProducts.length > 0"
        class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-x-8 gap-y-20 animate-fade-in"
      >
        <ProductCard
          v-for="(product, index) in displayProducts"
          :key="product.id"
          :product="product"
          :index="index"
          @click="emit('product-click', product)"
        />
      </div>

      <!-- 無結果 -->
      <div v-else class="text-center py-20 text-stone-500 font-serif italic">
        {{
          searchQuery
            ? "找不到符合的商品"
            : activeCategory === "All" && products.length > 0
            ? "All items are currently sold out."
            : "No items found in this category."
        }}
      </div>
    </div>
  </div>
</template>
