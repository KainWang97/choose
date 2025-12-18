/**
 * 前端驗證工具
 * 與後端驗證規則同步
 */

/**
 * Email 格式驗證
 * @param {string} email
 * @returns {boolean}
 */
export const isValidEmail = (email) => {
  if (!email) return false;
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

/**
 * 台灣手機號碼驗證 (09 開頭 + 8 碼數字)
 * @param {string} phone
 * @returns {boolean}
 */
export const isValidPhone = (phone) => {
  if (!phone) return false;
  const phoneRegex = /^09\d{8}$/;
  return phoneRegex.test(phone);
};

/**
 * 密碼驗證 (最少 6 字元)
 * @param {string} password
 * @returns {boolean}
 */
export const isValidPassword = (password) => {
  if (!password) return false;
  return password.length >= 6;
};

/**
 * 必填欄位驗證
 * @param {string} value
 * @returns {boolean}
 */
export const isRequired = (value) => {
  if (!value) return false;
  return value.trim().length > 0;
};

/**
 * 驗證器集合
 */
export const validators = {
  email: isValidEmail,
  phone: isValidPhone,
  password: isValidPassword,
  required: isRequired,
};

/**
 * 驗證錯誤訊息
 */
export const validationMessages = {
  email: "請輸入有效的電子郵件格式",
  phone: "請輸入有效的手機號碼（例：0912345678）",
  password: "密碼長度至少需要 6 個字元",
  required: "此欄位為必填",
  fullName: "請輸入收件人姓名",
  address: "請輸入配送地址",
  city: "請輸入縣市",
};

/**
 * 驗證表單欄位
 * @param {string} field - 欄位名稱
 * @param {string} value - 欄位值
 * @param {string[]} rules - 驗證規則陣列
 * @returns {{valid: boolean, message: string}}
 */
export const validateField = (field, value, rules) => {
  for (const rule of rules) {
    const validator = validators[rule];
    if (validator && !validator(value)) {
      return {
        valid: false,
        message: validationMessages[field] || validationMessages[rule],
      };
    }
  }
  return { valid: true, message: "" };
};
