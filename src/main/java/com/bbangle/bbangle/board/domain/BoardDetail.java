package com.bbangle.bbangle.board.domain;

import com.bbangle.bbangle.common.domain.SoftDeleteBaseEntity;
import com.bbangle.bbangle.util.HtmlUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "product_detail")
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardDetail extends SoftDeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(mappedBy = "boardDetail")
    private Board board;

    @Lob
    private String content;

    public String getFullUrl() {
        return HtmlUtils.convertHtmlWithFullImageUrls2(content);
    }

    public void update(String content) {
        this.content = content;
    }

    public void updateBoard(Board board) {
        this.board = board;
    }

}
