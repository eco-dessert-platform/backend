package com.bbangle.bbangle.wishListBoard.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWishlistProduct is a Querydsl query type for WishlistProduct
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWishlistProduct extends EntityPathBase<WishlistProduct> {

    private static final long serialVersionUID = -1083689232L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWishlistProduct wishlistProduct = new QWishlistProduct("wishlistProduct");

    public final com.bbangle.bbangle.common.domain.QBaseEntity _super = new com.bbangle.bbangle.common.domain.QBaseEntity(this);

    public final com.bbangle.bbangle.board.domain.QBoard board;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final com.bbangle.bbangle.wishListFolder.domain.QWishlistFolder wishlistFolder;

    public QWishlistProduct(String variable) {
        this(WishlistProduct.class, forVariable(variable), INITS);
    }

    public QWishlistProduct(Path<? extends WishlistProduct> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWishlistProduct(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWishlistProduct(PathMetadata metadata, PathInits inits) {
        this(WishlistProduct.class, metadata, inits);
    }

    public QWishlistProduct(Class<? extends WishlistProduct> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.board = inits.isInitialized("board") ? new com.bbangle.bbangle.board.domain.QBoard(forProperty("board"), inits.get("board")) : null;
        this.wishlistFolder = inits.isInitialized("wishlistFolder") ? new com.bbangle.bbangle.wishListFolder.domain.QWishlistFolder(forProperty("wishlistFolder"), inits.get("wishlistFolder")) : null;
    }

}

