<script setup>
import { ref, watch } from "vue";

const props = defineProps({
  show: Boolean,
  message: String,
  duration: Number,
});

const emit = defineEmits(["close"]);

const isVisible = ref(false);

watch(
  () => props.show,
  (newVal) => {
    if (newVal) {
      isVisible.value = true;
      // Auto close after duration
      setTimeout(() => {
        isVisible.value = false;
        emit("close");
      }, props.duration || 1500);
    }
  }
);
</script>

<template>
  <Teleport to="body">
    <Transition name="toast">
      <div
        v-if="isVisible"
        class="fixed inset-0 z-[200] flex items-center justify-center pointer-events-none"
      >
        <div
          class="bg-white shadow-2xl rounded-sm px-10 py-8 flex flex-col items-center gap-4 pointer-events-auto animate-fade-in-up"
        >
          <!-- Checkmark Icon -->
          <div
            class="w-14 h-14 rounded-full border-2 border-stone-300 flex items-center justify-center"
          >
            <svg
              class="w-7 h-7 text-stone-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M5 13l4 4L19 7"
              />
            </svg>
          </div>
          <!-- Message -->
          <p class="text-sm text-stone-700 tracking-wide">
            {{ message || "已加入購物車" }}
          </p>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: scale(0.9);
}
</style>
