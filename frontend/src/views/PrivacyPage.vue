<script setup>
import { useRouter } from "vue-router";

const router = useRouter();

const goBack = () => {
  router.back();
};
</script>

<template>
  <div class="min-h-screen bg-washi pt-32 pb-24 px-6 animate-fade-in">
    <div class="max-w-3xl mx-auto">
      <!-- Back Button -->
      <button
        @click="goBack"
        class="text-xs uppercase tracking-widest text-stone-400 hover:text-sumi mb-8 flex items-center gap-2"
      >
        ← 返回
      </button>

      <h1 class="text-3xl font-serif text-sumi mb-8">隱私權政策</h1>

      <div class="prose prose-stone max-w-none space-y-8">
        <!-- 前言 -->
        <section>
          <p class="text-stone-600 leading-relaxed">
            CHOOSE
            SELECT（以下簡稱「本網站」）重視您的隱私權，本隱私權政策說明我們如何收集、使用、保護您的個人資料。
          </p>
        </section>

        <!-- 資料收集 -->
        <section>
          <h2 class="text-xl font-serif text-sumi mb-4">資料收集範圍</h2>
          <p class="text-stone-600 leading-relaxed mb-4">
            我們可能收集以下類型的資料：
          </p>
          <ul class="list-disc list-inside text-stone-600 space-y-2 ml-4">
            <li>帳號資訊：電子郵件、姓名、電話號碼</li>
            <li>訂單資訊：收件地址、購買記錄</li>
            <li>瀏覽資訊：瀏覽頁面、停留時間</li>
          </ul>
        </section>

        <!-- 資料使用 -->
        <section>
          <h2 class="text-xl font-serif text-sumi mb-4">資料使用目的</h2>
          <p class="text-stone-600 leading-relaxed mb-4">
            您的個人資料將用於以下目的：
          </p>
          <ul class="list-disc list-inside text-stone-600 space-y-2 ml-4">
            <li>處理訂單及配送商品</li>
            <li>回覆客戶詢問</li>
            <li>改善網站服務體驗</li>
            <li>發送訂單狀態通知</li>
          </ul>
        </section>

        <!-- 帳號刪除說明 -->
        <section class="bg-stone-50 p-6 border border-stone-200 rounded">
          <h2 class="text-xl font-serif text-sumi mb-4">帳號刪除與資料處理</h2>
          <p class="text-stone-600 leading-relaxed mb-4">
            當您選擇刪除帳號時，我們將進行以下處理：
          </p>
          <ul class="list-disc list-inside text-stone-600 space-y-2 ml-4">
            <li>
              <strong>個人資料去識別化</strong
              >：您的姓名、電子郵件、電話等個人識別資訊將被移除或匿名化處理
            </li>
            <li>
              <strong>訂單記錄保留</strong
              >：基於法規遵循及商業需求，歷史訂單記錄將保留，但不再與您的個人身份關聯
            </li>
            <li><strong>購物車清空</strong>：您的購物車內容將被永久刪除</li>
            <li>
              <strong>不可恢復</strong>：帳號一旦刪除，將無法恢復，請謹慎操作
            </li>
          </ul>
          <p class="text-stone-500 text-sm mt-4">
            ※ 如有未完成的訂單，您需要先等待訂單完成或取消後，才能刪除帳號。
          </p>
        </section>

        <!-- 資料保護 -->
        <section>
          <h2 class="text-xl font-serif text-sumi mb-4">資料保護措施</h2>
          <p class="text-stone-600 leading-relaxed">
            我們採用 HTTPS 加密傳輸、密碼雜湊儲存等技術措施保護您的資料安全。
            您的密碼以加密形式儲存，即使是我們的工作人員也無法查看您的原始密碼。
          </p>
        </section>

        <!-- Cookie 使用 -->
        <section>
          <h2 class="text-xl font-serif text-sumi mb-4">Cookie 使用</h2>
          <p class="text-stone-600 leading-relaxed">
            本網站使用 Cookie 及 localStorage 來維持您的登入狀態及購物車資訊，
            以提供更好的使用體驗。您可以透過瀏覽器設定管理 Cookie。
          </p>
        </section>

        <!-- 聯絡方式 -->
        <section>
          <h2 class="text-xl font-serif text-sumi mb-4">聯絡我們</h2>
          <p class="text-stone-600 leading-relaxed">
            若您對隱私權政策有任何疑問，歡迎透過
            <router-link
              to="/contact"
              class="text-sumi underline hover:text-stone-700"
            >
              聯絡我們
            </router-link>
            頁面與我們聯繫。
          </p>
        </section>

        <!-- 更新日期 -->
        <section class="text-xs text-stone-400 pt-8 border-t border-stone-200">
          <p>最後更新日期：2025 年 12 月</p>
        </section>
      </div>
    </div>
  </div>
</template>
