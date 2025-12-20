<script setup>
import { ref, watch } from "vue";

const props = defineProps({
  initialName: String,
  initialEmail: String,
});

const emit = defineEmits(["submit"]);

const name = ref(props.initialName || "");
const email = ref(props.initialEmail || "");

watch(
  () => props.initialName,
  (newVal) => {
    if (newVal) name.value = newVal;
  }
);

watch(
  () => props.initialEmail,
  (newVal) => {
    if (newVal) email.value = newVal;
  }
);
const message = ref("");
const submitted = ref(false);

const showToast = ref(false);

const handleSubmit = () => {
  emit("submit", name.value, email.value, message.value);
  submitted.value = true;
  // 等待送出狀態結束後再顯示提示
  setTimeout(() => {
    submitted.value = false;
    message.value = "";
    showToast.value = true;
    setTimeout(() => {
      showToast.value = false;
    }, 1400);
  }, 2000);
};
</script>

<template>
  <section class="py-24 bg-washi">
    <div class="max-w-4xl mx-auto px-6 text-center">
      <h2 className="text-3xl font-serif text-sumi mb-12">Inquiries</h2>

      <!-- 備註說明 -->
      <div
        class="max-w-lg mx-auto mb-8 p-4 bg-stone-50 border border-stone-200 rounded"
      >
        <p class="text-sm text-stone-600 text-center leading-relaxed">
          感謝您的詢問！管理員將會儘快透過 <strong>Email</strong> 回覆您的訊息，
          請確保您填寫的電子郵件地址正確。
        </p>
      </div>

      <form
        @submit.prevent="handleSubmit"
        class="max-w-lg mx-auto space-y-6 text-left"
      >
        <div>
          <label
            class="block text-xs uppercase tracking-widest text-stone-500 mb-2"
            >Name</label
          >
          <input
            required
            type="text"
            v-model="name"
            class="w-full bg-transparent border-b border-stone-300 py-2 focus:outline-none focus:border-sumi transition-colors rounded-none"
          />
        </div>
        <div>
          <label
            class="block text-xs uppercase tracking-widest text-stone-500 mb-2"
            >Email</label
          >
          <input
            required
            type="email"
            v-model="email"
            class="w-full bg-transparent border-b border-stone-300 py-2 focus:outline-none focus:border-sumi transition-colors rounded-none"
          />
        </div>
        <div>
          <label
            class="block text-xs uppercase tracking-widest text-stone-500 mb-2"
            >Message</label
          >
          <textarea
            required
            rows="4"
            v-model="message"
            class="w-full bg-transparent border-b border-stone-300 py-2 focus:outline-none focus:border-sumi transition-colors resize-none rounded-none"
          ></textarea>
        </div>

        <div class="text-center pt-8">
          <button
            :disabled="submitted"
            class="px-12 py-3 border border-sumi text-sumi uppercase text-xs tracking-[0.2em] transition-all"
            :class="
              submitted
                ? 'bg-stone-800 text-washi border-stone-800'
                : 'hover:bg-sumi hover:text-washi'
            "
          >
            {{ submitted ? "送出中..." : "送出" }}
          </button>
        </div>
      </form>

      <!-- 提示 Toast -->
      <transition name="fade">
        <div
          v-if="showToast"
          class="fixed top-6 left-1/2 -translate-x-1/2 z-[120] bg-stone-100 border border-stone-300 text-stone-700 px-6 py-4 shadow-md rounded max-w-sm text-center"
        >
          <p class="font-medium">訊息已成功送出</p>
          <p class="text-sm mt-1 text-stone-500">
            管理員將會儘快透過 Email 回覆您
          </p>
        </div>
      </transition>
    </div>
  </section>
</template>
