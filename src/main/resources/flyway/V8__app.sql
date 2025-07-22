INSERT INTO product_img (product_board_id, url, img_order)
SELECT id, profile, 0
FROM product_board
WHERE profile IS NOT NULL;