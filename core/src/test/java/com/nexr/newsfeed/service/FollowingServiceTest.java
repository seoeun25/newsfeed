package com.nexr.newsfeed.service;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.entity.Friend;
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

public class FollowingServiceTest {

    private static Logger log = LoggerFactory.getLogger(FollowingServiceTest.class);

    private static FollowingService followingService;
    private static UserService userService;
    private static JPAService jpaService;


    @BeforeClass
    public static void setupClass() {
        try {
            System.setProperty("persistenceUnit", "newsfeed-test-hsql");
            Injector injector = Guice.createInjector(new NewsfeedServer());

            Thread.sleep(1000);
            followingService = injector.getInstance(FollowingService.class);
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
    public void testFollowing() {
        try {
            followingService.follow(100, 200);
            Assert.fail("Can not follow not registered user");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("User Not Found"));
        }
        try {
            User user1 = userService.createUser("hello.az@email.com", "hello.az");
            User user2 = userService.createUser("hello.seoeun@emil.com","hello.seoeun");
            User user3 = userService.createUser("hi.az@email.com", "hi.az");

            long userId = user1.getId();

            Friend friend1 = followingService.follow(userId, user2.getId());
            Assert.assertNotNull(friend1);
            Friend friend2 =followingService.follow(userId, user2.getId());
            Assert.assertNotNull(friend2);
            Friend friend3 =followingService.follow(userId, user3.getId());
            Assert.assertNotNull(friend3);

            // remove duplicated
            List<Long> list = followingService.getFollowings(userId);
            Assert.assertEquals(2, list.size());
        } catch (NewsfeedException e) {
            e.printStackTrace();
            Assert.fail();
        }

    }


}
