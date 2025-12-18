<script setup>
import SeasonalItem from "./SeasonalItem.vue";

defineProps({
  products: Array,
});

const emit = defineEmits(["product-click"]);
</script>

<template>
  <section class="bg-washi py-32 overflow-hidden">
    <div class="max-w-7xl mx-auto px-6 mb-24 text-center">
      <h2 class="text-xl font-serif text-sumi tracking-widest mb-4">
        季節選擇
      </h2>
      <p class="text-xs uppercase tracking-[0.2em] text-stone-500">
        Seasonal Essentials
      </p>
    </div>

    <div class="max-w-6xl mx-auto space-y-32 px-6">
      <SeasonalItem
        v-for="(product, index) in products"
        :key="product.id"
        :product="product"
        :index="index"
        @click="emit('product-click', product)"
      />
    </div>
  </section>
</template>
