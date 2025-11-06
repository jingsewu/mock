package org.openwes.mock.init.data;

import lombok.Data;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class SkuDataGenerator {

    private static final Random random = new Random();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final AtomicLong idCounter = new AtomicLong(1);

    // Sample data for random generation
    private static final String[] BRANDS = {"Nike", "Adidas", "Apple", "Samsung", "Sony", "LG", "HP", "Dell", "Lenovo", "Asus"};
    private static final String[] COLORS = {"Red", "Blue", "Green", "Black", "White", "Yellow", "Purple", "Pink", "Gray", "Silver"};
    private static final String[] SIZES = {"S", "M", "L", "XL", "XXL", "XS", "32", "34", "36", "38", "40", "42"};
    private static final String[] CATEGORIES = {"Electronics", "Clothing", "Food", "Books", "Home", "Sports", "Beauty", "Toys", "Automotive", "Health"};
    private static final String[] UNITS = {"PCS", "BOX", "KG", "L", "M", "SET", "PAIR", "CARTON"};
    private static final String[] STYLES = {"Casual", "Formal", "Sport", "Vintage", "Modern", "Classic", "Trendy"};

    @Data
    public static class SkuRecord {
        private long id;
        private long createTime;
        private String createUser;
        private long updateTime;
        private String updateUser;
        private String barcodeRuleCode;
        private String brand;
        private boolean calculateHeat;
        private String color;
        private int effectiveDays;
        private boolean enableEffective;
        private boolean enableSn;
        private long grossWeight;
        private String heat;
        private long height;
        private String imageUrl;
        private long length;
        private Integer maxStock;
        private Integer minStock;
        private long netWeight;
        private boolean noBarcode;
        private String ownerCode;
        private int shelfLife;
        private String size;
        private String skuAttributeCategory;
        private String skuAttributeSubCategory;
        private String skuCode;
        private String skuFirstCategory;
        private String skuName;
        private String skuSecondCategory;
        private String skuThirdCategory;
        private String style;
        private String unit;
        private Long version;
        private long volume;
        private String warehouseCode;
        private long width;

        // Constructor, getters, and setters
        public SkuRecord() {
        }

        // Getters and setters (generated or manually created)
        // ... (omitted for brevity, but you should generate them)
    }

    /**
     * Generate a random SKU record
     */
    public static SkuRecord generateSkuRecord(String warehouseCode, String ownerCode) {
        SkuRecord record = new SkuRecord();

        long currentTime = System.currentTimeMillis();
        String skuCode = generateRandomSkuCode();

        record.setId(idCounter.getAndIncrement());
        record.setCreateTime(currentTime);
        record.setCreateUser("SYSTEM");
        record.setUpdateTime(currentTime);
        record.setUpdateUser("SYSTEM");
        record.setBarcodeRuleCode("BRC" + String.format("%06d", random.nextInt(1000000)));
        record.setBrand(BRANDS[random.nextInt(BRANDS.length)]);
        record.setCalculateHeat(random.nextBoolean());
        record.setColor(COLORS[random.nextInt(COLORS.length)]);
        record.setEffectiveDays(30 + random.nextInt(365)); // 30-395 days
        record.setEnableEffective(random.nextBoolean());
        record.setEnableSn(random.nextBoolean());
        record.setGrossWeight(100 + random.nextInt(5000)); // 100-5100
        record.setHeat("MEDIUM"); // Simplified
        record.setHeight(10 + random.nextInt(100));
        record.setImageUrl("/images/" + skuCode + ".jpg");
        record.setLength(10 + random.nextInt(100));
        record.setMaxStock(1000 + random.nextInt(9000));
        record.setMinStock(10 + random.nextInt(100));
        record.setNetWeight(record.getGrossWeight() - random.nextInt(100));
        record.setNoBarcode(random.nextBoolean());
        record.setOwnerCode(ownerCode);
        record.setShelfLife(180 + random.nextInt(720)); // 180-900 days
        record.setSize(SIZES[random.nextInt(SIZES.length)]);
        record.setSkuAttributeCategory("ATTR_" + random.nextInt(10));
        record.setSkuAttributeSubCategory("SUB_ATTR_" + random.nextInt(20));
        record.setSkuCode(skuCode);
        record.setSkuFirstCategory(CATEGORIES[random.nextInt(CATEGORIES.length)]);
        record.setSkuName("Product " + skuCode);
        record.setSkuSecondCategory("Sub" + CATEGORIES[random.nextInt(CATEGORIES.length)]);
        record.setSkuThirdCategory("Detail" + CATEGORIES[random.nextInt(CATEGORIES.length)]);
        record.setStyle(STYLES[random.nextInt(STYLES.length)]);
        record.setUnit(UNITS[random.nextInt(UNITS.length)]);
        record.setVersion(1L);
        record.setVolume(record.getLength() * record.getWidth() * record.getHeight());
        record.setWarehouseCode(warehouseCode);
        record.setWidth(10 + random.nextInt(100));

        return record;
    }

    /**
     * Generate random SKU code
     */
    private static String generateRandomSkuCode() {
        StringBuilder sb = new StringBuilder("SKU");
        for (int i = 0; i < 10; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
