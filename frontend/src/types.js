/**
 * types.js - 型別定義文件 (JSDoc 格式)
 * 此檔案提供 JavaScript 專案的型別提示，供 IDE 自動完成使用
 */

// ============================================
// Product & Variant (對應 SQL: products, product_variants)
// ============================================

/**
 * 商品規格
 * @typedef {Object} ProductVariant
 * @property {string} id - 規格 ID
 * @property {string} productId - 所屬商品 ID
 * @property {string} skuCode - SKU 編碼
 * @property {string} color - 顏色
 * @property {string} size - 尺寸
 * @property {number} stock - 庫存數量
 * @property {string} [createdAt] - 建立時間
 */

/**
 * 商品
 * @typedef {Object} Product
 * @property {string} id - 商品 ID
 * @property {string} categoryId - 分類 ID
 * @property {string} name - 商品名稱
 * @property {string} description - 商品描述
 * @property {number} price - 價格
 * @property {string} imageUrl - 圖片網址
 * @property {boolean} isListed - 是否上架
 * @property {string} [createdAt] - 建立時間
 * @property {ProductVariant[]} [variants] - 商品規格列表
 * @property {number} [totalStock] - 計算屬性：所有規格庫存總和
 * @property {string} [category] - 為了向下相容，保留 category 作為顯示用 (由 API 填入)
 */

// ============================================
// Category (對應 SQL: categories)
// ============================================

/**
 * 商品分類
 * @typedef {Object} Category
 * @property {string} id - 分類 ID
 * @property {string} name - 分類名稱 (限英文)
 * @property {string} [description] - 分類描述
 * @property {string} [createdAt] - 建立時間
 */

// ============================================
// User (對應 SQL: users)
// ============================================

/**
 * 使用者角色
 * @typedef {'MEMBER' | 'ADMIN'} UserRole
 */

/**
 * 使用者
 * @typedef {Object} User
 * @property {string} [id] - 使用者 ID
 * @property {string} email - 電子郵件
 * @property {string} name - 姓名
 * @property {string} [phone] - 電話
 * @property {UserRole} [role] - 角色
 * @property {Order[]} orders - 訂單列表
 */

/**
 * Mock 使用者 (for authentication simulation)
 * @typedef {Object} MockUser
 * @property {string} id - 使用者 ID
 * @property {string} email - 電子郵件
 * @property {string} password - 密碼 (僅供模擬，正式後端應雜湊)
 * @property {string} name - 姓名
 * @property {string} [phone] - 電話
 * @property {UserRole} role - 角色
 */

// ============================================
// Inquiry (對應 SQL: contact_messages)
// ============================================

/**
 * 詢問狀態
 * @typedef {'UNREAD' | 'READ' | 'REPLIED'} InquiryStatus
 */

/**
 * 聯絡詢問
 * @typedef {Object} Inquiry
 * @property {string} id - 詢問 ID
 * @property {string} [userId] - 使用者 ID
 * @property {string} name - 姓名
 * @property {string} email - 電子郵件
 * @property {string} message - 訊息內容
 * @property {InquiryStatus} status - 狀態
 * @property {string} [createdAt] - 建立時間
 * @property {string} [repliedAt] - 回覆時間
 * @property {string} [date] - 為了向下相容
 */

// ============================================
// Cart (前端狀態，對應 SQL: cart_items)
// ============================================

/**
 * 購物車項目
 * @typedef {Object} CartItem
 * @property {Product} product - 商品
 * @property {ProductVariant} variant - 規格
 * @property {number} quantity - 數量
 */

// ============================================
// Order (對應 SQL: orders, order_items)
// ============================================

/**
 * 訂單狀態
 * @typedef {'PENDING' | 'PAID' | 'SHIPPED' | 'COMPLETED' | 'CANCELLED'} OrderStatus
 */

/**
 * 付款方式
 * @typedef {'BANK_TRANSFER' | 'STORE_PICKUP'} PaymentMethod
 */

/**
 * 運送資訊
 * @typedef {Object} ShippingDetails
 * @property {string} fullName - 收件人姓名
 * @property {string} phone - 收件人電話
 * @property {string} email - 收件人電子郵件
 * @property {PaymentMethod} method - 付款/取貨方式
 * @property {string} [address] - 地址
 * @property {string} [city] - 城市
 * @property {string} [storeCode] - 超商門市代碼
 * @property {string} [storeName] - 超商門市名稱
 */

/**
 * 訂單項目
 * @typedef {Object} OrderItem
 * @property {ProductVariant} variant - 規格
 * @property {Product} product - 商品
 * @property {number} price - 購買當下的單價
 * @property {number} quantity - 數量
 */

/**
 * 訂單
 * @typedef {Object} Order
 * @property {string} id - 訂單 ID
 * @property {string} [userId] - 使用者 ID
 * @property {OrderItem[]} items - 訂單項目
 * @property {number} total - 訂單總金額
 * @property {OrderStatus} status - 訂單狀態
 * @property {PaymentMethod} [paymentMethod] - 付款方式
 * @property {ShippingDetails} [shippingDetails] - 運送資訊
 * @property {string} [paymentNote] - 付款備註
 * @property {string} [createdAt] - 建立時間
 * @property {string} [date] - 為了向下相容
 */

// ============================================
// Page Navigation
// ============================================

/**
 * 頁面視圖
 * @typedef {'HOME' | 'COLLECTION' | 'PRODUCT_DETAIL' | 'CONTACT' | 'CHECKOUT' | 'ADMIN_DASHBOARD' | 'USER_DASHBOARD'} PageView
 */

// 匯出空物件以使此檔案成為 ES Module
export {};
