package com.bbangle.bbangle.board.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductImg is a Querydsl query type for ProductImg
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductImg extends EntityPathBase<ProductImg> {

    private static final long serialVersionUID = 757370291L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductImg productImg = new QProductImg("productImg");

    public final QBoard board;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath url = createString("url");

    public QProductImg(String variable) {
        this(ProductImg.class, forVariable(variable), INITS);
    }

    public QProductImg(Path<? extends ProductImg> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductImg(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductImg(PathMetadata metadata, PathInits inits) {
        this(ProductImg.class, metadata, inits);
    }

    public QProductImg(Class<? extends ProductImg> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.board = inits.isInitialized("board") ? new QBoard(forProperty("board"), inits.get("board")) : null;
    }

}

