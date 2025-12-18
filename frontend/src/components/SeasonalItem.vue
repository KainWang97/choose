<script setup>
import { ref, onMounted, onUnmounted, computed } from "vue";
import { getLargeImageUrl } from "../utils/imageHelper.js";

const props = defineProps({
  product: Object,
  index: Number,
});

const emit = defineEmits(["click"]);

const itemRef = ref(null);
const isVisible = ref(false);
let observer = null;

onMounted(() => {
  observer = new IntersectionObserver(
    ([entry]) => {
      if (entry.isIntersecting) {
        isVisible.value = true;
        if (itemRef.value && observer) observer.unobserve(itemRef.value);
      }
    },
    { threshold: 0.2 }
  );

  if (itemRef.value) observer.observe(itemRef.value);
});

onUnmounted(() => {
  if (observer) observer.disconnect();
});

const isEven = computed(() => props.index % 2 === 0);
</script>

<template>
  <div
    ref="itemRef"
    @click="emit('click', product)"
    class="flex flex-col md:flex-row items-center gap-12 md:gap-24 cursor-pointer group transition-all duration-1000 ease-out transform"
    :class="[
      isVisible
        ? 'opacity-100 translate-x-0'
        : isEven
        ? 'opacity-0 -translate-x-24'
        : 'opacity-0 translate-x-24',
      !isEven ? 'md:flex-row-reverse' : '',
    ]"
  >
    <!-- Large Image -->
    <div
      class="w-full md:w-3/5 aspect-[4/3] overflow-hidden relative bg-stone-200"
    >
      <img
        :src="getLargeImageUrl(product.imageUrl)"
        :alt="product.name"
        loading="lazy"
        class="w-full h-full object-cover transition-transform duration-1000 group-hover:scale-105"
      />
      <div
        class="absolute inset-0 bg-stone-900/0 group-hover:bg-stone-900/10 transition-colors duration-500"
      />
    </div>

    <!-- Editorial Text -->
    <div class="w-full md:w-2/5 space-y-6 text-center md:text-left px-6">
      <div class="space-y-2">
        <span class="text-xs uppercase tracking-[0.3em] text-stone-400"
          >Seasonal Feature 0{{ index + 1 }}</span
        >
        <h3 class="text-3xl md:text-4xl font-serif text-sumi">
          {{ product.name }}
        </h3>
      </div>
      <p
        class="text-sm font-light text-stone-600 leading-relaxed max-w-sm mx-auto md:mx-0"
      >
        {{ product.description }}
      </p>
      <button
        class="text-xs uppercase tracking-widest border-b border-sumi pb-1 hover:text-stone-500 hover:border-stone-500 transition-colors"
      >
        View Details
      </button>
    </div>
  </div>
</template>
