ALTER TABLE product_detail ADD COLUMN content LONGTEXT;
ALTER TABLE product_detail MODIFY img_index INT NOT NULL DEFAULT 0;
ALTER TABLE product_detail MODIFY url VARCHAR(255) NULL;
INSERT INTO product_detail (product_board_id, content, created_at)
SELECT 
    product_board_id,
    CONCAT(
        '<html><body>',
        GROUP_CONCAT(CONCAT('<img src="', url, '">') ORDER BY img_index SEPARATOR ' '),
        '</body></html>'
    ) AS content,
    NOW()
FROM product_detail
GROUP BY product_board_id;

SET SQL_SAFE_UPDATES = 0;
DELETE FROM product_detail WHERE url IS NOT NULL;
SET SQL_SAFE_UPDATES = 1;

ALTER TABLE product_detail DROP COLUMN url;
ALTER TABLE product_detail DROP COLUMN img_index;
