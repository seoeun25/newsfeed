package com.nexr.newsfeed.service;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.entity.Friend;
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
    private static JPAService jpaService;

    @BeforeClass
    public static void setupClass() {
        try {
            System.setProperty("persistenceUnit", "newsfeed-test-hsql");
            Injector injector = Guice.createInjector(new NewsfeedServer());

            Thread.sleep(1000);
            followingService = injector.getInstance(FollowingService.class);
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
            long userId = 20;
            Friend friend1 = followingService.follow(userId, 1);
            Assert.assertNotNull(friend1);
            Friend friend2 =followingService.follow(userId, 1);
            Assert.assertNotNull(friend2);
            Friend friend3 =followingService.follow(userId, 2);
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
