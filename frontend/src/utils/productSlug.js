/**
 * 產品 Slug 工具函數
 * 用於編碼/解碼產品 ID，避免直接在 URL 中暴露
 */

/**
 * 將產品 ID 編碼為 URL-safe slug
 * 使用 base64 編碼 + 產品名稱前綴
 * @param {string} productId
 * @param {string} productName
 * @returns {string}
 */
export function encodeProductSlug(productId, productName) {
  // 將名稱轉為 URL-safe 格式
  const nameSlug = productName
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/(^-|-$)/g, "")
    .substring(0, 30);

  // 編碼 ID (使用 base64)
  const encodedId = btoa(productId)
    .replace(/=/g, "")
    .replace(/\+/g, "-")
    .replace(/\//g, "_");

  return `${nameSlug}-${encodedId}`;
}

/**
 * 從 slug 解碼產品 ID
 * @param {string} slug
 * @returns {string | null}
 */
export function decodeProductSlug(slug) {
  try {
    // 取最後一個 - 之後的部分作為編碼 ID
    const parts = slug.split("-");
    const encodedId = parts[parts.length - 1];

    // 還原 base64 padding 和特殊字符
    let base64 = encodedId.replace(/-/g, "+").replace(/_/g, "/");
    const padding = (4 - (base64.length % 4)) % 4;
    base64 += "=".repeat(padding);

    return atob(base64);
  } catch {
    return null;
  }
}
