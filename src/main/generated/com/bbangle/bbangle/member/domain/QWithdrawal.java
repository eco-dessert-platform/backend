package com.bbangle.bbangle.member.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWithdrawal is a Querydsl query type for Withdrawal
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWithdrawal extends EntityPathBase<Withdrawal> {

    private static final long serialVersionUID = 2142906030L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWithdrawal withdrawal = new QWithdrawal("withdrawal");

    public final com.bbangle.bbangle.common.domain.QBaseEntity _super = new com.bbangle.bbangle.common.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath reason = createString("reason");

    public QWithdrawal(String variable) {
        this(Withdrawal.class, forVariable(variable), INITS);
    }

    public QWithdrawal(Path<? extends Withdrawal> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWithdrawal(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWithdrawal(PathMetadata metadata, PathInits inits) {
        this(Withdrawal.class, metadata, inits);
    }

    public QWithdrawal(Class<? extends Withdrawal> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
    }

}

