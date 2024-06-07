package com.bbangle.bbangle.wishlist.service;

import static org.assertj.core.api.Assertions.*;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.fixture.WishlistFolderFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.ranking.domain.BoardStatistic;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.dto.FolderRequestDto;
import com.bbangle.bbangle.wishlist.dto.FolderResponseDto;
import com.bbangle.bbangle.wishlist.dto.FolderUpdateDto;
import com.bbangle.bbangle.wishlist.dto.WishListBoardRequest;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import java.util.List;
import net.datafaker.Faker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

class WishListFolderServiceTest extends AbstractIntegrationTest {

    private static final Faker faker = new Faker();
    private static final String DEFAULT_FOLDER_NAME = "기본 폴더";

    @Autowired
    WishListFolderService wishListFolderService;

    @Autowired
    WishListBoardRepository wishlistBoardRepository;

    Member member;

    @BeforeEach
    void setup() {
        member = MemberFixture.createKakaoMember();
        member = memberService.getFirstJoinedMember(member);
    }

    @Nested
    @DisplayName("위시리스트 폴더 생성 서비스 로직 테스트")
    class CreatWishListFolder {

        @Test
        @DisplayName("처음 가입한 멤버는 기본 폴더 하나만을 가지고 있다")
        void memberWithFirstJoinedWishlistFolder() {
            //given, when
            List<FolderResponseDto> folderList = wishListFolderService.getList(member.getId());

            //then
            assertThat(folderList).hasSize(1);
            FolderResponseDto basicFolder = folderList.get(0);
            assertThat(basicFolder.title()).isEqualTo("기본 폴더");
        }

        @Test
        @DisplayName("정상적인 이름의 위시리스트 폴더 생성을 요청하면 새로운 폴더가 만들어진다")
        void memberCreateNewFolder() {
            //given, when
            String title = faker.book()
                .title();
            if (title.length() > 12) {
                title = title.substring(0, 12);
            }
            FolderRequestDto folderRequestDto = new FolderRequestDto(title);
            wishListFolderService.create(member.getId(), folderRequestDto);

            List<FolderResponseDto> folderList = wishListFolderService.getList(member.getId());

            //then
            assertThat(folderList).hasSize(2);
            FolderResponseDto basicFolder = folderList.get(1);
            assertThat(basicFolder.title()).isEqualTo(title);
        }


        @ParameterizedTest
        @DisplayName("비정상적인 제목의 폴더는 만들 수 없다")
        @ValueSource(strings = {" ", "aaaaaaaaaaaaaaaaaaaaa"})
        @NullAndEmptySource
        void cannotCrateFolderWithInvalidTitle(String title) {
            //given, when, then
            assertThatThrownBy(() -> new FolderRequestDto(title))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.INVALID_FOLDER_TITLE.getMessage());
        }

        @Test
        @DisplayName("이미 이 유저가 만든 폴더의 제목과 동일한 제목의 폴더를 만드는 경우 예외가 발생한다")
        void memberCreateNewFolderWithFolderNameAlreadyExist() {
            //given
            String title = faker.book()
                .title();
            if (title.length() > 12) {
                title = title.substring(0, 12);
            }
            FolderRequestDto folderRequestDto = new FolderRequestDto(title);
            wishListFolderService.create(member.getId(), folderRequestDto);

            //when, then
            assertThatThrownBy(() -> wishListFolderService.create(member.getId(), folderRequestDto))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.FOLDER_NAME_ALREADY_EXIST.getMessage());
        }

        @Test
        @DisplayName("다른 유저가 만든 폴더와 같은 이름의 폴더를 만들더라도 정상적으로 만들어진다")
        void createFolderWithSameTitleWithOthers() {
            //given
            String title = faker.book()
                .title();
            if (title.length() > 12) {
                title = title.substring(0, 12);
            }
            FolderRequestDto folderRequestDto = new FolderRequestDto(title);
            wishListFolderService.create(member.getId(), folderRequestDto);

            Member kakaoMember = MemberFixture.createKakaoMember();
            Member firstJoinedMember = memberService.getFirstJoinedMember(kakaoMember);

            //when, then
            Assertions.assertDoesNotThrow(
                () -> wishListFolderService.create(firstJoinedMember.getId(), folderRequestDto));
        }

        @Test
        @DisplayName("10개의 wishList 폴더를 가지고 있는 경우 더이상 폴더를 만들 수 없다.")
        void cannotCreateFolderMoreThan10() {
            //given
            String title = faker.book()
                .title();
            if (title.length() > 12) {
                title = title.substring(0, 11);
            }
            for (int i = 0; i < 9; i++) {
                FolderRequestDto folderRequestDto = new FolderRequestDto(title + i);
                wishListFolderService.create(member.getId(), folderRequestDto);
            }

            FolderRequestDto folderRequestDto = new FolderRequestDto(title);

            //when, then
            assertThatThrownBy(() -> wishListFolderService.create(member.getId(), folderRequestDto))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.OVER_MAX_FOLDER.getMessage());

        }

    }

    @Nested
    @DisplayName("위시리스트 폴더 이름 업데이트 서비스 로직 테스트")
    class UpdateWishListFolder {

        String beforeTitle;
        Long beforeFolderId;

        @BeforeEach
        void setup() {
            beforeTitle = faker.book()
                .title();
            if (beforeTitle.length() > 12) {
                beforeTitle = beforeTitle.substring(0, 12);
            }
            Long memberId = member.getId();
            FolderRequestDto folderUpdateDto = new FolderRequestDto(beforeTitle);
            beforeFolderId = wishListFolderService.create(memberId, folderUpdateDto);
        }

        @Test
        @DisplayName("정상적으로 폴더 이름 변경에 성공한다")
        void updateFolderName() {
            //given
            String newFolderName = faker.name()
                .name();
            if (newFolderName.length() > 12) {
                newFolderName = newFolderName.substring(0, 12);
            }
            FolderUpdateDto folderUpdateDto = new FolderUpdateDto(newFolderName);

            //when
            wishListFolderService.update(member.getId(), beforeFolderId, folderUpdateDto);

            //then
            WishListFolder changedFolder = wishListFolderRepository.findById(beforeFolderId)
                .get();
            assertThat(changedFolder.getFolderName()).isEqualTo(newFolderName);
        }

        @ParameterizedTest
        @DisplayName("비정상적인 제목의 폴더로 변경할 수 없다")
        @ValueSource(strings = {" ", "aaaaaaaaaaaaaaaaaaaaa"})
        @NullAndEmptySource
        void cannotUpdateFolderNameWithInvalidTitle(String invalidTitle) {
            //given, when, then
            assertThatThrownBy(() -> new FolderUpdateDto(invalidTitle))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.INVALID_FOLDER_TITLE.getMessage());
        }

        @Test
        @DisplayName("존재하지 않는 memberId로 조회할 수 없다")
        void cannotFindFolderWithAnonymousUser() {
            //given
            String newFolderName = faker.name()
                .name();
            if (newFolderName.length() > 12) {
                newFolderName = newFolderName.substring(0, 12);
            }
            FolderUpdateDto folderUpdateDto = new FolderUpdateDto(newFolderName);

            //when, then
            assertThatThrownBy(
                () -> wishListFolderService.update(-1L, beforeFolderId, folderUpdateDto))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.NOTFOUND_MEMBER.getMessage());
        }

        @Test
        @DisplayName("존재하지 않는 folderId로 조회할 수 없다")
        void cannotFindFolderWithInvalidFolderId() {
            //given
            String newFolderName = faker.name()
                .name();
            if (newFolderName.length() > 12) {
                newFolderName = newFolderName.substring(0, 12);
            }
            FolderUpdateDto folderUpdateDto = new FolderUpdateDto(newFolderName);

            //when, then
            assertThatThrownBy(
                () -> wishListFolderService.update(member.getId(), -1L, folderUpdateDto))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.FOLDER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("기본 폴더 이름은 변경할 수 없다.")
        void cannotChangeDefaultFolder() {
            //given
            String newFolderName = faker.name()
                .name();
            if (newFolderName.length() > 12) {
                newFolderName = newFolderName.substring(0, 12);
            }
            FolderUpdateDto folderUpdateDto = new FolderUpdateDto(newFolderName);

            //when, then
            FolderResponseDto folderResponseDto = wishListFolderService.getList(member.getId())
                .get(0);

            assertThatThrownBy(
                () -> wishListFolderService.update(member.getId(), folderResponseDto.folderId(),
                    folderUpdateDto))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.DEFAULT_FOLDER_NAME_CANNOT_CHNAGE.getMessage());
        }

    }

    @Nested
    @DisplayName("위시리스트 폴더 조회 서비스 로직 테스트")
    class FolderList {

        WishListFolder wishlistFolder;
        @BeforeEach
        void setup(){
            wishlistFolder = WishlistFolderFixture.createWishlistFolder(member);
            wishlistFolder = wishListFolderRepository.save(wishlistFolder);
        }

        @Test
        @DisplayName("위시리스트 폴더를 정상적으로 조회한다.")
        void getWishlistFolder() {
            //given, when
            List<FolderResponseDto> folderResponseDtoList = wishListFolderService.getList(member.getId());
            List<String> folderTitleList = folderResponseDtoList.stream()
                .map(FolderResponseDto::title)
                .toList();

            //then
            assertThat(folderTitleList).contains(wishlistFolder.getFolderName(), DEFAULT_FOLDER_NAME);
            assertThat(folderTitleList.get(0)).isEqualTo(DEFAULT_FOLDER_NAME);
        }

        @Test
        @DisplayName("존재하지 않는 멤버의 아이디로 조회하는 경우 예외가 발생한다")
        void getWishlistFolderWithAnonymousMember() {
            //given, when, then
            assertThatThrownBy(() -> wishListFolderService.getList(member.getId() + 1L))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.NOTFOUND_MEMBER.getMessage());
        }

        @Test
        @DisplayName("저장된 게시글의 개수만큼 이미지 썸네일을 보여주지만 네 개 이상인 경우 네 개만 보여준다.")
        void listContainsThumbnailWithFourMaxCount() {
            //given, when
            List<FolderResponseDto> folderResponseDtoList = wishListFolderService.getList(member.getId());
            FolderResponseDto defaultFolder = folderResponseDtoList.stream()
                .filter(folder -> folder.title()
                    .equals(DEFAULT_FOLDER_NAME))
                .findFirst()
                .get();
            Store store = StoreFixture.storeGenerator();
            storeRepository.save(store);
            for(int i = 0; i < 10; i++){
                Board board = BoardFixture.randomBoard(store);
                board = boardRepository.save(board);
                BoardStatistic boardStatistic = BoardStatistic.builder().board(board).popularScore(0.0).recommendScore(0.0).build();
                rankingRepository.save(boardStatistic);

                if(i < 3) {
                    wishListBoardService.wish(member.getId(), board.getId(),
                        new WishListBoardRequest(defaultFolder.folderId()));
                }
                if(i >= 3) {
                    wishListBoardService.wish(member.getId(), board.getId(),
                        new WishListBoardRequest(wishlistFolder.getId()));
                }
            }

            // then
            List<FolderResponseDto> afterWishFolderList = wishListFolderService.getList(member.getId());
            FolderResponseDto afterWishDefaultFolder = afterWishFolderList.stream()
                .filter(folderResponseDto -> folderResponseDto.title()
                    .equals(DEFAULT_FOLDER_NAME))
                .findFirst()
                .get();
            FolderResponseDto afterWishCreatedFolder = afterWishFolderList.stream()
                .filter(folderResponseDto -> !folderResponseDto.title()
                    .equals(DEFAULT_FOLDER_NAME))
                .findFirst()
                .get();

            assertThat(afterWishDefaultFolder.productImages()).hasSize(3);
            assertThat(afterWishCreatedFolder.productImages()).hasSize(4);
        }
    }

    @Nested
    @DisplayName("위시리스트 폴더 삭제 서비스 로직 테스트")
    class DeleteFolder{
        WishListFolder wishListFolder;
        @BeforeEach
        void setup(){
            wishListFolder = WishlistFolderFixture.createWishlistFolder(member);
            wishListFolder = wishListFolderRepository.save(wishListFolder);
        }

        @Test
        @DisplayName("정상적으로 위시리스트 폴더를 삭제한다")
        void deleteWishListFolder() {
            //given, when
            wishListFolderService.delete(wishListFolder.getId(), member.getId());

            //then
            List<FolderResponseDto> wishListFolderList = wishListFolderService.getList(member.getId());
            assertThat(wishListFolderList).hasSize(1);
        }

        @Test
        @DisplayName("이미 삭제된 위시리스트 폴더는 다시 삭제할 수 없다.")
        void cannotDeleteAlreadyDeletedFolder() {
            //given, when
            wishListFolderService.delete(wishListFolder.getId(), member.getId());

            //then
            assertThatThrownBy(() ->wishListFolderService.delete(wishListFolder.getId(), member.getId()))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.FOLDER_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("기볼 폴더는 삭제할 수 없다.")
        void cannotDeleteDefaultFolder() {
            //given, when
            FolderResponseDto DefaultFolder = wishListFolderService.getList(member.getId()).stream().filter(folder -> folder.title().equals(DEFAULT_FOLDER_NAME))
                .findFirst()
                .get();

            //then
            assertThatThrownBy(() ->wishListFolderService.delete(DefaultFolder.folderId(), member.getId()))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.CANNOT_DELETE_DEFAULT_FOLDER.getMessage());
        }
    }

}
