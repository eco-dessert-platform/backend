package com.bbangle.bbangle.testutil.model;

import com.bbangle.bbangle.store.domain.Store;

public class TestStore extends TestModel<Store>{
    String identifier = "000000";
    String name = "StoreName";
    String profile = "/test-image.jpg";
    String introduce = "테스트 가게 입니다";

    public TestStore setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public TestStore setName(String name) {
        this.name = name;
        return this;
    }

    public TestStore setProfile(String profile) {
        this.profile = profile;
        return this;
    }

    public TestStore setIntroduce(String introduce) {
        this.introduce = introduce;
        return this;
    }

    @Override
    public Store getModel(){
        return Store.builder()
                .identifier(identifier)
                .name(name)
                .profile(profile)
                .introduce(introduce)
                .build();
    }
}
