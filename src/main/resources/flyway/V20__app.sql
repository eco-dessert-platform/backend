UPDATE product_board
SET board_detail_id = NULL
WHERE board_detail_id IN (
    SELECT pb.board_detail_id
    FROM product_board pb
             LEFT JOIN product_detail pd ON pd.id = pb.board_detail_id
    WHERE pd.id IS NULL
);
