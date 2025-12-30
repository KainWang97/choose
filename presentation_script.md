# Choose 專案報告文字稿

## 報告流程圖

```mermaid
flowchart TD
    A["開場：自我介紹與專案名稱"] --> B["開發動機"]
    B --> C["技術架構"]
    C --> D["網站功能構思"]
    D --> E["後端技術實現"]
    E --> F["資料庫設計"]
    F --> G["資料庫三大核心"]
    G --> H["網站地圖 + 截圖"]
    H --> I["實際網站 Demo"]
    I --> J["返回簡報"]
    J --> K["未來功能擴充"]
    K --> L["開發困難與 AI 輔助"]
    L --> M["結語 + QRCode"]
```

---

## 一、開場：自我介紹與專案名稱

各位評審好，我是王凱，今天要報告的專案是 **Choose** — 一個電商服飾網站的全端開發專案。

Choose 的命名來自「選擇有品味的生活」這個理念，希望提供消費者一個簡潔、優雅的購物體驗。

---

## 二、開發動機

1. **驗證全端開發能力**：從資料庫設計、後端 API、到前端介面，完整走過電商網站開發流程
2. **學習企業級框架實務**：深入理解 Spring Boot 分層架構、JPA 資料庫操作、RESTful API 設計
3. **解決真實業務問題**：庫存管理、訂單追蹤、客服系統等，模擬真實電商運營情境

---

## 三、技術架構

| 層級         | 技術選型                              |
| ------------ | ------------------------------------- |
| **前端**     | Vue 3 + Vite + Tailwind CSS           |
| **後端**     | Spring Boot 4 + Spring Security + JPA |
| **資料庫**   | MySQL 8                               |
| **雲端服務** | Cloudinary（圖片儲存）                |
| **部署**     | Zeabur（PaaS 雲端部署）               |
| **版本控制** | Git + GitHub                          |

### Spring Boot Dependencies

後端專案使用以下核心依賴（`build.gradle`）：

| 依賴                             | 用途                     |
| -------------------------------- | ------------------------ |
| `spring-boot-starter-web`        | RESTful API 開發         |
| `spring-boot-starter-data-jpa`   | Hibernate ORM 資料庫操作 |
| `spring-boot-starter-security`   | JWT 認證與權限控管       |
| `spring-boot-starter-mail`       | SMTP 郵件發送            |
| `spring-boot-starter-validation` | 輸入驗證                 |
| `jjwt`                           | JWT Token 生成與解析     |
| `cloudinary-http5`               | 雲端圖片上傳 SDK         |
| `mysql-connector-j`              | MySQL 資料庫連接         |
| `lombok`                         | 減少樣板程式碼           |

### 後端架構分層

```
Controller 層 → 接收 HTTP 請求、回傳 API Response
      ↓
Service 層 → 業務邏輯處理、資料驗證
      ↓
Repository 層 → JPA 資料庫操作
      ↓
Model 層 → 資料庫實體映射（Hibernate Entity）
```

### Git 版本控制

使用 Git 進行版本控制，確保程式碼可追溯、可回復：

```bash
# 查看目前修改狀態
git status

# 查看檔案差異
git diff

# 查看提交歷史
git log --oneline -10

# 提交變更
git add . && git commit -m "feat: 新增功能描述"
```

**版控重要性**：

- 每次功能開發前先 commit，確保可回滾
- 使用有意義的 commit message 紀錄變更
- 透過 GitHub 與 Zeabur 整合實現自動部署

### 第三方圖庫整合（Cloudinary）

透過 Cloudinary Java SDK 實現後端代理上傳：

1. 前端將圖片 File 傳至後端 `/api/upload` 端點
2. 後端 `ImageUploadService` 使用環境變數中的 `CLOUDINARY_URL` 進行認證
3. 上傳成功後取得 `secure_url`，儲存至資料庫
4. 確保 API Key 不暴露於前端

### PaaS 雲端部署（Zeabur）

1. **後端服務**：GitHub 連結 → 自動建置 JAR → 部署
2. **前端服務**：偵測 Vite → `npm run build` → 部署靜態資源
3. **MySQL 服務**：Managed MySQL，內網連線
4. **環境變數**：`DATABASE_URL`、`CLOUDINARY_URL`、`JWT_SECRET`

---

## 四、網站功能構思

本專案功能圍繞電商核心流程設計：

### 1. 會員註冊/登入

- 「無密碼註冊」：Email 驗證完成註冊
- 首次登入設定密碼，後續可用密碼或 Magic Link
- JWT Token 維持登入狀態

### 2. 瀏覽商品

- 響應式商品列表，分類篩選（Top/Bottom/Coat/Accessories/Shoes）
- 關鍵字搜尋即時過濾
- 首頁「新品上架」區塊

### 3. 選擇商品規格

- 商品詳情頁多圖展示
- 動態顏色與尺寸選擇，自動標示無庫存規格

### 4. 購物車暫存

- 登入用戶購物車存於資料庫
- 支援數量調整、刪除、庫存檢查

### 5. 訂單結帳

- 配送方式選擇（宅配/超商取貨）
- 填寫收件資訊，提交後顯示訂單編號

### 6. 用戶疑問服務

- 「聯絡我們」表單提交問題
- 後台回覆自動發送 Email 通知

---

## 五、後端技術實現

### 會員驗證流程

```
用戶輸入 Email → AuthController.register()
       ↓
UserService 建立用戶 → EmailService 產生驗證 Token
       ↓
發送驗證信 → 用戶點擊連結 → AuthController.verifyEmail()
       ↓
VerificationToken 驗證成功 → 更新 user.email_verified = true
```

### JWT 認證機制

```java
// JwtTokenProvider.java
public String generateToken(User user) {
    return Jwts.builder()
        .setSubject(user.getEmail())
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
        .signWith(secretKey)
        .compact();
}
```

### SKU 自動生成

```java
// ProductService.java
private String generateUniqueSku(Long productId, String color, String size) {
    // 格式：P{商品ID}-{顏色縮寫}-{尺寸}-{6位隨機碼}
    String candidate = String.format("P%s-%s-%s-%06d",
        productId, abbreviate(color, 4), abbreviate(size, 4),
        (int)(Math.random() * 1_000_000));
    // 確保唯一性，若衝突則重新生成
}
```

### 訂單處理流程

```
購物車結帳 → OrderController.createOrder()
       ↓
OrderService 驗證庫存 → 建立 Order + OrderItems
       ↓
CartService 清空購物車 → ProductVariant 扣減庫存
       ↓
回傳訂單編號
```

---

## 六、資料庫設計

根據功能需求，設計了 **11 張資料表**：

| 類別       | 資料表                                       |
| ---------- | -------------------------------------------- |
| 使用者相關 | `users`、`verification_tokens`               |
| 商品相關   | `categories`、`products`、`product_variants` |
| 訂單相關   | `cart_items`、`orders`、`order_items`        |
| 客服相關   | `contact_messages`、`reply_templates`        |

---

## 七、資料庫三大核心

資料庫設計以 **消費者**、**商品**、**訂單** 為三大核心。

---

### 1️⃣ 消費者核心

**用戶（users）** 是整個電商系統的起點，記錄會員基本資料與登入狀態。

| 表格                  | 創建意義                                               |
| --------------------- | ------------------------------------------------------ |
| `users`               | 儲存 Email、密碼（BCrypt）、姓名、角色（MEMBER/ADMIN） |
| `verification_tokens` | 信箱驗證、密碼重設、Magic Link 登入的一次性 Token      |

**為什麼需要 verification_tokens？**

- Token 有時效性（24hr/1hr/15min），獨立表格方便清理過期資料
- 支援多種類型：`EMAIL_VERIFY`、`PASSWORD_RESET`、`LOGIN_LINK`

---

### 2️⃣ 商品核心

**商品（products）** 是電商的核心資產，透過分類與規格表實現完整的商品管理。

| 表格               | 創建意義                                      |
| ------------------ | --------------------------------------------- |
| `categories`       | 商品分類（Top/Bottom/Coat/Accessories/Shoes） |
| `products`         | 商品主資料：名稱、價格、描述、主圖、顏色圖片  |
| `product_variants` | 商品規格：顏色 + 尺寸 + 庫存 + SKU 編碼       |

**為什麼要拆分 products 和 product_variants？**

- 一件商品有多種規格組合（白色 M、白色 L、黑色 M...）
- 每個規格有獨立庫存，精確控管進出貨
- SKU 編碼（`P1-MORA-M-000001`）確保唯一識別
  Stock Keeping Unit Code

---

### 3️⃣ 訂單核心

**訂單（orders）** 是消費者與商品的交會點，記錄完整的購買歷程。

| 表格          | 創建意義                                   |
| ------------- | ------------------------------------------ |
| `cart_items`  | 購物車暫存，登入用戶的購物清單             |
| `orders`      | 訂單主檔：總金額、狀態、配送方式、收件資訊 |
| `order_items` | 訂單明細：購買的具體規格、當時價格、數量   |

**為什麼需要 order_items？**

- 記錄「下單當時」的價格，避免商品調價影響歷史訂單
- 透過 `variant_id` 關聯完整的顏色/尺寸資訊

**補充：客服相關表格**

- `contact_messages`：用戶提交的問題、管理員回覆、案件編號
- `reply_templates`：預設回覆模板，加速客服回覆效率

---

## 八、網站地圖與截圖展示

### 前台路由

`/` 首頁 → `/collection` 商品列表 → `/product/:id` 詳情頁 → `/checkout` 結帳 → `/account` 會員中心

### 後台路由

`/admin` → INVENTORY | CATEGORIES | ORDERS | MEMBERS | STATS | INQUIRIES

> [快速帶過簡報截圖]

---

## 九、實際網站 Demo

### 消費者端流程

1. 首頁 → 商品列表 → 商品詳情 → 加入購物車
2. 搜尋「外套」→ 選規格加入購物車
3. 結帳 → 信箱註冊 → 驗證 → 完成訂單
4. 會員中心查看訂單 → 設定密碼

### 管理員端流程

1. 商品庫存管理 → SKU 搜尋
2. 新增商品 → 上傳圖片 → 新增規格（自動生成 SKU）
3. 訂單管理 → 狀態更新
4. 客服訊息 → 回覆（自動發 Email）

---

## 十、未來功能擴充

### 1. WebSocket 即時通知

**目標**：訂單狀態變更、新客服訊息即時推送

**技術細節**：

- 後端：`spring-boot-starter-websocket` + STOMP 協定
- 頻道設計：`/topic/orders`（訂單）、`/topic/messages`（客服）
- 前端：Vue `useWebSocket` composable 監聽
- 實現：`SimpMessagingTemplate.convertAndSend()` 推送訊息

**應用場景**：管理員收到新訂單桌面通知、消費者收到出貨通知

### 2. 串接第三方金流（ECPay）

**目標**：實現線上付款，取代銀行轉帳備註

**實作細節**：

1. **建立付款請求**

   - PaymentService 組裝 ECPay 參數（MerchantID、TradeNo、CheckMacValue）
   - 使用 HMAC-SHA256 簽名確保安全

2. **用戶付款**

   - 前端以隱藏表單 POST 至 ECPay
   - 完成付款後導回 ReturnURL

3. **接收回調（Webhook）**
   - ECPay POST 至 NotifyURL → 驗證簽名 → 更新訂單狀態為 PAID

### 3. 註冊流程優化

**現況問題**：目前「先註冊再驗證」，未驗證用戶會佔用資料庫空間，且 Email 無法被重複使用。

**優化目標**：改為「驗證後才註冊」，避免惡意註冊攻擊。

**實作細節**：

1. **暫存註冊資料**

   - 用戶輸入 Email + Name → 暫存於 `verification_tokens` 表（新增 `pending_email`, `pending_name` 欄位）
   - 不寫入 `users` 表

2. **驗證成功後建立帳號**

   - 點擊驗證連結 → 從 Token 取出暫存資料 → 建立 User → 回傳 JWT 自動登入

3. **過期 Token 自動清理**
   - 使用 `@Scheduled(fixedRate = 15 * 60 * 1000)` 每 15 分鐘清理過期 Token
   - 過期後同一 Email 可重新註冊

---

## 十一、開發困難與 AI 輔助

開發過程中主要遇到三個面向的挑戰：**企業級框架的學習曲線**、**第三方服務的整合**，以及**如何正確運用 AI 輔助學習**。

### Spring Boot + Hibernate 難點

**挑戰**：首次接觸企業級 Java 框架，Hibernate ORM 概念複雜

**具體困難**：

1. **JPA 關聯映射**

   - `@OneToMany` 與 `@ManyToOne` 的雙向關聯設定
   - `mappedBy` 屬性容易搞混（哪一方擁有關聯）
   - 範例：`Product` 與 `ProductVariant` 的一對多關聯

2. **懶加載（Lazy Loading）陷阱**

   - `FetchType.LAZY` 在 Session 關閉後存取會拋 `LazyInitializationException`
   - 解法：在 Service 層使用 `@Transactional` 保持 Session 開啟

3. **N+1 查詢問題**

   - 遍歷商品列表時，每個商品都發一次 SQL 查規格
   - 解法：使用 `@EntityGraph` 或 `JOIN FETCH` 一次載入

4. **Transaction 管理**
   - 何時需要 `@Transactional`？為什麼 Service 層要加？
   - 理解：確保多個 Repository 操作在同一個事務中

**AI 輔助學習**：

- 請 AI 解釋「`@Transactional` 的 propagation 屬性有哪些？」
- 讓 AI 比較「LAZY vs EAGER 的效能差異」
- 遇到錯誤時貼 Stack Trace 請 AI 分析

### Cloudinary 第三方整合難點

**挑戰**：第一次串接雲端圖片服務

- 環境變數格式（`cloudinary://api_key:api_secret@cloud_name`）
- SDK 回傳的 Map 結構解析
- 選擇後端代理上傳，避免前端暴露 API Key

**解決方式**：先用 Postman 測試 API，再整合到專案

### Email 發送效能優化

**挑戰**：同步發送郵件導致使用者等待

- SMTP 發送可能耗時 1-5 秒
- 若收件者伺服器無回應，會等待 timeout
- 使用者感覺「卡住」

**解決方案**：使用 `@Async` 非同步發送

```java
@Async  // 背景執行緒處理
public void sendVerificationEmailAsync(String email, String token) {
    mailSender.send(message);  // 不阻塞主流程
}
```

**`@Async` 與 `@Transactional` 共用注意**：

- `@Async` 開新執行緒，Transaction Context 不會跟過去
- 需拆分方法：同步處理資料庫操作，異步只做 I/O

### AI 輔助開發心得

**正確使用方式**：

1. **學習導向**：請 AI 解釋原理，不直接複製程式碼
2. **逐步驗證**：每個功能先理解再整合
3. **追問細節**：「這段程式碼為什麼這樣寫？改成這樣會怎樣？」

**收穫**：加速學習曲線、分析 Stack Trace、培養獨立判斷能力

---

## 十二、結語

感謝各位評審的聆聽！

Choose 是我從零到一完成的全端專案：

- ✅ 前端：Vue 3 響應式設計
- ✅ 後端：Spring Boot 企業級架構
- ✅ 資料庫：MySQL 關聯設計
- ✅ 部署：Git + 雲端服務整合

歡迎掃描 QR Code 體驗網站或查看我的履歷！

| 網站 Demo | 履歷      |
| --------- | --------- |
| [QR Code] | [QR Code] |

謝謝！
