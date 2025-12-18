<script setup>
import { ref, onMounted, onUnmounted, computed } from "vue";
import { getThumbnailUrl } from "../utils/imageHelper.js";

const props = defineProps({
  product: Object,
  index: Number,
});

const emit = defineEmits(["click"]);

const cardRef = ref(null);
const isVisible = ref(false);
let observer = null;

onMounted(() => {
  observer = new IntersectionObserver(
    ([entry]) => {
      if (entry.isIntersecting) {
        isVisible.value = true;
        if (cardRef.value && observer) observer.unobserve(cardRef.value);
      }
    },
    { threshold: 0.15, rootMargin: "50px" }
  );

  if (cardRef.value) observer.observe(cardRef.value);
});

onUnmounted(() => {
  if (observer) observer.disconnect();
});

const isEven = computed(() => props.index % 2 === 0);
</script>

<template>
  <div
    ref="cardRef"
    @click="emit('click', product)"
    class="relative group cursor-pointer transition-all duration-1000 ease-out flex flex-col gap-4"
    :class="[
      isVisible
        ? 'opacity-100 translate-x-0'
        : isEven
        ? 'opacity-0 -translate-x-12'
        : 'opacity-0 translate-x-12',
    ]"
    :style="{ transitionDelay: `${(index % 2) * 100}ms` }"
  >
    <div class="relative overflow-hidden w-full aspect-[3/4] bg-stone-200">
      <img
        :src="getThumbnailUrl(product.imageUrl)"
        :alt="product.name"
        loading="lazy"
        class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
      />
      <div
        class="absolute inset-0 bg-stone-900/0 group-hover:bg-stone-900/5 transition-colors duration-500"
      />

      <div
        class="absolute bottom-6 left-1/2 -translate-x-1/2 opacity-0 group-hover:opacity-100 transition-all duration-500 translate-y-4 group-hover:translate-y-0"
      >
        <span
          class="bg-washi/90 px-6 py-2 text-xs uppercase tracking-widest text-sumi border border-stone-200 backdrop-blur-sm"
        >
          View
        </span>
      </div>
    </div>

    <div class="text-center space-y-1">
      <h3 class="font-serif text-lg text-sumi">{{ product.name }}</h3>
      <p class="text-xs text-stone-500 tracking-wider uppercase">
        {{ product.category }}
      </p>
      <p class="text-sm font-light text-stone-800 mt-2">${{ product.price }}</p>
    </div>
  </div>
</template>
