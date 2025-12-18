<script setup>
/**
 * ReturnPolicyView - 退換貨說明頁面
 */
</script>

<template>
  <div class="pt-24 min-h-screen bg-stone-50">
    <div class="max-w-3xl mx-auto px-6 py-12">
      <!-- Header -->
      <div class="mb-12 text-center">
        <h1 class="text-3xl font-serif text-sumi mb-4">退換貨說明</h1>
        <p class="text-sm text-stone-500">Return & Exchange Policy</p>
      </div>

      <!-- 內容區塊 -->
      <div class="space-y-10 text-stone-700">
        <!-- 退換貨原則 -->
        <section class="bg-white p-8 rounded border border-stone-200">
          <h2
            class="text-xl font-serif text-sumi mb-4 pb-2 border-b border-stone-100"
          >
            退換貨原則
          </h2>
          <ul class="space-y-3 text-sm leading-relaxed">
            <li class="flex items-start gap-2">
              <span class="text-stone-400">•</span>
              <span
                >商品到貨後
                <strong>7 天內</strong
                >，如有瑕疵或尺寸不合，可申請退換貨。</span
              >
            </li>
            <li class="flex items-start gap-2">
              <span class="text-stone-400">•</span>
              <span
                >退換貨商品須保持<strong>全新狀態</strong>，包含吊牌、包裝完整，未經使用、洗滌或修改。</span
              >
            </li>
            <li class="flex items-start gap-2">
              <span class="text-stone-400">•</span>
              <span
                >特價商品、內衣褲、貼身衣物及個人衛生用品，恕不接受退換。</span
              >
            </li>
          </ul>
        </section>

        <!-- 退換貨流程 -->
        <section class="bg-white p-8 rounded border border-stone-200">
          <h2
            class="text-xl font-serif text-sumi mb-4 pb-2 border-b border-stone-100"
          >
            退換貨流程
          </h2>
          <ol class="space-y-4 text-sm leading-relaxed">
            <li class="flex items-start gap-3">
              <span
                class="flex-shrink-0 w-6 h-6 bg-sumi text-white text-xs flex items-center justify-center rounded-full"
                >1</span
              >
              <span
                >透過「聯絡我們」頁面或 Email
                提出退換貨申請，請註明訂單編號及退換原因。</span
              >
            </li>
            <li class="flex items-start gap-3">
              <span
                class="flex-shrink-0 w-6 h-6 bg-sumi text-white text-xs flex items-center justify-center rounded-full"
                >2</span
              >
              <span>客服確認後，將提供退貨地址及注意事項。</span>
            </li>
            <li class="flex items-start gap-3">
              <span
                class="flex-shrink-0 w-6 h-6 bg-sumi text-white text-xs flex items-center justify-center rounded-full"
                >3</span
              >
              <span
                >請將商品妥善包裝寄回，建議使用掛號或可追蹤之物流方式。</span
              >
            </li>
            <li class="flex items-start gap-3">
              <span
                class="flex-shrink-0 w-6 h-6 bg-sumi text-white text-xs flex items-center justify-center rounded-full"
                >4</span
              >
              <span
                >收到商品並確認無誤後，將於
                <strong>7~10個工作天內</strong> 完成退款或換貨。</span
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
              <span
                >因個人因素（如不喜歡、尺寸選錯）申請退換貨，運費由買家自行負擔。</span
              >
            </li>
            <li class="flex items-start gap-2">
              <span class="text-stone-400">•</span>
              <span>如商品有瑕疵或寄錯商品，運費由本店負擔。</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-stone-400">•</span>
              <span>退款將退回原付款方式，銀行轉帳約 3-5 個工作天入帳。</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-stone-400">•</span>
              <span>如有任何問題，歡迎透過聯絡我們頁面詢問。</span>
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
