package com.bbangle.bbangle.member.service;

import static com.bbangle.bbangle.image.domain.ImageCategory.MEMBER_PROFILE;

import com.bbangle.bbangle.image.customer.service.ImageService;
import com.bbangle.bbangle.member.domain.Agreement;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.domain.SignatureAgreement;
import com.bbangle.bbangle.member.domain.Withdrawal;
import com.bbangle.bbangle.member.dto.MemberAssignResponse;
import com.bbangle.bbangle.member.dto.MemberInfoRequest;
import com.bbangle.bbangle.member.dto.WithdrawalRequestDto;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.member.repository.SignatureAgreementRepository;
import com.bbangle.bbangle.member.repository.WithdrawalRepository;
import com.bbangle.bbangle.preference.repository.MemberPreferenceRepository;
import com.bbangle.bbangle.survey.repository.SurveyRepository;
import com.bbangle.bbangle.wishlist.dto.FolderRequestDto;
import com.bbangle.bbangle.wishlist.service.WishListBoardService;
import com.bbangle.bbangle.wishlist.service.WishListFolderService;
import com.bbangle.bbangle.wishlist.service.WishListStoreService;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private static final long DEFAULT_MEMBER_ID = 1L;
    private static final String DEFAULT_MEMBER_NAME = "비회원";
    private static final String DEFAULT_MEMBER_NICKNAME = "비회원";
    private static final String DEFAULT_MEMBER_EMAIL = "example@xxxxx.com";
    private static final String DEFAULT_FOLDER_NAME = "기본 폴더";

    private final ImageService imageService;
    private final MemberRepository memberRepository;
    private final SignatureAgreementRepository signatureAgreementRepository;
    private final WishListStoreService wishListStoreServiceImpl;
    private final WishListBoardService wishListBoardService;
    private final WithdrawalRepository withdrawalRepository;
    private final WishListFolderService wishListFolderService;
    private final MemberPreferenceRepository memberPreferenceRepository;
    private final SurveyRepository surveyRepository;

    @PostConstruct
    public void initSetting() {
        memberRepository.findById(DEFAULT_MEMBER_ID)
            .ifPresentOrElse(
                member -> log.info("Default member already exists"),
                () -> {
                    memberRepository.save(Member.builder()
                        .id(DEFAULT_MEMBER_ID)
                        .name(DEFAULT_MEMBER_NAME)
                        .nickname(DEFAULT_MEMBER_NICKNAME)
                        .email(DEFAULT_MEMBER_EMAIL)
                        .build());
                    log.info("Default member created");
                }
            );
    }

    public Member findById(Long id) {
        return memberRepository.findMemberById(id);
    }

    @Transactional
    public void updateMemberInfo(
        MemberInfoRequest request,
        Long memberId,
        MultipartFile profileImg
    ) {
        Member loginedMember = findById(memberId);
        if (profileImg != null && !profileImg.isEmpty()) {
            String imagePath = imageService.save(MEMBER_PROFILE, profileImg, loginedMember.getId());
            loginedMember.updateProfile(imagePath);
        }
        loginedMember.updateFirst(request);

        checkingConsent(request);
        saveConsent(request, loginedMember);
    }

    private void saveConsent(MemberInfoRequest request, Member member) {
        List<SignatureAgreement> agreementList = new ArrayList<>();
        SignatureAgreement marketingAgreement = SignatureAgreement.builder()
            .member(member)
            .name(Agreement.ALLOWING_MARKETING)
            .agreementStatus(request.isAllowingMarketing())
            .dateOfSignature(LocalDateTime.now())
            .build();
        agreementList.add(marketingAgreement);
        SignatureAgreement serviceAgreement = SignatureAgreement.builder()
            .member(member)
            .name(Agreement.TERMS_OF_SERVICE)
            .agreementStatus(request.isTermsOfServiceAccepted())
            .dateOfSignature(LocalDateTime.now())
            .build();
        agreementList.add(serviceAgreement);
        SignatureAgreement personalInfoAgreement = SignatureAgreement.builder()
            .member(member)
            .name(Agreement.PERSONAL_INFO)
            .agreementStatus(request.isPersonalInfoConsented())
            .dateOfSignature(LocalDateTime.now())
            .build();
        agreementList.add(personalInfoAgreement);
        signatureAgreementRepository.saveAll(agreementList);
    }

    private void checkingConsent(MemberInfoRequest request) {
        if (!request.isPersonalInfoConsented()) {
            throw new IllegalArgumentException("개인 정보 동의는 필수입니다.");
        }
        if (!request.isTermsOfServiceAccepted()) {
            throw new IllegalArgumentException("서비스 이용 동의는 필수입니다.");
        }
    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = findById(memberId);
        member.delete();
        wishListStoreServiceImpl.deletedByDeletedMember(memberId);
        wishListBoardService.deletedByDeletedMember(memberId);
        wishListFolderService.deletedByDeletedMember(memberId);
    }

    @Transactional
    public void saveDeleteReason(WithdrawalRequestDto withdrawalRequestDto, Long memberId) {
        Member member = memberRepository.findMemberById(memberId);
        String[] reasons = withdrawalRequestDto.getReasons().split(",");
        for (String reason : reasons) {
            withdrawalRepository.save(Withdrawal.builder()
                .reason(reason)
                .member(member)
                .build());
        }
    }

    public Member getFirstJoinedMember(Member oauthMember) {
        Member newMember = memberRepository.save(oauthMember);
        Long newMemberId = newMember.getId();
        wishListFolderService.create(newMemberId, new FolderRequestDto(DEFAULT_FOLDER_NAME));
        return newMember;
    }

    @Transactional(readOnly = true)
    public MemberAssignResponse getIsAssigned(Long memberId) {

        boolean isFullyAssigned = signatureAgreementRepository.existsByMemberId(memberId);
        boolean isPreferenceAssigned = memberPreferenceRepository.existsByMemberId(memberId);
        boolean isSurveyed = surveyRepository.existsByMemberId(memberId);

        return new MemberAssignResponse(isFullyAssigned, isPreferenceAssigned, isSurveyed);
    }

}
