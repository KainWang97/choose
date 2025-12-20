<script setup>
import { watch } from "vue";

const props = defineProps({
  isOpen: Boolean,
  title: String,
  message: String,
  confirmText: String,
  cancelText: String,
  variant: String,
});

const emit = defineEmits(["confirm", "cancel"]);

// 防止背景滾動
watch(
  () => props.isOpen,
  (isOpen) => {
    if (isOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "";
    }
  }
);

const handleConfirm = () => {
  emit("confirm");
};

const handleCancel = () => {
  emit("cancel");
};

// 根據 variant 決定按鈕顏色
const confirmButtonClass = {
  danger: "bg-red-600 hover:bg-red-700 text-white",
  warning: "bg-amber-600 hover:bg-amber-700 text-white",
  info: "bg-sumi hover:bg-stone-800 text-washi",
};
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div
        v-if="isOpen"
        class="fixed inset-0 z-[200] flex items-center justify-center p-4"
      >
        <!-- Backdrop -->
        <div
          class="absolute inset-0 bg-black/50 backdrop-blur-sm"
          @click="handleCancel"
        />

        <!-- Modal -->
        <Transition name="scale">
          <div
            v-if="isOpen"
            class="relative bg-white w-full max-w-md p-8 shadow-2xl rounded-sm"
            @click.stop
          >
            <!-- Title -->
            <h3 v-if="title" class="text-xl font-serif text-sumi mb-4">
              {{ title }}
            </h3>

            <!-- Message -->
            <p class="text-stone-600 font-light leading-relaxed mb-8">
              {{ message }}
            </p>

            <!-- Buttons -->
            <div class="flex justify-end gap-3">
              <button
                @click="handleCancel"
                class="px-6 py-2.5 border-1 border-stone-300 text-stone-700 text-sm uppercase tracking-widest hover:bg-stone-100 hover:border-stone-300 transition-colors"
              >
                {{ cancelText || "取消" }}
              </button>
              <button
                @click="handleConfirm"
                class="px-6 py-2.5 border-1 border-stone-300 text-sm uppercase tracking-widest hover:bg-stone-200 hover:border-stone-600 transition-colors"
                :class="confirmButtonClass[variant || 'info']"
              >
                {{ confirmText || "確認" }}
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.scale-enter-active,
.scale-leave-active {
  transition: all 0.2s ease;
}

.scale-enter-from,
.scale-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>
