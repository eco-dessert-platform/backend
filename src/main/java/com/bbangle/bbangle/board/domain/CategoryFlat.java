package com.bbangle.bbangle.board.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "category_flat")
@Entity
public class CategoryFlat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 대분류 표시명
    @Column(name = "top_name")
    private String topName;

    // 대분류 코드
    @Column(name = "top_code")
    private String topCode;

    // 중분류 표시명
    @Column(name = "sub_name")
    private String subName;

    // 중분류 코드
    @Column(name = "sub_code")
    private String subCode;
}
