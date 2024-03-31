package com.bbangle.bbangle.store.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStore is a Querydsl query type for Store
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStore extends EntityPathBase<Store> {

    private static final long serialVersionUID = -754054649L;

    public static final QStore store = new QStore("store");

    public final com.bbangle.bbangle.common.domain.QBaseEntity _super = new com.bbangle.bbangle.common.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath identifier = createString("identifier");

    public final StringPath introduce = createString("introduce");

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath name = createString("name");

    public final StringPath profile = createString("profile");

    public final ListPath<com.bbangle.bbangle.wishListStore.domain.WishlistStore, com.bbangle.bbangle.wishListStore.domain.QWishlistStore> wishlistStores = this.<com.bbangle.bbangle.wishListStore.domain.WishlistStore, com.bbangle.bbangle.wishListStore.domain.QWishlistStore>createList("wishlistStores", com.bbangle.bbangle.wishListStore.domain.WishlistStore.class, com.bbangle.bbangle.wishListStore.domain.QWishlistStore.class, PathInits.DIRECT2);

    public QStore(String variable) {
        super(Store.class, forVariable(variable));
    }

    public QStore(Path<? extends Store> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStore(PathMetadata metadata) {
        super(Store.class, metadata);
    }

}

