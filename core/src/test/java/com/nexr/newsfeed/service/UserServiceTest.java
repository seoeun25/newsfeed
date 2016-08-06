package com.nexr.newsfeed.service;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.entity.User;
import com.nexr.newsfeed.jpa.JPAService;
import com.nexr.newsfeed.server.NewsfeedServer;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserServiceTest {

    private static Logger log = LoggerFactory.getLogger(UserServiceTest.class);

    private static UserService userService;
    private static JPAService jpaService;

    @BeforeClass
    public static void setupClass() {
        try {
            System.setProperty("persistenceUnit", "newsfeed-test-hsql");
            Injector injector = Guice.createInjector(new NewsfeedServer());

            Thread.sleep(1000);
            userService = injector.getInstance(UserService.class);
            jpaService = injector.getInstance(JPAService.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDown() {
        if (jpaService != null) {
            jpaService.shutdown();
        }
    }

    @Test
    public void testCreateUser() {
        try {
            userService.createUser("hello@email.com", "hello");
            userService.createUser("abc@email.com", "az");

        } catch (NewsfeedException e) {
            Assert.fail();
        }

        try {
            userService.createUser("hello@email.com", "hello");
            Assert.fail();
        } catch (NewsfeedException e) {
            // duplicated email
        }

    }

    @Test
    public void testUpdateViewTime() {
        try {
            List<User> userList = userService.getUsers();
            User user = new User("nobody@email.com", "nobody");
            user.setId(userList.size() + 10);
            User userRe = userService.updateLastviewTime(user.getId(), System.currentTimeMillis());
            log.info("----- userRe : " + userRe);
            Assert.fail("Can not update the unregistered user");
        }catch (NewsfeedException e) {
            Assert.assertTrue(e.getMessage().contains("Not Found"));
        }

        try {
            User user = userService.createUser("abcde@email.com", "abcde");
            User userRe = userService.updateLastviewTime(user.getId(), 0);
            Assert.assertEquals(user.getId(), userRe.getId());
            Assert.assertEquals(0, userService.getUser(user.getId()).getLastviewTime().getTime());
        } catch (NewsfeedException e) {
            Assert.fail("Fail to update");
        }
    }




}
