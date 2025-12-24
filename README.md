# Choose 電商平台

> **學習專案聲明**  
> 本專案為個人練習作品，僅供學習與展示用途，**非商業運營網站**。

---

## 專案簡介

Choose 是一個全端電商平台練習專案。專案包含商品展示、購物車、訂單管理、會員系統及後台管理等電商核心功能。

## 使用技術

### 後端

- **框架**：Spring Boot
- **建置工具**：Gradle
- **安全**：Spring Security + JWT 認證
- **資料庫**：MySQL + JPA/Hibernate
- **圖片儲存**：Cloudinary
- **部署**：Docker / Zeabur

### 前端

- **框架**：Vue 3 + Composition API
- **狀態管理**：Pinia
- **路由**：Vue Router
- **樣式**：Tailwind CSS
- **HTTP 客戶端**：Axios

## 功能特點

- 商品瀏覽與搜尋
- 購物車管理
- 訂單建立與追蹤
- 會員註冊與登入
- JWT Token 認證
- 響應式設計
- 管理後台（商品/訂單/用戶管理）

## 專案結構

```
Choose/
├── backend/          # Spring Boot 後端 API
├── frontend/         # Vue 3 前端應用
├── mySQL/            # 資料庫 Schema
└── README.md
```

## 本地開發

### 前置需求

- Node.js 18+
- Java 17+
- MySQL 8+

### 啟動後端

```bash
cd backend
./gradlew bootRun
```

### 啟動前端

```bash
cd frontend
npm install
npm run dev
```

## 授權

本專案僅供學習參考，請勿用於商業用途。

---

<p align="center">
  <i>Built for learning purposes</i>
</p>
