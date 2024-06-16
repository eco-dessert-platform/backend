package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.ProductImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// FIXME: 예찬님 여기 테스트 패키지인데 이거 왜 여깄나요? 확인좀 부탁드려요
@Repository
public interface BoardImgRepository extends JpaRepository<ProductImg, Long> {

}
