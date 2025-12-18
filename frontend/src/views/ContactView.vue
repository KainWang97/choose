<script setup>
/**
 * ContactView - 聯絡我們頁面
 */
import { inject } from "vue";
import ContactSection from "../components/ContactSection.vue";

// 從 App.vue 注入資料
const user = inject("user");

// 從 App.vue 注入方法
const handleContactSubmit = inject("handleContactSubmit");
</script>

<template>
  <div class="pt-24 min-h-screen bg-washi">
    <ContactSection
      :initial-name="user?.name || ''"
      :initial-email="user?.email || ''"
      @submit="handleContactSubmit"
    />
  </div>
</template>
