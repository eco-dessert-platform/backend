package com.bbangle.bbangle.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBoard is a Querydsl query type for Board
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBoard extends EntityPathBase<Board> {

    public static final QBoard board = new QBoard("board");
    private static final long serialVersionUID = -1270237666L;
    private static final PathInits INITS = PathInits.DIRECT2;
    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath detail = createString("detail");

    public final BooleanPath friday = createBoolean("friday");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final BooleanPath monday = createBoolean("monday");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final ListPath<Product, QProduct> productList = this.<Product, QProduct>createList(
        "productList", Product.class, QProduct.class, PathInits.DIRECT2);

    public final StringPath profile = createString("profile");

    public final StringPath purchaseUrl = createString("purchaseUrl");

    public final BooleanPath saturday = createBoolean("saturday");

    public final BooleanPath status = createBoolean("status");

    public final QStore store;

    public final BooleanPath sunday = createBoolean("sunday");

    public final BooleanPath thursday = createBoolean("thursday");

    public final StringPath title = createString("title");

    public final BooleanPath tuesday = createBoolean("tuesday");

    public final NumberPath<Integer> view = createNumber("view", Integer.class);

    public final BooleanPath wednesday = createBoolean("wednesday");

    public final NumberPath<Integer> wishCnt = createNumber("wishCnt", Integer.class);

    public QBoard(String variable) {
        this(Board.class, forVariable(variable), INITS);
    }

    public QBoard(Path<? extends Board> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBoard(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBoard(PathMetadata metadata, PathInits inits) {
        this(Board.class, metadata, inits);
    }

    public QBoard(Class<? extends Board> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.store = inits.isInitialized("store") ? new QStore(forProperty("store")) : null;
    }

}

