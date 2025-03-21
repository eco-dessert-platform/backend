UPDATE product_board pb
    JOIN product_detail pd ON pd.product_board_id = pb.id
    SET pb.board_detail_id = pd.id
WHERE pb.board_detail_id IS NULL;