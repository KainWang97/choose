<script setup>
import { useToast } from "../composables/useToast.js";

const { toasts, removeToast } = useToast();

// Toast 類型配置
const typeConfig = {
  success: {
    icon: "check",
    bgClass: "bg-stone-50 border-stone-300",
    iconClass: "text-emerald-600 border-emerald-300",
  },
  error: {
    icon: "x",
    bgClass: "bg-stone-50 border-stone-300",
    iconClass: "text-red-500 border-red-300",
  },
  info: {
    icon: "info",
    bgClass: "bg-stone-50 border-stone-300",
    iconClass: "text-blue-500 border-blue-300",
  },
  warning: {
    icon: "warning",
    bgClass: "bg-stone-50 border-stone-300",
    iconClass: "text-amber-500 border-amber-300",
  },
};

const getConfig = (type) => typeConfig[type] || typeConfig.info;
const needsCloseButton = (type) => type !== "success";
</script>

<template>
  <Teleport to="body">
    <div
      class="fixed top-4 left-1/2 -translate-x-1/2 z-[300] flex flex-col items-center gap-3 pointer-events-none"
    >
      <TransitionGroup name="toast">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          class="pointer-events-auto flex items-start gap-3 px-4 py-3 bg-white shadow-lg rounded-sm border min-w-[280px] max-w-[380px]"
          :class="getConfig(toast.type).bgClass"
        >
          <!-- Icon -->
          <div
            class="w-8 h-8 rounded-full border flex items-center justify-center flex-shrink-0"
            :class="getConfig(toast.type).iconClass"
          >
            <!-- Success Icon (Checkmark) -->
            <svg
              v-if="getConfig(toast.type).icon === 'check'"
              class="w-4 h-4"
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

            <!-- Error Icon (X) -->
            <svg
              v-else-if="getConfig(toast.type).icon === 'x'"
              class="w-4 h-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>

            <!-- Info Icon -->
            <svg
              v-else-if="getConfig(toast.type).icon === 'info'"
              class="w-4 h-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>

            <!-- Warning Icon -->
            <svg
              v-else-if="getConfig(toast.type).icon === 'warning'"
              class="w-4 h-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
              />
            </svg>
          </div>

          <!-- Message -->
          <p class="flex-1 text-sm text-stone-700 leading-relaxed pt-1">
            {{ toast.message }}
          </p>

          <!-- Close Button (only for non-success types) -->
          <button
            v-if="needsCloseButton(toast.type)"
            @click="removeToast(toast.id)"
            class="flex-shrink-0 text-stone-400 hover:text-stone-600 transition-colors p-1 -mr-1"
            aria-label="關閉"
          >
            <svg
              class="w-4 h-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

.toast-move {
  transition: transform 0.3s ease;
}
</style>
