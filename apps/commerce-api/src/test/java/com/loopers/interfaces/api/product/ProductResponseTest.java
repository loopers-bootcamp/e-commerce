package com.loopers.interfaces.api.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

// If AssertJ is available in the project, uncomment the following and prefer assertThat-style assertions.
// import static org.assertj.core.api.Assertions.assertThat;

class ProductResponseTest {

    @Nested
    @DisplayName("SearchProducts.from")
    class SearchProductsMapping {

        @Test
        @DisplayName("maps full SearchProducts result with multiple items (happy path)")
        void mapsFullSearchProducts() {
            // Arrange: build a fake ProductResult.SearchProducts with two items.
            FakeProductResultItem item1 = new FakeProductResultItem(
                    101L, "Alpha Tee", 1999, 12L, 11L, "BrandA"
            );
            FakeProductResultItem item2 = new FakeProductResultItem(
                    102L, "Beta Hoodie", 4999, 34L, 22L, "BrandB"
            );
            FakeSearchProductsResult input = new FakeSearchProductsResult(
                    7, 140L, 2, 20, List.of(item1, item2)
            );

            // Act
            ProductResponse.SearchProducts out = ProductResponse.SearchProducts.from(input);

            // Assert basic page info
            assertEquals(7, out.getTotalPages());
            assertEquals(140L, out.getTotalItems());
            assertEquals(2, out.getPage());
            assertEquals(20, out.getSize());

            // Assert items mapped correctly
            assertNotNull(out.getItems());
            assertEquals(2, out.getItems().size());

            ProductResponse.SearchProducts.Item mapped1 = out.getItems().get(0);
            assertEquals(101L, mapped1.getProductId());
            assertEquals("Alpha Tee", mapped1.getProductName());
            assertEquals(1999, mapped1.getBasePrice());
            assertEquals(12L, mapped1.getLikeCount());
            assertEquals(11L, mapped1.getBrandId());
            assertEquals("BrandA", mapped1.getBrandName());

            ProductResponse.SearchProducts.Item mapped2 = out.getItems().get(1);
            assertEquals(102L, mapped2.getProductId());
            assertEquals("Beta Hoodie", mapped2.getProductName());
            assertEquals(4999, mapped2.getBasePrice());
            assertEquals(34L, mapped2.getLikeCount());
            assertEquals(22L, mapped2.getBrandId());
            assertEquals("BrandB", mapped2.getBrandName());
        }

        @Test
        @DisplayName("handles empty items list without error")
        void handlesEmptyItems() {
            FakeSearchProductsResult input = new FakeSearchProductsResult(
                    0, 0L, 0, 10, List.of()
            );

            ProductResponse.SearchProducts out = ProductResponse.SearchProducts.from(input);

            assertEquals(0, out.getTotalPages());
            assertEquals(0L, out.getTotalItems());
            assertEquals(0, out.getPage());
            assertEquals(10, out.getSize());
            assertNotNull(out.getItems());
            assertTrue(out.getItems().isEmpty());
        }

        @Test
        @DisplayName("allows null brand values in items (brandless products)")
        void allowsNullBrandValues() {
            FakeProductResultItem brandless = new FakeProductResultItem(
                    201L, "Gamma Cap", 1299, 0L, null, null
            );
            FakeSearchProductsResult input = new FakeSearchProductsResult(
                    1, 1L, 0, 10, List.of(brandless)
            );

            ProductResponse.SearchProducts out = ProductResponse.SearchProducts.from(input);

            assertEquals(1, out.getItems().size());
            ProductResponse.SearchProducts.Item mapped = out.getItems().get(0);
            assertEquals(201L, mapped.getProductId());
            assertEquals("Gamma Cap", mapped.getProductName());
            assertEquals(1299, mapped.getBasePrice());
            assertEquals(0L, mapped.getLikeCount());
            assertNull(mapped.getBrandId());
            assertNull(mapped.getBrandName());
        }

        @Test
        @DisplayName("does not mutate source list (defensive mapping)")
        void doesNotMutateSourceList() {
            ArrayList<FakeProductResultItem> srcItems = new ArrayList<>();
            srcItems.add(new FakeProductResultItem(1L, "One", 100, 0L, 1L, "B1"));
            FakeSearchProductsResult input = new FakeSearchProductsResult(
                    1, 1L, 0, 10, srcItems
            );

            ProductResponse.SearchProducts out = ProductResponse.SearchProducts.from(input);

            // mutate source after mapping
            srcItems.clear();

            // mapped output should remain intact
            assertEquals(1, out.getItems().size());
            assertEquals("One", out.getItems().get(0).getProductName());
        }
    }

    @Nested
    @DisplayName("GetProductDetail.from")
    class GetProductDetailMapping {

        @Test
        @DisplayName("maps full detail with options and brand info (happy path)")
        void mapsFullDetail() {
            FakeDetailOption opt1 = new FakeDetailOption(1001L, "Size M", 0, 501L, 50);
            FakeDetailOption opt2 = new FakeDetailOption(1002L, "Size L", 200, 501L, 20);

            FakeGetProductDetail input = new FakeGetProductDetail(
                    501L, "Omega Jacket", 12999, 77L,
                    List.of(opt1, opt2),
                    901L, "OmegaBrand", "Premium outerwear", 3L
            );

            ProductResponse.GetProductDetail out = ProductResponse.GetProductDetail.from(input);

            assertEquals(501L, out.getProductId());
            assertEquals("Omega Jacket", out.getProductName());
            assertEquals(12999, out.getBasePrice());
            assertEquals(77L, out.getLikeCount());
            assertEquals(901L, out.getBrandId());
            assertEquals("OmegaBrand", out.getBrandName());
            assertEquals("Premium outerwear", out.getBrandDescription());
            assertEquals(3L, out.getRank());

            assertNotNull(out.getOptions());
            assertEquals(2, out.getOptions().size());

            ProductResponse.GetProductDetail.Option m = out.getOptions().get(0);
            assertEquals(1001L, m.getProductOptionId());
            assertEquals("Size M", m.getProductOptionName());
            assertEquals(0, m.getAdditionalPrice());
            assertEquals(501L, m.getProductId());
            assertEquals(50, m.getStockQuantity());

            ProductResponse.GetProductDetail.Option l = out.getOptions().get(1);
            assertEquals(1002L, l.getProductOptionId());
            assertEquals("Size L", l.getProductOptionName());
            assertEquals(200, l.getAdditionalPrice());
            assertEquals(501L, l.getProductId());
            assertEquals(20, l.getStockQuantity());
        }

        @Test
        @DisplayName("handles empty options and null brand/rank fields")
        void handlesEmptyOptionsAndNulls() {
            FakeGetProductDetail input = new FakeGetProductDetail(
                    777L, "Nameless", 0, 0L,
                    List.of(), // empty options
                    null, null, null, null // brandId, brandName, brandDescription, rank
            );

            ProductResponse.GetProductDetail out = ProductResponse.GetProductDetail.from(input);

            assertEquals(777L, out.getProductId());
            assertEquals("Nameless", out.getProductName());
            assertEquals(0, out.getBasePrice());
            assertEquals(0L, out.getLikeCount());

            assertTrue(out.getOptions().isEmpty());
            assertNull(out.getBrandId());
            assertNull(out.getBrandName());
            assertNull(out.getBrandDescription());
            assertNull(out.getRank());
        }

        @Test
        @DisplayName("does not mutate option list after mapping")
        void doesNotMutateOptionsAfterMapping() {
            ArrayList<FakeDetailOption> options = new ArrayList<>();
            options.add(new FakeDetailOption(1L, "Opt", 0, 9L, 9));

            FakeGetProductDetail input = new FakeGetProductDetail(
                    9L, "P", 1, 1L, options, null, null, null, null
            );

            ProductResponse.GetProductDetail out = ProductResponse.GetProductDetail.from(input);

            options.clear(); // mutate source after mapping

            assertEquals(1, out.getOptions().size());
            assertEquals("Opt", out.getOptions().get(0).getProductOptionName());
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Minimal fakes to simulate the getters used by ProductResponse.from(...) without depending on
    // the real domain/application classes. These are simple POJOs providing the required getters.
    // This keeps tests isolated and avoids brittle coupling to builder constructors elsewhere.

    // Fakes for ProductResult.SearchProducts and its item "content"
    private static final class FakeProductResultItem {
        private final Long productId;
        private final String productName;
        private final Integer basePrice;
        private final Long likeCount;
        private final Long brandId;
        private final String brandName;

        FakeProductResultItem(Long productId, String productName, Integer basePrice, Long likeCount,
                              Long brandId, String brandName) {
            this.productId = productId;
            this.productName = productName;
            this.basePrice = basePrice;
            this.likeCount = likeCount;
            this.brandId = brandId;
            this.brandName = brandName;
        }

        Long getProductId() { return productId; }
        String getProductName() { return productName; }
        Integer getBasePrice() { return basePrice; }
        Long getLikeCount() { return likeCount; }
        Long getBrandId() { return brandId; }
        String getBrandName() { return brandName; }
    }

    private static final class FakeSearchProductsResult implements com.loopers.domain.product.ProductResult.SearchProducts {
        private final Integer totalPages;
        private final Long totalItems;
        private final Integer page;
        private final Integer size;
        private final List<FakeProductResultItem> items;

        FakeSearchProductsResult(Integer totalPages, Long totalItems, Integer page, Integer size,
                                 List<FakeProductResultItem> items) {
            this.totalPages = totalPages;
            this.totalItems = totalItems;
            this.page = page;
            this.size = size;
            this.items = items;
        }

        public Integer getTotalPages() { return totalPages; }
        public Long getTotalItems() { return totalItems; }
        public Integer getPage() { return page; }
        public Integer getSize() { return size; }
        public List<FakeProductResultItem> getItems() { return items; }
    }

    // Fakes for ProductOutput.GetProductDetail and its option
    private static final class FakeDetailOption {
        private final Long productOptionId;
        private final String productOptionName;
        private final Integer additionalPrice;
        private final Long productId;
        private final Integer stockQuantity;

        FakeDetailOption(Long productOptionId, String productOptionName, Integer additionalPrice,
                         Long productId, Integer stockQuantity) {
            this.productOptionId = productOptionId;
            this.productOptionName = productOptionName;
            this.additionalPrice = additionalPrice;
            this.productId = productId;
            this.stockQuantity = stockQuantity;
        }

        Long getProductOptionId() { return productOptionId; }
        String getProductOptionName() { return productOptionName; }
        Integer getAdditionalPrice() { return additionalPrice; }
        Long getProductId() { return productId; }
        Integer getStockQuantity() { return stockQuantity; }
    }

    private static final class FakeGetProductDetail implements com.loopers.application.product.ProductOutput.GetProductDetail {
        private final Long productId;
        private final String productName;
        private final Integer basePrice;
        private final Long likeCount;
        private final List<FakeDetailOption> options;
        private final Long brandId;
        private final String brandName;
        private final String brandDescription;
        private final Long rank;

        FakeGetProductDetail(Long productId, String productName, Integer basePrice, Long likeCount,
                             List<FakeDetailOption> options,
                             Long brandId, String brandName, String brandDescription, Long rank) {
            this.productId = productId;
            this.productName = productName;
            this.basePrice = basePrice;
            this.likeCount = likeCount;
            this.options = options;
            this.brandId = brandId;
            this.brandName = brandName;
            this.brandDescription = brandDescription;
            this.rank = rank;
        }

        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public Integer getBasePrice() { return basePrice; }
        public Long getLikeCount() { return likeCount; }
        public List<FakeDetailOption> getOptions() { return options; }
        public Long getBrandId() { return brandId; }
        public String getBrandName() { return brandName; }
        public String getBrandDescription() { return brandDescription; }
        public Long getRank() { return rank; }
    }
}