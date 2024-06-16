package com.bbangle.bbangle.member.repository;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.member.domain.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProfileRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    ProfileRepository profileRepository;


    @BeforeEach
    void setUp(){
        createDummyMember();
    }

    @AfterEach
    void tearDown() {
        profileRepository.deleteAll();
    }


    @Test
    @DisplayName("중복된 닉네임이 있는 지 확인한다")
    void isDuplicatedNickname() {
        //given
        String nickname = "윤동석";

        //when
        Member member = profileRepository.findByNickname(nickname).get();

        //then
        Assertions.assertThat(member.getNickname()).isEqualTo(nickname);

    }

    private void createDummyMember(){
        Member newMember = Member.builder()
                .nickname("윤동석")
                .build();
        profileRepository.save(newMember);
    }
}

