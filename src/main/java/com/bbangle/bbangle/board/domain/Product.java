package com.bbangle.bbangle.board.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Table(name = "product")
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter // board 에 product 세팅해서 저장해도 product 는 저장안되서 수기로 product 에서 board 세팅해줘야함...
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_board_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Board board;

    @Column(name = "title")
    private String title;

    @Column(name = "price")
    private int price;

    @Column(name = "category", columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "gluten_free_tag", columnDefinition = "tinyint")
    private boolean glutenFreeTag;

    @Column(name = "high_protein_tag", columnDefinition = "tinyint")
    private boolean highProteinTag;

    @Column(name = "sugar_free_tag", columnDefinition = "tinyint")
    private boolean sugarFreeTag;

    @Column(name = "vegan_tag", columnDefinition = "tinyint")
    private boolean veganTag;

    @Column(name = "ketogenic_tag", columnDefinition = "tinyint")
    private boolean ketogenicTag;

    @Column(name = "sugars")
    private Integer sugars;

    @Column(name = "protein")
    private Integer protein;

    @Column(name = "carbohydrates")
    private Integer carbohydrates;

    @Column(name = "fat")
    private Integer fat;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "calories")
    private Integer calories;

    @Column(name = "monday", columnDefinition = "tinyint")
    private boolean monday;

    @Column(name = "tuesday", columnDefinition = "tinyint")
    private boolean tuesday;

    @Column(name = "wednesday", columnDefinition = "tinyint")
    private boolean wednesday;

    @Column(name = "thursday", columnDefinition = "tinyint")
    private boolean thursday;

    @Column(name = "friday", columnDefinition = "tinyint")
    private boolean friday;

    @Column(name = "saturday", columnDefinition = "tinyint")
    private boolean saturday;

    @Column(name = "sunday", columnDefinition = "tinyint")
    private boolean sunday;

    @Column(name = "order_start_date")
    private LocalDateTime orderStartDate;

    @Column(name = "order_end_date")
    private LocalDateTime orderEndDate;

    @NotNull
    @Column(name = "is_soldout", columnDefinition = "tinyint")
    private boolean soldout;

    // True인 태그 스트링 리스트로 만들어 반환
    public List<String> getTags() {
        return Stream.of(
                Map.entry(glutenFreeTag, TagEnum.GLUTEN_FREE.label()),
                Map.entry(highProteinTag, TagEnum.HIGH_PROTEIN.label()),
                Map.entry(sugarFreeTag, TagEnum.SUGAR_FREE.label()),
                Map.entry(veganTag, TagEnum.VEGAN.label()),
                Map.entry(ketogenicTag, TagEnum.KETOGENIC.label())
            )
            .filter(Map.Entry::getKey)
            .map(Map.Entry::getValue)
            .toList();
    }
}
