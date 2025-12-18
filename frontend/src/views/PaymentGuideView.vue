<script setup>
/**
 * PaymentGuideView - 匯款說明頁面
 */
</script>

<template>
  <div class="pt-24 min-h-screen bg-stone-50">
    <div class="max-w-3xl mx-auto px-6 py-12">
      <!-- Header -->
      <div class="mb-12 text-center">
        <h1 class="text-3xl font-serif text-sumi mb-4">匯款說明</h1>
        <p class="text-sm text-stone-500">Payment Guide</p>
      </div>

      <!-- 內容區塊 -->
      <div class="space-y-10 text-stone-700">
        <!-- 付款方式 -->
        <section class="bg-white p-8 rounded border border-stone-200">
          <h2
            class="text-xl font-serif text-sumi mb-4 pb-2 border-b border-stone-100"
          >
            付款方式
          </h2>
          <div class="space-y-6">
            <div class="flex items-start gap-4">
              <div
                class="flex-shrink-0 w-10 h-10 bg-teal-50 text-teal-600 flex items-center justify-center rounded-full"
              >
                <svg
                  class="w-5 h-5"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"
                  />
                </svg>
              </div>
              <div>
                <h3 class="text-base font-medium text-sumi mb-1">
                  銀行轉帳 / 匯款（郵局宅配）
                </h3>
                <p class="text-sm text-stone-600">
                  選擇此方式，商品將以郵局包裹寄送到您指定的地址。
                </p>
              </div>
            </div>
            <div class="flex items-start gap-4">
              <div
                class="flex-shrink-0 w-10 h-10 bg-slate-100 text-slate-600 flex items-center justify-center rounded-full"
              >
                <svg
                  class="w-5 h-5"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
                  />
                </svg>
              </div>
              <div>
                <h3 class="text-base font-medium text-sumi mb-1">
                  店到店 / 貨到付款
                </h3>
                <p class="text-sm text-stone-600">
                  選擇此方式，商品將寄送至您指定的便利商店，取貨時付款。
                </p>
              </div>
            </div>
          </div>
        </section>

        <!-- 匯款資訊 -->
        <section class="bg-amber-50 p-8 rounded border border-amber-200">
          <h2
            class="text-xl font-serif text-amber-800 mb-4 pb-2 border-b border-amber-200"
          >
            匯款帳戶資訊
          </h2>
          <div class="bg-white/70 p-6 rounded-lg space-y-3">
            <div class="flex items-center gap-3">
              <span
                class="text-sm text-amber-600 font-medium w-16 flex-shrink-0"
                >銀行</span
              >
              <span class="text-base text-sumi font-mono"
                >Choose Bank (808)</span
              >
            </div>
            <div class="flex items-center gap-3">
              <span
                class="text-sm text-amber-600 font-medium w-16 flex-shrink-0"
                >帳號</span
              >
              <span class="text-base text-sumi font-mono">1234-5678-9012</span>
            </div>
            <div class="flex items-center gap-3">
              <span
                class="text-sm text-amber-600 font-medium w-16 flex-shrink-0"
                >戶名</span
              >
              <span class="text-base text-sumi font-mono"
                >Choose Select Ltd.</span
              >
            </div>
          </div>
          <p class="text-sm text-amber-700 mt-4">
            ※ 請於下訂後
            <strong>3 天內</strong>
            完成匯款，逾期訂單將自動取消。
          </p>
        </section>

        <!-- 匯款流程 -->
        <section class="bg-white p-8 rounded border border-stone-200">
          <h2
            class="text-xl font-serif text-sumi mb-4 pb-2 border-b border-stone-100"
          >
            匯款流程
          </h2>
          <ol class="space-y-4 text-sm leading-relaxed">
            <li class="flex items-start gap-3">
              <span
                class="flex-shrink-0 w-6 h-6 bg-sumi text-white text-xs flex items-center justify-center rounded-full"
                >1</span
              >
              <span
                >完成結帳後，請至銀行 ATM 或網路銀行將
                <strong>訂單總金額</strong> 轉帳至上述帳戶。</span
              >
            </li>
            <li class="flex items-start gap-3">
              <span
                class="flex-shrink-0 w-6 h-6 bg-sumi text-white text-xs flex items-center justify-center rounded-full"
                >2</span
              >
              <span
                >登入會員中心，進入「訂單管理」頁面，點擊該訂單的「新增付款備註」。</span
              >
            </li>
            <li class="flex items-start gap-3">
              <span
                class="flex-shrink-0 w-6 h-6 bg-sumi text-white text-xs flex items-center justify-center rounded-full"
                >3</span
              >
              <span
                >填寫匯款資訊：<strong>帳號末五碼、匯款金額、匯款時間</strong>。</span
              >
            </li>
            <li class="flex items-start gap-3">
              <span
                class="flex-shrink-0 w-6 h-6 bg-sumi text-white text-xs flex items-center justify-center rounded-full"
                >4</span
              >
              <span
                >確認匯款資訊無誤後，我們將於
                <strong>1~2 個工作天內</strong> 確認並安排出貨。</span
              >
            </li>
          </ol>
        </section>

        <!-- 注意事項 -->
        <section class="bg-white p-8 rounded border border-stone-200">
          <h2
            class="text-xl font-serif text-sumi mb-4 pb-2 border-b border-stone-100"
          >
            注意事項
          </h2>
          <ul class="space-y-3 text-sm leading-relaxed">
            <li class="flex items-start gap-2">
              <span class="text-stone-400">•</span>
              <span>請務必填寫正確的匯款資訊，以利對帳作業。</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-stone-400">•</span>
              <span>如使用 ATM 轉帳，請記得保留交易明細作為憑證。</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-stone-400">•</span>
              <span>跨行轉帳可能產生手續費，請自行負擔或選擇同行匯款。</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-stone-400">•</span>
              <span>如有任何問題，歡迎透過「聯絡我們」頁面詢問。</span>
            </li>
          </ul>
        </section>

        <!-- 聯絡資訊 -->
        <div class="text-center pt-6 border-t border-stone-200">
          <p class="text-sm text-stone-500 mb-4">
            如需協助，請<strong>聯繫我們</strong> <br />
            我們將會盡快為您提供協助
          </p>

          <a
            href="/contact"
            class="inline-block px-6 py-3 bg-sumi text-white text-sm uppercase tracking-widest hover:bg-stone-800 transition-colors"
          >
            聯絡我們
          </a>
        </div>
      </div>
    </div>
  </div>
</template>
