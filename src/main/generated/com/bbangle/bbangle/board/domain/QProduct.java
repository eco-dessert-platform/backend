package com.bbangle.bbangle.board.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProduct is a Querydsl query type for Product
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProduct extends EntityPathBase<Product> {

    private static final long serialVersionUID = -1645098000L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProduct product = new QProduct("product");

    public final QBoard board;

    public final EnumPath<Category> category = createEnum("category", Category.class);

    public final BooleanPath glutenFreeTag = createBoolean("glutenFreeTag");

    public final BooleanPath highProteinTag = createBoolean("highProteinTag");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath ketogenicTag = createBoolean("ketogenicTag");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final BooleanPath sugarFreeTag = createBoolean("sugarFreeTag");

    public final StringPath title = createString("title");

    public final BooleanPath veganTag = createBoolean("veganTag");

    public QProduct(String variable) {
        this(Product.class, forVariable(variable), INITS);
    }

    public QProduct(Path<? extends Product> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProduct(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProduct(PathMetadata metadata, PathInits inits) {
        this(Product.class, metadata, inits);
    }

    public QProduct(Class<? extends Product> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.board = inits.isInitialized("board") ? new QBoard(forProperty("board"), inits.get("board")) : null;
    }

}

