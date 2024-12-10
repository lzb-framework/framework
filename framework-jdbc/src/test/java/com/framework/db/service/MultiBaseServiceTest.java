package com.framework.db.service;

import com.pro.framework.jdbc.service.DBBaseService;
import com.pro.framework.api.database.page.Page;
import com.pro.framework.api.database.page.PageInput;
import com.pro.framework.jdbc.sqlexecutor.DbJdbcAdaptor;

@SuppressWarnings("deprecation")
class MultiBaseServiceTest {
    public static void main(String[] args) {
        save();
        query();
    }

    private static void save() {
        DBBaseService<TestUser> service = new DBBaseService<>(new DbJdbcAdaptor());
        TestUser user = new TestUser();
//        user.setId(1L);
        user.setName("user1");
        service.save(user);
    }

    private static void query() {
        DBBaseService<TestUser> service = new DBBaseService<>(new DbJdbcAdaptor());
        TestUser user = new TestUser();
//        user.setId(1L);
//        user.setName("user1");
//        service.save(user);
        PageInput multiPageInput = new PageInput();
        Page<TestUser> page = service.getPage(user, multiPageInput);
        System.out.println(page);
    }

}
