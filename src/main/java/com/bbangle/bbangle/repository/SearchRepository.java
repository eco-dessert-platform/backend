package com.bbangle.bbangle.repository;

import com.bbangle.bbangle.model.Member;
import com.bbangle.bbangle.model.Search;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SearchRepository extends JpaRepository<Search, Long>, SearchQueryDSLRepository {

    @Transactional
    @Modifying
    @Query("UPDATE Search s SET s.isDeleted = true WHERE s.id = :keywordId AND s.member = :member")
    void markAsDeleted(@Param("keywordId") Long keywordId, @Param("member") Member member);
}
