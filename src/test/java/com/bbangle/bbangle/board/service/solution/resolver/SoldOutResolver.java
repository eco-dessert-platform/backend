package com.bbangle.bbangle.board.service.solution.resolver;

import com.bbangle.bbangle.board.service.solution.dao.SoldOutDao;
import java.util.List;

public class SoldOutResolver implements Resolver<List<SoldOutDao>, Boolean> {
    @Override
    public Boolean resolve(List<SoldOutDao> soldOuts) {
        return soldOuts.stream().noneMatch(soldOutDao -> !soldOutDao.isSoldOut());
    }
}
