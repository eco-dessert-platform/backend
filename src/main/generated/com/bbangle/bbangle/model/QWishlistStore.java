package com.bbangle.bbangle.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWishlistStore is a Querydsl query type for WishlistStore
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWishlistStore extends EntityPathBase<WishlistStore> {

    private static final long serialVersionUID = 2108306772L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWishlistStore wishlistStore = new QWishlistStore("wishlistStore");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final QStore store;

    public QWishlistStore(String variable) {
        this(WishlistStore.class, forVariable(variable), INITS);
    }

    public QWishlistStore(Path<? extends WishlistStore> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWishlistStore(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWishlistStore(PathMetadata metadata, PathInits inits) {
        this(WishlistStore.class, metadata, inits);
    }

    public QWishlistStore(Class<? extends WishlistStore> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
        this.store = inits.isInitialized("store") ? new QStore(forProperty("store")) : null;
    }

}

