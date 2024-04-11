package com.bbangle.bbangle.testutil;

import com.bbangle.bbangle.board.repository.BoardImgRepository;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.search.repository.SearchRepository;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.testutil.factory.*;
import com.bbangle.bbangle.wishListBoard.repository.WishListProductRepository;
import com.bbangle.bbangle.wishListFolder.repository.WishListFolderRepository;
import com.bbangle.bbangle.wishListStore.repository.WishListStoreRepository;
import jakarta.persistence.EntityManager;

public class TestFactoryManager {
    private final EntityManager entityManager;
    private TestStoreFactory testStoreFactory;
    private TestBoardFactory testBoardFactory;
    private TestProductFactory testProductFactory;
    private TestBoardImageFactory testBoardImageFactory;
    private TestMemberFactory testMemberFactory;
    private TestWishlistFolderFactory testWishlistFolderFactory;
    private TestWishlistBoardFactory testWishlistBoardFactory;
    private TestWishlistStoreFactory testWishlistStoreFactory;
    private TestSearchFactory testSearchFactory;

    public TestFactoryManager(EntityManager entityManager){
        this.entityManager = entityManager;
    }

    public TestStoreFactory getTestStoreFactory(){
        return testStoreFactory != null ? testStoreFactory : null;
    }

    public TestFactoryManager setTestStoreFactory(StoreRepository repository) {
        this.testStoreFactory = new TestStoreFactory(entityManager, repository);

        return this;
    }

    public TestBoardFactory getTestBoardFactory(){
        return testBoardFactory != null ? testBoardFactory : null;
    }

    public TestFactoryManager setTestBoardFactory(BoardRepository repository) {
        this.testBoardFactory = new TestBoardFactory(entityManager, repository);

        return this;
    }

    public TestProductFactory getTestProductFactory(){
        return testProductFactory != null ? testProductFactory : null;
    }

    public TestFactoryManager setTestProductFactory(ProductRepository repository) {
        this.testProductFactory = new TestProductFactory(entityManager, repository);

        return this;
    }

    public TestBoardImageFactory getTestBoardImageFactory(){
        return testBoardImageFactory != null ? testBoardImageFactory : null;
    }

    public TestFactoryManager setTestBoardImageFactory(BoardImgRepository repository) {
        this.testBoardImageFactory = new TestBoardImageFactory(entityManager, repository);

        return this;
    }

    public TestMemberFactory getTestMemberFactory(){
        return testMemberFactory != null ? testMemberFactory : null;
    }

    public TestFactoryManager setTestMemberFactory(MemberRepository repository) {
        this.testMemberFactory = new TestMemberFactory(entityManager, repository);

        return this;
    }

    public TestWishlistFolderFactory getTestWishlistFolderFactory(){
        return testWishlistFolderFactory != null ? testWishlistFolderFactory : null;
    }

    public TestFactoryManager setTestWishlistFolderFactory(WishListFolderRepository repository) {
        this.testWishlistFolderFactory = new TestWishlistFolderFactory(entityManager, repository);

        return this;
    }

    public TestWishlistBoardFactory getTestWishlistBoardFactory(){
        return testWishlistBoardFactory != null ? testWishlistBoardFactory : null;
    }

    public TestFactoryManager setTestWishlistBoardFactory(WishListProductRepository repository) {
        this.testWishlistBoardFactory = new TestWishlistBoardFactory(entityManager, repository);

        return this;
    }

    public TestWishlistStoreFactory getTestWishlistStoreFactory(){
        return testWishlistStoreFactory != null ? testWishlistStoreFactory : null;
    }

    public TestFactoryManager setTestWishlistStoreFactory(WishListStoreRepository repository) {
        this.testWishlistStoreFactory = new TestWishlistStoreFactory(entityManager, repository);

        return this;
    }

    public TestSearchFactory getTestSearchFactory(){
        return testSearchFactory != null ? testSearchFactory : null;
    }

    public TestFactoryManager setTestSearchFactory(SearchRepository repository) {
        this.testSearchFactory = new TestSearchFactory(entityManager, repository);

        return this;
    }


    public void resetAutoIncreasementAndRowData(){
        if (testSearchFactory != null){
            testSearchFactory.getRepository().deleteAll();
            startAutoIncreasement("ALTER TABLE search AUTO_INCREMENT = 1");
            testSearchFactory.resetEntityMap();
        }

        if (testWishlistStoreFactory != null){
            testWishlistStoreFactory.getRepository().deleteAll();
            startAutoIncreasement("ALTER TABLE wishlist_store AUTO_INCREMENT = 1");
            testWishlistStoreFactory.resetEntityMap();
        }

        if (testWishlistBoardFactory != null){
            testWishlistBoardFactory.getRepository().deleteAll();
            startAutoIncreasement("ALTER TABLE wishlist_product AUTO_INCREMENT = 1");
            testWishlistBoardFactory.resetEntityMap();
        }

        if (testWishlistFolderFactory != null){
            testWishlistFolderFactory.getRepository().deleteAll();
            startAutoIncreasement("ALTER TABLE wishlist_folder AUTO_INCREMENT = 1");
            testWishlistBoardFactory.resetEntityMap();
        }

        if (testMemberFactory != null){
            testMemberFactory.getRepository().deleteAll();
            startAutoIncreasement("ALTER TABLE member AUTO_INCREMENT = 2");
            testMemberFactory.resetEntityMap();
        }

        if (testBoardImageFactory != null){
            testBoardImageFactory.getRepository().deleteAll();
            startAutoIncreasement("ALTER TABLE product_img AUTO_INCREMENT = 1");
            testBoardImageFactory.resetEntityMap();
        }

        if (testProductFactory != null){
            testProductFactory.getRepository().deleteAll();
            startAutoIncreasement("ALTER TABLE product AUTO_INCREMENT = 1");
            testProductFactory.resetEntityMap();
        }

        if (testBoardFactory != null){
            testBoardFactory.getRepository().deleteAll();
            startAutoIncreasement("ALTER TABLE product_board AUTO_INCREMENT = 1");
            testBoardFactory.resetEntityMap();
        }

        if (testStoreFactory != null){
            testStoreFactory.getRepository().deleteAll();
            startAutoIncreasement("ALTER TABLE store AUTO_INCREMENT = 1");
            testStoreFactory.resetEntityMap();
        }
    }

    private void startAutoIncreasement(String query){
        entityManager
                .createNativeQuery(query)
                .executeUpdate();
    }
}
