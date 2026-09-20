-- fix_wishlist_product_duplicate.sql

DELETE w1 FROM wishlist_product w1
INNER JOIN wishlist_product w2
    ON w1.product_board_id = w2.product_board_id
    AND w1.member_id = w2.member_id
    AND w1.id > w2.id;

ALTER TABLE wishlist_product
    ADD CONSTRAINT uk_wishlist_product UNIQUE (product_board_id, member_id);
