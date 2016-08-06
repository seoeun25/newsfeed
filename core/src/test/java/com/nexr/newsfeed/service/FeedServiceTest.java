package com.nexr.newsfeed.service;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.common.Utils;
import com.nexr.newsfeed.entity.Activity;
import com.nexr.newsfeed.entity.User;
import com.nexr.newsfeed.jpa.ActivityQueryExceutor;
import com.nexr.newsfeed.jpa.JPAService;
import com.nexr.newsfeed.server.NewsfeedServer;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;

public class FeedServiceTest {
    private static Logger log = LoggerFactory.getLogger(FeedServiceTest.class);

    private static FeedService feedService;
    private static FollowingService followingService;
    private static JPAService jpaService;
    private static ActivityQueryExceutor activityQueryExceutor;
    private static UserService userService;

    private static long userId = -1;

    @BeforeClass
    public static void setupClass() {
        try {
            System.setProperty("persistenceUnit", "newsfeed-test-hsql");
            Injector injector = Guice.createInjector(new NewsfeedServer());
            jpaService = injector.getInstance(JPAService.class);

            Thread.sleep(1000);
            feedService = injector.getInstance(FeedService.class);
            followingService = injector.getInstance(FollowingService.class);

            activityQueryExceutor = injector.getInstance(ActivityQueryExceutor.class);

            userService = injector.getInstance(UserService.class);

            initData();
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

    public static void initData() throws ParseException {
        try {
            User user = userService.createUser("abc@email.com", "hello");
            log.info("user : " + user.toString());
            userId = user.getId();

            User user1 = userService.createUser("a@email.com", "a");
            User user2 = userService.createUser("b@email.com", "b");
            User user3 = userService.createUser("c@email.com", "c");
            User user4 = userService.createUser("d@email.com", "d");
            User user5 = userService.createUser("e@email.com", "e");


            followingService.follow(user1.getId(), user3.getId());
            followingService.follow(userId, user1.getId());
            followingService.follow(userId, user3.getId());
            followingService.follow(userId, user5.getId());
            followingService.follow(user2.getId(), user5.getId());

            Activity activity1 = new Activity(1, "hello world 11");
            activity1.setCreatedTime(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:01:01")));
            Activity activityRe = feedService.postMessage(activity1);
            Assert.assertNotNull(activityRe);
            Assert.assertEquals(activity1.getUserId(), activityRe.getUserId());
            Assert.assertEquals(activity1.getMessage(), activity1.getMessage());
            long id1 = activityRe.getId();
            log.info("activityId1 : {}", id1);
            Activity activity2 = new Activity(1, "hello world 12");
            activity2.setCreatedTime(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:10:00")));
            feedService.postMessage(activity2);
            Activity activity3 = new Activity(2, "hello world 13");
            activity3.setCreatedTime(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:01:03")));
            feedService.postMessage(activity3);
            Activity activity4 = new Activity(3, "hello world 14");
            activity4.setCreatedTime(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:01:02")));
            feedService.postMessage(activity4);
            Activity activity5 = new Activity(5, "hello world 15");
            activity5.setCreatedTime(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:20:02")));
            feedService.postMessage(activity5);


            User user6 = userService.createUser("hollo@email.com", "hello");
            log.info("user6 : {}", user6);
            log.info("Created data for testing");

        } catch (NewsfeedException e) {
            log.warn("Fail to create initial data", e);
            Assert.fail();
        }
    }

    @Test
    public void testGetFeeds() throws ParseException {
        try {
            Assert.assertTrue(userId >= 0);

            List<Long> following = followingService.getFollowings(userId);
            Assert.assertEquals(3, following.size());

            List<Activity> feeds = feedService.getFeeds(userId, Utils.parseTimeInMillis("2016-07-31 00:01:01"));
            Assert.assertEquals(4, feeds.size());
            Activity previousActivity = null;
            for (int i = 0; i < feeds.size(); i++) {
                Activity activity = feeds.get(i);
                log.info("forward1 desc {} ", activity.toString());
                Assert.assertTrue(activity.getCreatedTime().after(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:00:00"))));
                if (previousActivity != null) {
                    Assert.assertTrue(previousActivity.getCreatedTime().after(activity.getCreatedTime()));
                }
                previousActivity = activity;
            }

            // maxResult = 2
            feeds = feedService.getFeeds(userId, Utils.parseTimeInMillis("2016-07-31 00:00:00"), 2);
            Assert.assertEquals(2, feeds.size());
            previousActivity = null;
            for (int i = 0; i < feeds.size(); i++) {
                Activity activity = feeds.get(i);
                log.info("forward2 desc {} ", activity.toString());
                Assert.assertTrue(activity.getCreatedTime().after(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:00:00"))));
                if (previousActivity != null) {
                    Assert.assertTrue(previousActivity.getCreatedTime().after(activity.getCreatedTime()));
                }
                previousActivity = activity;
            }

            feeds = feedService.getFeeds(userId, Utils.parseTimeInMillis("2016-07-31 00:20:00"));
            Assert.assertEquals(1, feeds.size());
            for (Activity activity : feeds) {
                log.info("forward3 desc {} ", activity.toString());
                Assert.assertTrue(activity.getCreatedTime().after(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:20:00"))));
            }

            // backward from
            feeds = feedService.getFeeds(userId, Utils.parseTimeInMillis("2016-07-31 00:20:00"), false);
            Assert.assertEquals(3, feeds.size());
            previousActivity = null;
            for (int i = 0; i < feeds.size(); i++) {
                Activity activity = feeds.get(i);
                log.info("backward1 desc {}", activity.toString());
                Assert.assertTrue(activity.getCreatedTime().before(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:20:00"))));
                if (previousActivity != null) {
                    Assert.assertTrue(previousActivity.getCreatedTime().after(activity.getCreatedTime()));
                }
                previousActivity = activity;
            }

            // maxResult = 1
            feeds = feedService.getFeeds(userId, Utils.parseTimeInMillis("2016-07-31 00:20:00"), false, 1, false);
            Assert.assertEquals(1, feeds.size());
            for (int i = 0; i < feeds.size(); i++) {
                Activity activity = feeds.get(i);
                Assert.assertTrue(activity.getCreatedTime().before(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:20:00"))));
            }
        } catch (NewsfeedException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
