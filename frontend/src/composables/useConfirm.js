/**
 * useConfirm Composable
 * 提供全域的確認對話框功能，取代原生 confirm()
 */

import { reactive } from "vue";

// 全域狀態
const state = reactive({
  isOpen: false,
  title: "",
  message: "",
  confirmText: "確認",
  cancelText: "取消",
  variant: "info", // 'danger' | 'warning' | 'info'
  resolve: null,
});

/**
 * 顯示確認對話框並返回 Promise
 * @param {Object} options 對話框選項
 * @param {string} [options.title] 標題
 * @param {string} options.message 訊息
 * @param {string} [options.confirmText] 確認按鈕文字
 * @param {string} [options.cancelText] 取消按鈕文字
 * @param {'danger' | 'warning' | 'info'} [options.variant] 樣式變體
 * @returns {Promise<boolean>} - 使用者確認返回 true，取消返回 false
 */
const confirm = (options) => {
  return new Promise((resolve) => {
    state.isOpen = true;
    state.title = options.title || "";
    state.message = options.message;
    state.confirmText = options.confirmText || "確認";
    state.cancelText = options.cancelText || "取消";
    state.variant = options.variant || "info";
    state.resolve = resolve;
  });
};

/**
 * 處理確認
 */
const handleConfirm = () => {
  if (state.resolve) {
    state.resolve(true);
  }
  state.isOpen = false;
  state.resolve = null;
};

/**
 * 處理取消
 */
const handleCancel = () => {
  if (state.resolve) {
    state.resolve(false);
  }
  state.isOpen = false;
  state.resolve = null;
};

/**
 * useConfirm Hook
 */
export const useConfirm = () => {
  return {
    state,
    confirm,
    handleConfirm,
    handleCancel,
  };
};

export default useConfirm;
