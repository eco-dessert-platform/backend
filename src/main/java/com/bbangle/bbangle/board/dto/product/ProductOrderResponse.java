package com.bbangle.bbangle.board.dto.product;

import com.bbangle.bbangle.board.domain.OrderTypeEnum;
import com.bbangle.bbangle.board.dto.Nutrient;
import com.bbangle.bbangle.board.dto.OrderAvailableDate;
import com.bbangle.bbangle.board.dto.OrderAvailableWeek;
import com.bbangle.bbangle.board.dto.OrderDateByUserDTO;
import com.bbangle.bbangle.board.dto.OrderWeekByUserDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductOrderResponse {

    private Long id;
    private String title;
    private Boolean glutenFreeTag;
    private Boolean highProteinTag;
    private Boolean sugarFreeTag;
    private Boolean veganTag;
    private Boolean ketogenicTag;
    private Integer price;
    private Nutrient nutrient;
    @JsonInclude(Include.NON_NULL)
    private OrderAvailableWeek orderAvailableWeek;
    @JsonInclude(Include.NON_NULL)
    private OrderWeekByUserDTO appliedOrderWeek;
    @JsonInclude(Include.NON_NULL)
    private OrderAvailableDate orderAvailableDate;
    @JsonInclude(Include.NON_NULL)
    private OrderDateByUserDTO appliedOrderDate;
    @JsonInclude(Include.NON_NULL)
    private OrderTypeEnum orderType;
    @JsonInclude(Include.NON_NULL)
    private Boolean isNotified;
    private Boolean isSoldout;
}
