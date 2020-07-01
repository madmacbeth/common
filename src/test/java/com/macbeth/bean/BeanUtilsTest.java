package com.macbeth.bean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BeanUtilsTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void copyProperties() {

        User user = new User();
        user.setAInteger(1);
        user.setALong(2L);
        user.setAString("chenwei");
        User subUser = new User();
        subUser.setAString("sub user");
        subUser.setALong(1L);
        subUser.setAInteger(2);
        user.setSubUser(subUser);
        UserVo userVo = new UserVo();
        userVo.setAString("macbeth");
        BeanUtils.copyPropertiesWithComplexProperty(user, userVo, true);
        System.out.println(userVo);
    }

    @Test
    public void copyPropertiesWithComplexProperty() {
        User user = new User();
        BeanUtils.copyPropertiesWithComplexProperty(user, null, false);
    }
}