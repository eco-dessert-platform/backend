package com.bbangle.bbangle.board.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "product_detail")
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_board_id")
    private Board board;

    @Column(name = "img_index")
    private int imgIndex;

    @Column(name = "url")
    private String url;
}
