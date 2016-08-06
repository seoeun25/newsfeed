package com.nexr.newsfeed.jpa;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.entity.Friend;
import com.nexr.newsfeed.server.NewsfeedServer;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FriendQueryExecutorTest {

    private static Logger log = LoggerFactory.getLogger(FriendQueryExecutorTest.class);

    private static FriendQueryExceutor queryExecutor;
    private static JPAService jpaService;

    @BeforeClass
    public static void setupClass() {
        try {
            System.setProperty("persistenceUnit", "newsfeed-test-hsql");
            Injector injector = Guice.createInjector(new NewsfeedServer());

            Thread.sleep(1000);
            queryExecutor = injector.getInstance(FriendQueryExceutor.class);
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
    public void testInsertAndGet() {

        try {

            Friend friend1 = new Friend(1, 2);
            queryExecutor.insert(friend1);
            Friend friend2 = new Friend(1, 3);
            queryExecutor.insert(friend2);

            Friend friendRe1 = queryExecutor.get(FriendQueryExceutor.FriendQuery.GET_FOLLOWING, new Object[]{1, 2});
            Assert.assertNotNull(friendRe1);
            Assert.assertEquals(friend1.getUserId(), friendRe1.getUserId());
            Assert.assertEquals(friend1.getFollowingId(), friendRe1.getFollowingId());

            List<Friend> friendList = queryExecutor.getList(FriendQueryExceutor.FriendQuery.GET_FOLLOWINGS, new Object[]{1});
            Assert.assertEquals(2, friendList.size());
            for (Friend friend : friendList) {
                Assert.assertEquals(1, friend.getUserId());
                log.info("friend json : {}", friend.toJson());
                log.info("friend string : {}", friend.toString());
            }

            Friend friend3 = new Friend(2,3);
            queryExecutor.insert(friend3);
            Friend friend4 = new Friend(2,4);
            queryExecutor.insert(friend4);

            List<Friend> list = queryExecutor.getList(FriendQueryExceutor.FriendQuery.GET_FOLLOWING_ALL, new Object[]{}) ;
            Assert.assertEquals(4, list.size());
            for (Friend friend: list) {
                log.info(friend.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetNegative() {
        try {
            Object[] params = new Object[]{-1};
            Friend friend = queryExecutor.get(FriendQueryExceutor.FriendQuery.GET_FOLLOWINGS, params);

            Assert.fail("Should there no Friend");
        } catch (NewsfeedException e) {
            Assert.assertTrue(e.getMessage().contains("Not Found"));
        }
    }

}

