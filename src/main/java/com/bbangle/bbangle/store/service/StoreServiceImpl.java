package com.bbangle.bbangle.store.service;

import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;

import com.bbangle.bbangle.board.dto.StoreAllBoardDto;

import com.bbangle.bbangle.page.StoreCustomPage;
import com.bbangle.bbangle.store.dto.StoreDetailResponseDto;
import com.bbangle.bbangle.store.dto.StoreResponseDto;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.util.SecurityUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;

    @Override
    public StoreDetailResponseDto getStoreDetailResponse(Long memberId, Long storeId) {
        return memberId > 1L ?
            storeRepository.getStoreDetailResponseDtoWithLike(memberId, storeId) :
            storeRepository.getStoreDetailResponseDto(storeId);

    }

    @Override
    public SliceImpl<StoreAllBoardDto> getAllBoard(int page, Long memberId, Long storeId) {
        int PAGE_SIZE = 10;

        return memberId > 1L ?
            storeRepository.getAllBoardWithLike(PageRequest.of(page, PAGE_SIZE), memberId, storeId) :
                storeRepository.getAllBoard(PageRequest.of(page, PAGE_SIZE), storeId);
    }

    @Override
    public StoreCustomPage<List<StoreResponseDto>> getList(Long cursorId) {
        if(!SecurityUtils.isLogin()){
            return storeRepository.findNextCursorPageWithoutLogin(cursorId);
        }

        Long memberId = SecurityUtils.getMemberId();
        Member member = memberRepository.findById(memberId)
            .orElseThrow(BbangleException::new);
        return storeRepository.findNextCursorPageWithLogin(cursorId, member);
    }

}
