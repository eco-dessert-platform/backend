package com.bbangle.bbangle.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSearch is a Querydsl query type for Search
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSearch extends EntityPathBase<Search> {

    public static final QSearch search = new QSearch("search");
    private static final long serialVersionUID = -245201552L;
    private static final PathInits INITS = PathInits.DIRECT2;
    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt",
        java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final StringPath keyword = createString("keyword");

    public final QMember member;

    public QSearch(String variable) {
        this(Search.class, forVariable(variable), INITS);
    }

    public QSearch(Path<? extends Search> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSearch(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSearch(PathMetadata metadata, PathInits inits) {
        this(Search.class, metadata, inits);
    }

    public QSearch(Class<? extends Search> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
    }

}

