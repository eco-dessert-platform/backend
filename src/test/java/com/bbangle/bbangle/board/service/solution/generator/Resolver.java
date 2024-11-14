package com.bbangle.bbangle.board.service.solution.generator;

public interface Resolver<TInput, TOutput> {
    TOutput resolve(TInput tInput);
}
