package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.board.repository.dao.TagsDao;

public class TagDaoFixture {

    public static TagsDao getGlutenFreeTagDao() {
        return new TagsDao(true,
            false,
            false,
            false,
            false);
    }

    public static TagsDao getProteinHighTagDao() {
        return new TagsDao(false,
            true,
            false,
            false,
            false);
    }

    public static TagsDao getSugarFreeTagDao() {
        return new TagsDao(false,
            false,
            true,
            false,
            false);
    }

    public static TagsDao getVeganTagDao() {
        return new TagsDao(false,
            false,
            false,
            true,
            false);
    }

    public static TagsDao getKetogenicTagDao() {
        return new TagsDao(false,
            false,
            false,
            false,
            true);
    }

    public static TagsDao getGlutenFreeTagAndHighProteinTagDao() {
        return new TagsDao(true,
            true,
            false,
            false,
            false);
    }

    public static TagsDao getSugarFreeAndVeganTagDao() {
        return new TagsDao(false,
            false,
            true,
            true,
            false);
    }

}
