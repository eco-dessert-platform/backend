package com.bbangle.bbangle.testutil.factory;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;

public class TestMemberFactory extends TestModelFactory<Member, MemberRepository> {
    public TestMemberFactory(EntityManager entityManager, MemberRepository repository) {
        super(entityManager, repository);
    }

    @Override
    protected Member saveEntity(Member member) {
        return repository.save(member);
    }
}
