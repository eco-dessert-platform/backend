package com.bbangle.bbangle.board.repository.basic;

import com.bbangle.bbangle.board.domain.RandomBoard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RandomBoardRepository extends JpaRepository<RandomBoard, Long> {

}
