ALTER TABLE product_board
    ADD COLUMN is_crawling TINYINT(1) NOT NULL DEFAULT 0
    COMMENT '크롤링 여부';

UPDATE product_board
SET is_crawling = 1;