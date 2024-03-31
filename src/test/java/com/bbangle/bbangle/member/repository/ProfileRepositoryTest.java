package com.bbangle.bbangle.member.repository;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.ProfileRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ProfileRepositoryTest {
    @Autowired
    ProfileRepository profileRepository;

    @Test
    @DisplayName("중복된 닉네임이 있는 지 확인한다")
    public void isDuplicatedNickname() throws Exception{
        //given
        String nickname = "윤동석";

        //when
        Member member = profileRepository.findByNickname(nickname).get();

        //then
        Assertions.assertThat(member.getNickname()).isEqualTo(nickname);

    }
}
