-- =========================================
-- 파라미터(원하는 규모로 조절)
-- =========================================
SET @NUM_BRANDS            = 50;
SET @NUM_USERS             = 50000;   -- 유저 수
SET @NUM_PRODUCTS          = 200000;  -- 최소 10만 이상 요구 → 기본 20만
SET @OPTIONS_PER_PRODUCT   = 3;       -- 제품옵션 수(제품당 3개 → 총 60만개)
SET @NUM_COUPONS           = 200;
SET @NUM_USER_COUPONS      = 150000;  -- (user, coupon) 유니크 제약 주의
SET @NUM_ORDERS            = 100000;  -- 주문 수
SET @ORDER_ITEMS_PER_ORDER = 2;       -- 주문당 아이템 수
SET @NUM_LIKES             = 120000;  -- (user, product) 유니크
SET @NUM_VIEWS             = 120000;  -- (user, product) 유니크
SET @NUM_POINT_HIST        = 200000;

-- 재귀 CTE 최대 깊이 확장 (대량 생성용)
SET SESSION cte_max_recursion_depth = 2000000;

-- 편의용 숫자 시퀀스 CTE (필요할 때 재사용)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < GREATEST(
            @NUM_BRANDS,
            @NUM_USERS,
            @NUM_PRODUCTS,
            @NUM_USER_COUPONS,
            @NUM_ORDERS,
            @NUM_LIKES,
            @NUM_VIEWS,
            @NUM_POINT_HIST
                                    )
)
SELECT 1; -- 시퀀스 준비 끝 (더미 셀렉트)


-- =========================================
-- brands
-- =========================================
INSERT INTO brands (brand_name, description, created_at, updated_at)
WITH RECURSIVE t AS (
    SELECT 1 AS n
    UNION ALL SELECT n+1 FROM t WHERE n < @NUM_BRANDS
)
SELECT CONCAT('브랜드 ', n),
       CONCAT('브랜드 설명 ', n),
       NOW(6), NOW(6)
FROM t;

-- =========================================
-- users (user_name 유니크 주의)
-- =========================================
INSERT INTO users (birth_date, gender, created_at, updated_at, email, user_name)
WITH RECURSIVE t AS (
    SELECT 1 AS n
    UNION ALL SELECT n+1 FROM t WHERE n < @NUM_USERS
)
SELECT DATE_ADD('1970-01-01', INTERVAL (n MOD 20000) DAY),   -- 1970~2024 안쪽 랜덤 분포
       (n MOD 3) + 1,                                        -- gender: 1,2,3 정도로
       NOW(6), NOW(6),
       CONCAT('user', LPAD(n, 6, '0'), '@example.com'),
       CONCAT('user', LPAD(n, 6, '0'))
FROM t;


-- =========================================
-- products (ref_brand_id 랜덤)
-- =========================================
INSERT INTO products (base_price, created_at, deleted_at, product_id, ref_brand_id, updated_at, product_name)
WITH RECURSIVE t AS (
    SELECT 1 AS n
    UNION ALL SELECT n+1 FROM t WHERE n < @NUM_PRODUCTS
)
SELECT
    /* base_price */  1000 + (n MOD 100000),                 -- 1,000 ~ 100,999
                      NOW(6),
                      NULL,
                      NULL,                                                    -- AUTO_INCREMENT, 명시 NULL
                      1 + FLOOR(RAND(n) * @NUM_BRANDS),                        -- 1..@NUM_BRANDS
                      NOW(6),
                      CONCAT('상품 ', LPAD(n, 7, '0'))
FROM t;


-- =========================================
-- product_options (제품당 @OPTIONS_PER_PRODUCT 개 생성)
-- =========================================
-- 작은 시퀀스 1..@OPTIONS_PER_PRODUCT
INSERT INTO product_options (additional_price, created_at, deleted_at, ref_product_id, updated_at, product_option_name)
WITH RECURSIVE opt(n) AS (
    SELECT 1
    UNION ALL SELECT n+1 FROM opt WHERE n < @OPTIONS_PER_PRODUCT
)
SELECT
    /* additional_price */ (o.n * 500) + (p.n MOD 300),      -- 옵션별 + 약간의 변동
                           NOW(6),
                           NULL,
                           p.n,                                                     -- ref_product_id = 제품 id
                           NOW(6),
                           CONCAT('옵션-', o.n, ' (', LPAD(p.n,7,'0'), ')')
FROM (
         WITH RECURSIVE t AS (
             SELECT 1 AS n
             UNION ALL SELECT n+1 FROM t WHERE n < @NUM_PRODUCTS
         )
         SELECT n FROM t
     ) AS p
         JOIN opt o ON 1=1;


-- =========================================
-- product_stocks (옵션당 1개, UNIQUE(ref_product_option_id))
-- =========================================
INSERT INTO product_stocks (quantity, created_at, deleted_at, ref_product_option_id, updated_at)
SELECT
    10 + (po.product_option_id MOD 90),  -- 10~99
    NOW(6),
    NULL,
    po.product_option_id,
    NOW(6)
FROM product_options po;


-- =========================================
-- points (유저별 1행, UNIQUE(ref_user_id))
-- =========================================
INSERT INTO points (balance, created_at, deleted_at, ref_user_id, updated_at)
WITH RECURSIVE t AS (
    SELECT 1 AS n
    UNION ALL SELECT n+1 FROM t WHERE n < @NUM_USERS
)
SELECT
    (n MOD 50000),  -- 0~49999
    NOW(6), NULL,
    n,
    NOW(6)
FROM t;

-- point_histories
INSERT INTO point_histories (cause, amount, created_at, deleted_at, ref_user_id, updated_at)
WITH RECURSIVE t AS (
    SELECT 1 AS n
    UNION ALL SELECT n+1 FROM t WHERE n < @NUM_POINT_HIST
)
SELECT
    (n MOD 4),                        -- 0~3
    (CASE WHEN n MOD 2 = 0 THEN 500 ELSE -300 END),
    NOW(6), NULL,
    1 + ((n*11) MOD @NUM_USERS),
    NOW(6)
FROM t;


-- =========================================
-- liked_products ((user, product) 유니크)
-- =========================================
INSERT INTO liked_products (created_at, deleted_at, ref_product_id, ref_user_id, updated_at)
WITH RECURSIVE t AS (
    SELECT 1 AS k
    UNION ALL SELECT k+1 FROM t WHERE k < @NUM_LIKES
)
SELECT
    NOW(6), NULL,
    1 + ((k*19) MOD @NUM_PRODUCTS),
    1 + ((k*23) MOD @NUM_USERS),
    NOW(6)
FROM t;


-- =========================================
-- viewed_products ((user, product) 유니크)
-- =========================================
INSERT INTO viewed_products (created_at, deleted_at, ref_product_id, ref_user_id, updated_at, view_count)
WITH RECURSIVE t AS (
    SELECT 1 AS k
    UNION ALL SELECT k+1 FROM t WHERE k < @NUM_VIEWS
)
SELECT
    NOW(6), NULL,
    1 + ((k*29) MOD @NUM_PRODUCTS),
    1 + ((k*31) MOD @NUM_USERS),
    NOW(6),
    1 + (k MOD 50)   -- 1~50 회
FROM t;


/*
-- =========================================
-- coupons & coupon_stocks
-- =========================================
INSERT INTO coupons (discount_rule, discount_value, max_discount_amount,
                     created_at, deleted_at, ended_at, revoked_at, started_at, updated_at,
                     coupon_name, coupon_validity_period)
WITH RECURSIVE t AS (
    SELECT 1 AS n
    UNION ALL SELECT n+1 FROM t WHERE n < @NUM_COUPONS
)
SELECT
    (n MOD 2)+1,                                 -- 1:정액,2:정율
    CASE (n MOD 3)
        WHEN 1 THEN 5 + (n MOD 20)               -- 정율: 5~24%
        ELSE 1000 + (n MOD 9000)                 -- 정액: 1,000~9,999
        END,
    20000 + (n MOD 80000),                     -- 최대 할인 상한
    NOW(6), NULL,
    DATE_ADD(NOW(6), INTERVAL 90 DAY),
    NULL,
    NOW(6),
    NOW(6),
    CONCAT('쿠폰 ', LPAD(n,4,'0')),
    CASE (n MOD 5)
        WHEN 0 THEN 'P7D'
        WHEN 1 THEN 'P14D'
        WHEN 2 THEN 'P1M'
        WHEN 3 THEN 'P3M'
        WHEN 4 THEN 'P1Y'
        END
FROM t;

INSERT INTO coupon_stocks (quantity, created_at, deleted_at, ref_coupon_id, updated_at)
SELECT
    1000 + (c.coupon_id MOD 5000),
    NOW(6), NULL,
    c.coupon_id,
    NOW(6)
FROM coupons c;


-- =========================================
-- user_coupons ((user, coupon) 유니크)
-- =========================================
-- user_id와 coupon_id의 주기를 어긋나게 매핑하여 중복 회피
INSERT INTO user_coupons (coupon_used, created_at, deleted_at, ended_at,
                          ref_coupon_id, ref_user_id, started_at, updated_at, version)
WITH RECURSIVE t AS (
    SELECT 1 AS k
    UNION ALL SELECT k+1 FROM t WHERE k < @NUM_USER_COUPONS
)
SELECT
    b'0',
    NOW(6), NULL,
    DATE_ADD(NOW(6), INTERVAL 60 DAY),
    1 + ((k*7) MOD @NUM_COUPONS),          -- 1..@NUM_COUPONS (7배수로 어긋나게)
    1 + ((k*13) MOD @NUM_USERS),           -- 1..@NUM_USERS  (13배수로 어긋나게)
    NOW(6),
    NOW(6),
    0
FROM t;


-- =========================================
-- orders (order_id = UUID → BINARY(16))
-- =========================================
INSERT INTO orders (discount_amount, status, created_at, deleted_at, ref_user_id,
                    total_price, updated_at, order_id)
WITH RECURSIVE t AS (
    SELECT 1 AS n
    UNION ALL SELECT n+1 FROM t WHERE n < @NUM_ORDERS
)
SELECT
    (n MOD 5) * 1000,                   -- 0,1000,2000...
    (n MOD 4)+1,                        -- 상태 1~4 가정
    NOW(6), NULL,
    1 + ((n*17) MOD @NUM_USERS),
    20000 + (n MOD 500000),             -- 총액
    NOW(6),
    UNHEX(REPLACE(UUID(), '-', ''))
FROM t;


-- =========================================
-- order_products (주문당 N개)
-- =========================================
-- product_option_id 범위 확보
SET @MAX_PO_ID := (SELECT MAX(product_option_id) FROM product_options);

-- 주문 키 시퀀스
INSERT INTO order_products (price, quantity, created_at, deleted_at,
                            ref_product_option_id, updated_at, ref_order_id)
WITH RECURSIVE ord AS (
    SELECT 1 AS rn
    UNION ALL SELECT rn+1 FROM ord WHERE rn < @NUM_ORDERS
),
               items AS (
                   SELECT 1 AS i
                   UNION ALL SELECT i+1 FROM items WHERE i < @ORDER_ITEMS_PER_ORDER
               )
SELECT
    5000 + (o.rn * i * 37 MOD 200000),     -- 대략적 가격
    1 + ((o.rn + i) MOD 3),                -- 1~3개
    NOW(6), NULL,
    1 + ((o.rn*123 + i*97) MOD @MAX_PO_ID),
    NOW(6),
    -- o.rn 번째 주문의 order_id를 구해 매핑
    (SELECT order_id FROM orders LIMIT 1 OFFSET o.rn-1)
FROM ord o
         JOIN items i ON 1=1;


-- =========================================
-- payments (주문 1건당 1결제)
-- =========================================
INSERT INTO payments (method, status, amount, created_at, deleted_at,
                      ref_user_id, updated_at, ref_order_id)
SELECT
    (o.status MOD 3)+1,                -- 결제수단 1~3
    (o.status),                        -- 상태를 주문 상태에 맞춰
    o.total_price - o.discount_amount,
    NOW(6), NULL,
    o.ref_user_id,
    NOW(6),
    o.order_id
FROM orders o;


-- =========================================
-- order_coupons (일부 주문에 쿠폰 1개 연결, user_coupon 유니크 제약 준수)
-- =========================================
-- user_coupons에서 앞쪽 일부만 사용해 매핑
INSERT INTO order_coupons (created_at, deleted_at, order_coupon_id, ref_user_coupon_id, updated_at, ref_order_id)
SELECT
    NOW(6), NULL, NULL,
    uc.user_coupon_id,
    NOW(6),
    o.order_id
FROM (
         SELECT order_id FROM orders ORDER BY created_at LIMIT LEAST(@NUM_ORDERS, @NUM_USER_COUPONS)
     ) o
         JOIN (
    SELECT user_coupon_id FROM user_coupons ORDER BY created_at LIMIT LEAST(@NUM_ORDERS, @NUM_USER_COUPONS)
) uc ON 1=1;
*/
