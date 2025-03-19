SET SQL_SAFE_UPDATES = 0;

UPDATE product_img p
JOIN (
    SELECT id, product_board_id, ROW_NUMBER() OVER (PARTITION BY product_board_id ORDER BY id) AS new_order
    FROM product_img
) ranked ON p.id = ranked.id
SET p.img_order = ranked.new_order;

SET SQL_SAFE_UPDATES = 1;