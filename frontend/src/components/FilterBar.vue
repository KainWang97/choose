<script setup>
defineProps({
  categories: Array,
  activeCategory: String,
});

const emit = defineEmits(["select-category"]);
</script>

<template>
  <div class="flex flex-wrap justify-center gap-x-8 gap-y-4 mb-16 px-4">
    <button
      v-for="category in categories"
      :key="category"
      @click="emit('select-category', category)"
      class="text-xs uppercase tracking-[0.15em] transition-all duration-300 relative"
      :class="
        activeCategory === category
          ? 'text-sumi'
          : 'text-stone-400 hover:text-stone-600'
      "
    >
      {{ category }}
      <span
        class="absolute -bottom-2 left-1/2 transform -translate-x-1/2 w-1 h-1 rounded-full bg-sumi transition-all duration-300"
        :class="
          activeCategory === category
            ? 'opacity-100 scale-100'
            : 'opacity-0 scale-0'
        "
      />
    </button>
  </div>
</template>
