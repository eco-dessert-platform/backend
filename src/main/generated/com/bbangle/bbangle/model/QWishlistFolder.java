package com.bbangle.bbangle.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWishlistFolder is a Querydsl query type for WishlistFolder
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWishlistFolder extends EntityPathBase<WishlistFolder> {

    public static final QWishlistFolder wishlistFolder = new QWishlistFolder("wishlistFolder");
    private static final long serialVersionUID = 556101211L;
    private static final PathInits INITS = PathInits.DIRECT2;
    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath folderName = createString("folderName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public QWishlistFolder(String variable) {
        this(WishlistFolder.class, forVariable(variable), INITS);
    }

    public QWishlistFolder(Path<? extends WishlistFolder> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWishlistFolder(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWishlistFolder(PathMetadata metadata, PathInits inits) {
        this(WishlistFolder.class, metadata, inits);
    }

    public QWishlistFolder(
        Class<? extends WishlistFolder> type,
        PathMetadata metadata,
        PathInits inits
    ) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
    }

}

