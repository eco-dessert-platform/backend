package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.push.domain.PushType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductOrderDto {

    private Long id;
    private String title;
    private Integer price;
    private Category category;
    private Boolean glutenFreeTag;
    private Boolean highProteinTag;
    private Boolean sugarFreeTag;
    private Boolean veganTag;
    private Boolean ketogenicTag;
    private Integer sugars;
    private Integer protein;
    private Integer carbohydrates;
    private Integer fat;
    private Integer weight;
    private Integer calories;
    private Boolean monday;
    private Boolean tuesday;
    private Boolean wednesday;
    private Boolean thursday;
    private Boolean friday;
    private Boolean saturday;
    private Boolean sunday;
    private LocalDateTime orderStartDate;
    private LocalDateTime orderEndDate;
    private Boolean soldout;
    private PushType pushType;
    private String days;
    private Boolean isActive;
}
