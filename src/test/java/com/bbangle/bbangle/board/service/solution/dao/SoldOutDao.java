package com.bbangle.bbangle.board.service.solution.dao;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SoldOutDao implements Dao {
    boolean soldOut;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        SoldOutDao other = (SoldOutDao) obj;
        return soldOut == other.soldOut;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(soldOut);
    }
}
