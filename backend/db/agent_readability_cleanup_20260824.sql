-- GooShare local data cleanup for accurate Agent retrieval.
-- Data-only migration: no CREATE, ALTER, DROP, or TRUNCATE statements.

-- Complete the brand dictionary used by BrandMapper's exact-name lookup.
INSERT INTO brand (id, name)
SELECT 66, '美旅'
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE name = '美旅');

-- Correct product categories from the product title/description and the category dictionary.
UPDATE item
SET category_id = CASE id
        WHEN 1 THEN 5 WHEN 4 THEN 3 WHEN 5 THEN 4 WHEN 15 THEN 2 WHEN 20 THEN 4
        WHEN 21 THEN 5 WHEN 33 THEN 2 WHEN 35 THEN 2 WHEN 41 THEN 2 WHEN 42 THEN 3
        WHEN 43 THEN 3 WHEN 44 THEN 3 WHEN 45 THEN 3 WHEN 46 THEN 3 WHEN 47 THEN 3
        WHEN 48 THEN 3 WHEN 50 THEN 3 WHEN 51 THEN 3 WHEN 52 THEN 4 WHEN 53 THEN 4
        WHEN 55 THEN 4 WHEN 56 THEN 4 WHEN 59 THEN 5 WHEN 60 THEN 7 WHEN 61 THEN 7
        WHEN 71 THEN 2 WHEN 72 THEN 2 WHEN 73 THEN 2 WHEN 76 THEN 3 WHEN 77 THEN 3
        WHEN 78 THEN 3 WHEN 79 THEN 8 WHEN 80 THEN 3 WHEN 81 THEN 4 WHEN 82 THEN 4
        WHEN 86 THEN 2 WHEN 87 THEN 2 WHEN 88 THEN 2 WHEN 89 THEN 2 WHEN 90 THEN 5
        WHEN 91 THEN 7 WHEN 92 THEN 7 WHEN 93 THEN 7 WHEN 95 THEN 2 WHEN 97 THEN 6
        WHEN 99 THEN 2 WHEN 100 THEN 6 WHEN 148 THEN 5 WHEN 154 THEN 5 WHEN 155 THEN 2
        WHEN 157 THEN 4 WHEN 158 THEN 6 ELSE category_id END,
    category_item_id = CASE id
        WHEN 1 THEN 24 WHEN 4 THEN 18 WHEN 5 THEN 21 WHEN 15 THEN 12 WHEN 20 THEN 21
        WHEN 21 THEN 24 WHEN 33 THEN 12 WHEN 35 THEN 14 WHEN 41 THEN 14 WHEN 42 THEN 17
        WHEN 43 THEN 19 WHEN 44 THEN 18 WHEN 45 THEN 17 WHEN 46 THEN 20 WHEN 47 THEN 18
        WHEN 48 THEN 15 WHEN 50 THEN 19 WHEN 51 THEN 20 WHEN 52 THEN 21 WHEN 53 THEN 21
        WHEN 55 THEN 22 WHEN 56 THEN 22 WHEN 59 THEN 26 WHEN 60 THEN 33 WHEN 61 THEN 34
        WHEN 71 THEN 12 WHEN 72 THEN 12 WHEN 73 THEN 12 WHEN 76 THEN 19 WHEN 77 THEN 15
        WHEN 78 THEN 19 WHEN 79 THEN 39 WHEN 80 THEN 18 WHEN 81 THEN 21 WHEN 82 THEN 21
        WHEN 86 THEN 14 WHEN 87 THEN 14 WHEN 88 THEN 14 WHEN 89 THEN 14 WHEN 90 THEN 25
        WHEN 91 THEN 33 WHEN 92 THEN 33 WHEN 93 THEN 34 WHEN 95 THEN 12 WHEN 97 THEN 31
        WHEN 99 THEN 14 WHEN 100 THEN 31 WHEN 148 THEN 25 WHEN 154 THEN 25 WHEN 155 THEN 11
        WHEN 157 THEN 21 WHEN 158 THEN 29 ELSE category_item_id END
WHERE id IN (1,4,5,15,20,21,33,35,41,42,43,44,45,46,47,48,50,51,52,53,55,56,
             59,60,61,71,72,73,76,77,78,79,80,81,82,86,87,88,89,90,91,92,93,95,
             97,99,100,148,154,155,157,158);

-- Fill brand IDs only where the product text names an existing, unambiguous brand.
UPDATE item
SET brand_id = CASE id
        WHEN 16 THEN 30 WHEN 18 THEN 49 WHEN 19 THEN 50 WHEN 20 THEN 51 WHEN 33 THEN 29
        WHEN 34 THEN 52 WHEN 36 THEN 53 WHEN 38 THEN 54 WHEN 40 THEN 55 WHEN 44 THEN 56
        WHEN 45 THEN 57 WHEN 49 THEN 66 WHEN 52 THEN 58 WHEN 53 THEN 51 WHEN 66 THEN 24
        WHEN 69 THEN 59 WHEN 70 THEN 60 WHEN 71 THEN 3 WHEN 73 THEN 29 WHEN 76 THEN 61
        WHEN 77 THEN 62 WHEN 79 THEN 49 WHEN 82 THEN 26 WHEN 83 THEN 63 WHEN 84 THEN 64
        WHEN 87 THEN 53 WHEN 92 THEN 65 WHEN 132 THEN 27 WHEN 133 THEN 27 WHEN 134 THEN 27 WHEN 135 THEN 27
        WHEN 136 THEN 27 WHEN 155 THEN 52 WHEN 158 THEN 28 ELSE brand_id END
WHERE id IN (16,18,19,20,33,34,36,38,40,44,45,49,52,53,66,69,70,71,73,76,77,79,
             82,83,84,87,92,132,133,134,135,136,155,158);

-- Correct two misleading product titles without inventing missing specifications.
UPDATE item SET title = '索尼 A7M3 微单相机' WHERE id = 30;
UPDATE item SET title = '二手绘画数位屏' WHERE id = 93;

-- A listing with no stock is not active.
UPDATE item SET status = 0 WHERE stock = 0 AND status = 1;

-- Remove exact duplicate seed/publish records. None of these IDs is referenced by another table.
DELETE FROM item
WHERE id IN (103,104,105,106,108,109,110,111,113,114,115,116,118,119,120,121,
             123,124,125,126,128,129,130,131,133,134,135,136,138,139,140,141,147);

-- Make legacy comments point to the product actually discussed.
UPDATE comment SET item_id = 28 WHERE id IN (1,2,3);
UPDATE comment SET item_id = 148 WHERE id IN (4,5,6);
UPDATE comment SET item_id = 29 WHERE id IN (7,8);
UPDATE comment SET item_id = 153 WHERE id IN (11,12,13);

-- Seller replies use the user account linked to the listing's seller profile.
UPDATE comment SET user_id = 6 WHERE id = 2;
UPDATE comment SET user_id = 7 WHERE id = 8;
UPDATE comment SET user_id = 10 WHERE id = 10;

-- Repair the one orphan reply and remove a legacy meta-comment that describes the old mismatch.
UPDATE comment SET parent_id = 0, reply_user_id = NULL, reply_user_name = NULL WHERE id = 20;
DELETE FROM comment WHERE id = 21;

-- Replace the only conversation whose subject does not exist in the product catalog.
UPDATE comment SET content = '请问这支球拍适合新手吗？拍框和线况怎么样？' WHERE id = 9;
UPDATE comment SET content = '这款定位适合入门，具体拍框和线况建议面交时确认。' WHERE id = 10;
UPDATE comment SET content = '同排，如果前面的同学不要我排队。' WHERE id = 3;

-- Synchronize denormalized comment identity snapshots with the authoritative user table.
UPDATE comment c
JOIN user u ON u.id = c.user_id
SET c.user_name = u.username, c.user_avatar = u.avatar;

UPDATE comment c
LEFT JOIN user u ON u.id = c.reply_user_id
SET c.reply_user_name = CASE WHEN c.reply_user_id IS NULL THEN NULL ELSE u.username END;

UPDATE comment c
JOIN item i ON i.id = c.item_id
JOIN seller s ON s.id = i.seller_id
SET c.is_seller = (s.user_id = c.user_id);

-- Orders must retain the seller attached to the ordered item.
UPDATE orders o
JOIN item i ON i.id = o.item_id
SET o.seller_id = i.seller_id
WHERE o.seller_id <> i.seller_id;

-- Coupon names describe stable rules; validity and availability come from their dedicated fields.
UPDATE coupon SET title = CASE id
    WHEN 1 THEN '满100减10商品券'
    WHEN 2 THEN '50元无门槛商品券'
    WHEN 3 THEN '双11满500减200商品券'
    WHEN 4 THEN '满10减5限时商品券'
    WHEN 5 THEN '情人节满200减20商品券'
    ELSE title END
WHERE id IN (1,2,3,4,5);

-- Attach threshold coupons to products whose prices actually meet their thresholds.
UPDATE coupon_item_rel SET item_id = CASE id
    WHEN 1 THEN 13 WHEN 2 THEN 1 WHEN 3 THEN 22 WHEN 4 THEN 2 WHEN 5 THEN 14
    ELSE item_id END
WHERE id IN (1,2,3,4,5);

-- ItemSearchMapper returns SELECT * without joining reference tables. Embed readable names in
-- descriptions so the Agent receives semantics rather than opaque category/brand IDs.
UPDATE item i
JOIN category c ON c.id = i.category_id
JOIN category_item ci ON ci.id = i.category_item_id
LEFT JOIN brand b ON b.id = i.brand_id
SET i.description = CONCAT(
    '商品名：', i.title,
    '；品牌：', COALESCE(b.name, '未注明'),
    '；分类：', c.name, ' / ', ci.item_name,
    '；检索词：', TRIM(TRAILING '。' FROM i.description),
    '。具体成色、型号配置、附件和交易方式以卖家确认信息为准。'
)
WHERE i.description NOT LIKE '商品名：%';

-- Idempotent repair for databases where this migration was applied before item 87 was mapped.
UPDATE item
SET description = REPLACE(description, '品牌：未注明', '品牌：宜家')
WHERE id = 87 AND brand_id = 53;
