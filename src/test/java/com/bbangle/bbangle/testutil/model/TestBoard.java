package com.bbangle.bbangle.testutil.model;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.store.domain.Store;

public class TestBoard extends TestModel<Board>{
    private Store store;
    private String boardName = "테스트 게시물";
    private Integer price = 5400;
    private Boolean status = true;
    private String profile = "/test-profile.jpg";
    private String purchaseUrl = "www.test.com";
    private Integer view = 0;
    private Boolean sunday = false;
    private Boolean monday = false;
    private Boolean tuesday = false;
    private Boolean wednessday = false;
    private Boolean thursday = false;
    private Boolean friday = false;
    private Boolean saturday = false;

    public TestBoard(Store store){
        this.store = store;
    }

    public TestBoard setBoardName(String boardName){
        this.boardName = boardName;

        return this;
    }

    public TestBoard setPrice(Integer price) {
        this.price = price;

        return this;
    }

    public TestBoard setStatus(Boolean status) {
        this.status = status;

        return this;
    }

    public TestBoard setProfile(String profile) {
        this.profile = profile;

        return this;
    }

    public TestBoard setPurchaseUrl(String purchaseUrl) {
        this.purchaseUrl = purchaseUrl;

        return this;
    }

    public TestBoard setView(Integer view) {
        this.view = view;

        return this;
    }

    public TestBoard setSunday(Boolean sunday) {
        this.sunday = sunday;

        return this;
    }

    public TestBoard setMonday(Boolean monday) {
        this.monday = monday;

        return this;
    }

    public TestBoard setTuesday(Boolean tuesday) {
        this.tuesday = tuesday;

        return this;
    }

    public TestBoard setWednessday(Boolean wednessday) {
        this.wednessday = wednessday;

        return this;
    }

    public TestBoard setThursday(Boolean thursday) {
        this.thursday = thursday;

        return this;
    }

    public TestBoard setFriday(Boolean friday) {
        this.friday = friday;

        return this;
    }

    public TestBoard setSaturday(Boolean saturday) {
        this.saturday = saturday;

        return this;
    }

    @Override
    public Board getModel(){
        return Board.builder()
                .store(store)
                .title(boardName)
                .price(price)
                .status(status)
                .profile(profile)
                .purchaseUrl(purchaseUrl)
                .view(view)
                .sunday(sunday)
                .monday(monday)
                .tuesday(tuesday)
                .friday(friday)
                .saturday(saturday)
                .build();
    }
}
