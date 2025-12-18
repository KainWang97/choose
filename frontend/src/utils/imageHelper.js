/**
 * 圖片處理工具函數
 * 用於優化 Cloudinary 圖片 URL，提升載入速度
 */

/**
 * 優化 Cloudinary 圖片 URL
 * 自動加入格式轉換、品質壓縮、尺寸限制參數
 *
 * @param {string} url - 原始 Cloudinary URL
 * @param {Object} options - 選項
 * @param {number} [options.width] - 限制寬度（預設不限制）
 * @param {number} [options.height] - 限制高度（預設不限制）
 * @param {string} [options.quality='auto'] - 品質（auto, auto:low, auto:eco, auto:good, auto:best）
 * @param {string} [options.format='auto'] - 格式（auto, webp, avif, jpg, png）
 * @returns {string} 優化後的 URL
 *
 * @example
 * // 縮圖 400px
 * optimizeImageUrl(url, { width: 400 })
 *
 * // 大圖 800px，高品質
 * optimizeImageUrl(url, { width: 800, quality: 'auto:good' })
 */
export function optimizeImageUrl(url, options = {}) {
  // 非 Cloudinary URL 直接回傳
  if (!url || !url.includes("cloudinary.com")) {
    return url;
  }

  const { width, height, quality = "auto", format = "auto" } = options;

  // 組合轉換參數
  const transforms = [`f_${format}`, `q_${quality}`];

  if (width) {
    transforms.push(`w_${width}`);
  }

  if (height) {
    transforms.push(`h_${height}`);
  }

  // 加入 crop 模式（當有尺寸限制時）
  if (width || height) {
    transforms.push("c_limit"); // limit 模式不會放大圖片
  }

  const transformString = transforms.join(",");

  // 將轉換參數插入 /upload/ 後面
  return url.replace("/upload/", `/upload/${transformString}/`);
}

/**
 * 取得縮圖 URL（用於商品列表）
 * @param {string} url - 原始 URL
 * @param {number} [size=600] - 縮圖尺寸（對應 Eager Transformation 版本）
 * @returns {string} 優化後的 URL
 */
export function getThumbnailUrl(url, size = 600) {
  return optimizeImageUrl(url, { width: size, quality: "auto:best" });
}

/**
 * 取得大圖 URL（用於商品詳情）
 * @param {string} url - 原始 URL
 * @param {number} [size=1200] - 圖片尺寸（對應 Eager Transformation 版本）
 * @returns {string} 優化後的 URL
 */
export function getLargeImageUrl(url, size = 1200) {
  return optimizeImageUrl(url, { width: size, quality: "auto:good" });
}
