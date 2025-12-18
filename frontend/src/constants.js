// ============================================
// Mock Users (對應 SQL users)
// ============================================
/** @type {import('./types.js').MockUser[]} */
export const MOCK_USERS = [
  {
    id: "1",
    email: "admin@komorebi.com",
    password: "admin",
    name: "Admin",
    phone: "0912-345-678",
    role: "ADMIN",
  },
  {
    id: "2",
    email: "test@test.com",
    password: "test123",
    name: "測試會員",
    phone: "0987-654-321",
    role: "MEMBER",
  },
];

// ============================================
// Mock Categories (對應 SQL categories)
/** @type {import('./types.js').Category[]} */
export const MOCK_CATEGORIES = [
  {
    id: "1",
    name: "Tea Ritual",
    description: "Traditional tea ceremony items",
  },
  { id: "2", name: "Apparel", description: "Japanese-inspired clothing" },
  { id: "3", name: "Home Decor", description: "Minimalist home decorations" },
  { id: "4", name: "Bath", description: "Bath and wellness products" },
  { id: "5", name: "Accessories", description: "Handcrafted accessories" },
  { id: "6", name: "Kitchen", description: "Artisan kitchen items" },
];

// Category 名稱陣列 (向下相容)
export const CATEGORIES = MOCK_CATEGORIES.map((c) => c.name);

// ============================================
// Mock Product Variants (對應 SQL product_variants)
// ============================================
/** @type {import('./types.js').ProductVariant[]} */
export const MOCK_VARIANTS = [
  // Product 1: Iron Teapot - 單一規格
  {
    id: "v1",
    productId: "1",
    skuCode: "TEBL-F-001",
    color: "Black",
    size: "Free",
    stock: 15,
  },

  // Product 2: Linen Haori Jacket - 多規格
  {
    id: "v2",
    productId: "2",
    skuCode: "APNA-S-001",
    color: "Natural",
    size: "S",
    stock: 3,
  },
  {
    id: "v3",
    productId: "2",
    skuCode: "APNA-M-001",
    color: "Natural",
    size: "M",
    stock: 5,
  },
  {
    id: "v4",
    productId: "2",
    skuCode: "APNA-L-001",
    color: "Natural",
    size: "L",
    stock: 2,
  },
  {
    id: "v5",
    productId: "2",
    skuCode: "APIN-M-001",
    color: "Indigo",
    size: "M",
    stock: 4,
  },

  // Product 3: Ceramic Flower Vase - 單一規格
  {
    id: "v6",
    productId: "3",
    skuCode: "HOGR-F-001",
    color: "Grey",
    size: "Free",
    stock: 20,
  },

  // Product 4: Hinoki Bath Stool - 單一規格
  {
    id: "v7",
    productId: "4",
    skuCode: "BANA-F-001",
    color: "Natural",
    size: "Free",
    stock: 12,
  },

  // Product 5: Indigo Dyed Scarf - 多規格
  {
    id: "v8",
    productId: "5",
    skuCode: "ACDE-F-001",
    color: "Deep Blue",
    size: "Free",
    stock: 10,
  },
  {
    id: "v9",
    productId: "5",
    skuCode: "ACLI-F-001",
    color: "Light Blue",
    size: "Free",
    stock: 15,
  },

  // Product 6: Wooden Bento Box - 單一規格
  {
    id: "v10",
    productId: "6",
    skuCode: "KINA-F-001",
    color: "Natural",
    size: "Free",
    stock: 18,
  },
];

// ============================================
// Mock Products (對應 SQL products)
// ============================================
/** @type {import('./types.js').Product[]} */
export const PRODUCTS = [
  {
    id: "1",
    categoryId: "1",
    category: "Tea Ritual",
    name: "Iron Teapot (Kyusu)",
    description:
      "Hand-cast iron teapot retaining heat for the perfect brew. Crafted in Morioka, this iron teapot develops a unique patina over time. The interior is untreated to allow the iron to subtly enhance the flavor of the tea.",
    price: 120,
    imageUrl: "https://picsum.photos/800/800?random=1",
    isListed: true,
    variants: MOCK_VARIANTS.filter((v) => v.productId === "1"),
    totalStock: MOCK_VARIANTS.filter((v) => v.productId === "1").reduce(
      (sum, v) => sum + v.stock,
      0
    ),
  },
  {
    id: "2",
    categoryId: "2",
    category: "Apparel",
    name: "Linen Haori Jacket",
    description:
      "A modern reinterpretation of the Haori. Made from 100% organic French linen, dyed with natural indigo. Perfect for layering in transitional seasons.",
    price: 240,
    imageUrl: "https://picsum.photos/800/1000?random=2",
    isListed: true,
    variants: MOCK_VARIANTS.filter((v) => v.productId === "2"),
    totalStock: MOCK_VARIANTS.filter((v) => v.productId === "2").reduce(
      (sum, v) => sum + v.stock,
      0
    ),
  },
  {
    id: "3",
    categoryId: "3",
    category: "Home Decor",
    name: "Ceramic Flower Vase",
    description:
      "Wheel-thrown stoneware. The glaze is applied unevenly by hand to create depth and texture, embodying the philosophy of Wabi-sabi.",
    price: 85,
    imageUrl: "https://picsum.photos/800/900?random=3",
    isListed: true,
    variants: MOCK_VARIANTS.filter((v) => v.productId === "3"),
    totalStock: MOCK_VARIANTS.filter((v) => v.productId === "3").reduce(
      (sum, v) => sum + v.stock,
      0
    ),
  },
  {
    id: "4",
    categoryId: "4",
    category: "Bath",
    name: "Hinoki Bath Stool",
    description:
      "Made from high-quality Hinoki cypress, known for its calming scent and resistance to humidity. Smoothly sanded finish.",
    price: 150,
    imageUrl: "https://picsum.photos/800/800?random=4",
    isListed: true,
    variants: MOCK_VARIANTS.filter((v) => v.productId === "4"),
    totalStock: MOCK_VARIANTS.filter((v) => v.productId === "4").reduce(
      (sum, v) => sum + v.stock,
      0
    ),
  },
  {
    id: "5",
    categoryId: "5",
    category: "Accessories",
    name: "Indigo Dyed Scarf",
    description:
      'Utilizing the "Shibori" tie-dye technique. Each piece is unique with slight variations in the pattern.',
    price: 95,
    imageUrl: "https://picsum.photos/800/800?random=5",
    isListed: true,
    variants: MOCK_VARIANTS.filter((v) => v.productId === "5"),
    totalStock: MOCK_VARIANTS.filter((v) => v.productId === "5").reduce(
      (sum, v) => sum + v.stock,
      0
    ),
  },
  {
    id: "6",
    categoryId: "6",
    category: "Kitchen",
    name: "Wooden Bento Box",
    description:
      "Traditional craftsmanship that bends thin sheets of cedar wood. It regulates moisture naturally, keeping rice fresh and delicious.",
    price: 110,
    imageUrl: "https://picsum.photos/800/700?random=6",
    isListed: true,
    variants: MOCK_VARIANTS.filter((v) => v.productId === "6"),
    totalStock: MOCK_VARIANTS.filter((v) => v.productId === "6").reduce(
      (sum, v) => sum + v.stock,
      0
    ),
  },
];

// 新品上架精選商品 IDs (由管理員選擇，最多5個)
/** @type {string[]} */
export const INITIAL_FEATURED_IDS = ["2", "4", "6"];
