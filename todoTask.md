# Choose 專案待辦任務清單

## 優先順序排序

### 1. ✅ RWD Navbar 漢堡選單

**困難度**：低 | **狀態**：已完成

- [x] 新增漢堡選單圖示按鈕
- [x] 實作側邊滑出選單
- [x] 加入開關動畫效果
- [x] 行動版隱藏原有導覽列項目

**相關檔案**：`Navbar.vue`

---

### 2. ✅ 商品詳情頁多圖片介紹

**困難度**：中 | **狀態**：已完成

- [x] 後端：擴充 Product Model 支援多圖片
- [x] 後端：新增/修改 ProductDTO
- [x] 前端：商品詳情頁圖片輪播
- [x] 前端：縮圖列表切換
- [ ] Admin：多圖片上傳 UI (延後)

**相關檔案**：`Product.java`, `ProductDTO.java`, `ProductDetail.vue`, `api.js`

---

### 3. 🟡 管理面板數據圖表 + 流量分析

**困難度**：中 | **預估時間**：6-8 小時

- [ ] 安裝圖表套件（Chart.js 或 Echarts）
- [ ] 銷售趨勢折線圖
- [ ] 商品分類銷量圓餅圖
- [ ] 訂單狀態統計長條圖
- [ ] 整合 Google Analytics 或自建流量追蹤

**相關檔案**：`AdminDashboard.vue`, 新增統計 API

---

### 4. ✅ 信箱驗證（註冊 + 更改密碼）

**困難度**：高 | **狀態**：已完成

- [x] 後端：配置 SMTP 服務 (Gmail)
- [x] 後端：新增 EmailService
- [x] 後端：新增 VerificationToken 資料表
- [x] 後端：修改註冊流程（發送驗證信）
- [x] 後端：新增忘記密碼 / 重設密碼流程
- [x] 前端：驗證成功/失敗頁面
- [x] 前端：忘記密碼 / 重設密碼頁面
- [x] 前端：結帳時檢查信箱驗證狀態

**相關檔案**：`AuthController.java`, `UserService.java`, `EmailService.java`, `VerificationToken.java`, `EmailVerificationView.vue`, `ForgotPasswordView.vue`, `ResetPasswordView.vue`

---

### 5. 🟡 管理面板回覆用戶信件

**困難度**：中 | **預估時間**：4-6 小時

- [ ] 後端：新增回覆信件 API（使用 Gmail SMTP）
- [ ] 前端：在 ContactMessages 管理區新增回覆輸入框
- [ ] 後端：記錄回覆內容到資料庫

**相關檔案**：`ContactMessageController.java`, `EmailService.java`, `AdminDashboard.vue`

---

## 圖例說明

| 符號 | 困難度                     |
| ---- | -------------------------- |
| 🟢   | 低（純前端，快速可完成）   |
| 🟡   | 中（前後端皆需修改）       |
| 🔴   | 高（涉及新服務或複雜流程） |
