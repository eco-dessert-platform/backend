package com.bbangle.bbangle.board.domain;

import com.bbangle.bbangle.util.HtmlUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "product_detail")
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardDetail {

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

    public void updateBoard(Board board) {
        this.board = board;
    }

}
