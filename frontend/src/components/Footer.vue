<script setup>
import { useRouter } from "vue-router";

const router = useRouter();

const goToContact = () => {
  router.push("/contact");
};

const goToCollection = () => {
  router.push("/collection");
};

const goHome = () => {
  router.push("/");
};

const goToReturnPolicy = () => {
  router.push("/return-policy");
};

const goToPaymentGuide = () => {
  router.push("/payment-guide");
};
</script>

<template>
  <footer class="bg-sumi text-stone-400 py-16 px-6">
    <div
      class="max-w-7xl mx-auto flex flex-col md:flex-row justify-around items-center"
    >
      <div>
        <h2 class="text-2xl font-serif text-washi mb-4 tracking-widest">
          CHOOSE
        </h2>
        <div class="md:block hidden">
          <p class="text-xs font-light max-w-xs leading-relaxed">
            為生活所選，依品味所選。<br />
            選擇有品味的生活，更好的選擇。
          </p>
        </div>
      </div>

      <div class="flex gap-12 text-xs tracking-widest uppercase">
        <div class="flex flex-col gap-4">
          <span class="text-washi">Shop</span>

          <button
            @click="goToCollection"
            class="hover:text-washi transition-colors text-left"
          >
            所有商品
          </button>
        </div>
        <div class="flex flex-col gap-4">
          <span class="text-washi">Support</span>
          <button
            @click="goToContact"
            class="hover:text-washi transition-colors text-left"
          >
            聯絡我們
          </button>
          <button
            @click="goToPaymentGuide"
            class="hover:text-washi transition-colors text-left"
          >
            匯款說明
          </button>
          <button
            @click="goToReturnPolicy"
            class="hover:text-washi transition-colors text-left"
          >
            退換貨說明
          </button>
        </div>
      </div>
    </div>

    <div
      class="max-w-7xl mx-auto mt-16 pt-6 border-t border-stone-800 text-[10px] flex justify-center"
    >
      <span>© 2025 CHOOSE SELECT </span>

      <span class="mx-4">Taipei</span>
    </div>
  </footer>
</template>
