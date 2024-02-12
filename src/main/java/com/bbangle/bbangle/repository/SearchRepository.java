package com.bbangle.bbangle.repository;

import com.bbangle.bbangle.dto.StoreResponseDto;
import com.bbangle.bbangle.model.Member;
import com.bbangle.bbangle.model.Search;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SearchRepository extends JpaRepository<Search, Long>, SearchQueryDSLRepository {
}
