package com.bbangle.bbangle.member.repository;

import static com.bbangle.bbangle.exception.BbangleErrorCode.NOTFOUND_MEMBER;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.domain.QMember;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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

}
