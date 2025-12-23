<script setup>
import { ref } from "vue";

const isImageLoaded = ref(false);
const isExploring = ref(false);
const emit = defineEmits(["explore"]);

const handleExplore = () => {
  isExploring.value = true;
  emit("explore");
  setTimeout(() => (isExploring.value = false), 2000);
};
</script>

<template>
  <header
    class="relative w-full h-screen overflow-hidden flex items-center justify-center cursor-pointer"
    @click="handleExplore"
  >
    <!-- Background -->
    <div class="absolute inset-0 z-0">
      <img
        src="https://res.cloudinary.com/dtowrsbhe/image/upload/v1765812237/lgk4wjn7ofrhbe4ofmod.jpg"
        alt="Minimalist Interior"
        class="w-full h-full object-cover transition-opacity duration-700 ease-out"
        :class="isImageLoaded ? 'opacity-90' : 'opacity-0'"
        @load="isImageLoaded = true"
      />
      <!-- Darkening Overlay on Click -->
      <div
        class="absolute inset-0 bg-black transition-opacity duration-1000 ease-out pointer-events-none"
        :class="isExploring ? 'opacity-60' : 'opacity-0'"
      ></div>
      <div class="absolute inset-0 bg-stone-100/20 mix-blend-overlay"></div>
    </div>

    <!-- Content -->
    <div class="relative z-10 flex flex-col items-center text-center space-y-4">
      <h1 class="inline-block animate-breathe" style="animation-delay: 0s, 2s">
        <span
          class="inline-block text-stone-600 tracking-[0.3em] text-sm md:text-2xl font-medium uppercase transition-all duration-500 hover-glow"
        >
          Choose a tasteful life
        </span>
      </h1>
      <p class="inline-block animate-breathe" style="animation-delay: 1s, 2.5s">
        <span
          class="inline-block text-stone-500 text-xs tracking-widest font-medium transition-all duration-500 hover-glow"
        >
          點擊探索您的選擇
        </span>
      </p>
    </div>

    <!-- Scroll Indicator -->
    <div
      class="absolute bottom-12 left-1/2 transform -translate-x-1/2 flex flex-col items-center gap-2 opacity-50"
    >
      <span class="text-[10px] tracking-widest uppercase">Scroll</span>
      <div class="w-[1px] h-12 bg-stone-800"></div>
    </div>
  </header>
</template>

<style scoped>
.animate-breathe {
  opacity: 0;
  animation: emerge 3s ease-out forwards, breathe 6s ease-in-out infinite;
  /* Delays are set inline to coordinate the sequence */
}

@keyframes emerge {
  0% {
    opacity: 0;
    filter: blur(10px);
    transform: translateY(8px);
  }
  100% {
    opacity: 1;
    filter: blur(0);
    transform: translateY(0);
  }
}

@keyframes breathe {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.05);
  }
}

.hover-glow:hover {
  transform: scale(1.1);
  color: #1c1917; /* stone-900 */
  text-shadow: 0 0 10px rgba(255, 255, 255, 1),
    0 0 20px rgba(255, 255, 255, 0.8), 0 0 30px rgba(255, 255, 255, 0.6);
}
</style>
