package com.bbangle.bbangle.member.repository;

import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.domain.QMember;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

import static com.bbangle.bbangle.exception.BbangleErrorCode.NOTFOUND_MEMBER;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberQueryDSLRepository{

    private static final QMember member = QMember.member;
    private final JPAQueryFactory queryFactory;


    @Override
    public Member findMemberById(Long memberId) {
        if(memberId == null){
            throw new BbangleException(NOTFOUND_MEMBER);
        }

        return Optional.ofNullable(queryFactory.selectFrom(member)
                        .where(member.id.eq(memberId))
                        .fetchOne())
                .orElseThrow(() -> new BbangleException(NOTFOUND_MEMBER));
    }

    @Override
    public Long countMembers() {
        return queryFactory.select(member.id.count())
                .from(member)
                .where(member.isDeleted.isFalse())
                .fetchOne();
    }

    @Override
    public Long countMembersCreatedBetweenPeriod(LocalDate startLocalDate, LocalDate endLocalDate) {
        DateTemplate<Date> createdAt = getCreatedAtDate();
        Date startDate = Date.valueOf(startLocalDate);
        Date endDate = Date.valueOf(endLocalDate);

        return queryFactory.select(member.id.count())
                .from(member)
                .where(member.isDeleted.isFalse()
                        .and(createdAt.between(startDate, endDate)))
                .fetchOne();
    }


    private static DateTemplate<Date> getCreatedAtDate() {
        return Expressions.dateTemplate(Date.class, "DATE({0})", member.createdAt);
    }

}
