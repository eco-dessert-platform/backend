package com.bbangle.bbangle.preference.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.preference.domain.MemberPreference;
import com.bbangle.bbangle.preference.domain.Preference;
import com.bbangle.bbangle.preference.dto.MemberPreferenceResponse;
import com.bbangle.bbangle.preference.dto.PreferenceSelectRequest;
import com.bbangle.bbangle.preference.dto.PreferenceUpdateRequest;
import com.bbangle.bbangle.preference.repository.MemberPreferenceRepository;
import com.bbangle.bbangle.preference.repository.PreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PreferenceService {

    private final PreferenceRepository preferenceRepository;
    private final MemberRepository memberRepository;
    private final MemberPreferenceRepository memberPreferenceRepository;

    @Transactional
    public void register(PreferenceSelectRequest request, Long memberId) {
        Preference preference = preferenceRepository.findByPreferenceType(request.preferenceType())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.PREFERENCE_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOTFOUND_MEMBER));

        if (memberPreferenceRepository.existsByMember(member)) {
            throw new BbangleException(BbangleErrorCode.PREFERENCE_ALREADY_ASSIGNED);
        }

        MemberPreference memberPreference = MemberPreference.builder()
            .member(member)
            .preference(preference)
            .build();

        memberPreferenceRepository.save(memberPreference);
    }

    @Transactional
    public void update(PreferenceUpdateRequest request, Long memberId) {
        Preference preference = preferenceRepository.findByPreferenceType(request.preferenceType())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.PREFERENCE_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOTFOUND_MEMBER));

        MemberPreference memberPreference = memberPreferenceRepository.findByMember(member)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.MEMBER_PREFERENCE_NOT_FOUND));

        memberPreference.updatePreference(preference);
    }

    public MemberPreferenceResponse getPreference(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOTFOUND_MEMBER));

        return null;
    }

}
