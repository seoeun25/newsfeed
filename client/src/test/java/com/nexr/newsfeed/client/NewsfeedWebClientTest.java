package com.nexr.newsfeed.client;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.nexr.newsfeed.entity.Activity;
import com.nexr.newsfeed.entity.Friend;
import com.nexr.newsfeed.entity.User;
import com.nexr.newsfeed.server.NewsfeedServer;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;

public class NewsfeedWebClientTest {

    private static final String BASE_URL = "http://localhost:2828";
    private static Logger log = LoggerFactory.getLogger(NewsfeedWebClientTest.class);
    private static NewsfeedWebClient client;
    private static NewsfeedServer server;

    @BeforeClass
    public static void setupClass() {
        System.setProperty("persistenceUnit", "newsfeed-test-hsql");
        startServer();
        client = new NewsfeedWebClient(BASE_URL);
    }

    @AfterClass
    public static void tearDown() {
        shutdownServer();
    }

    private static void startServer() {
        try {
            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    Injector injector = Guice.createInjector(new NewsfeedServer());
                    Injector injector2 = injector.getInstance(Injector.class);
                    log.info(String.valueOf(injector == injector2));
                    server = injector.getInstance(NewsfeedServer.class);
                    try {
                        server.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
            t1.start();

            Thread.sleep(7000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void shutdownServer() {
        try {
            server.getJPAService().instrument();
            server.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUsers() {
        User user1 = null;
        try {
            user1 = client.createUser("abcde@email.com", "abcde");
        } catch (Exception e) {
            Assert.fail("Fail to create user");
        }

        try {
            User user2 = client.createUser("abcde@email.com", "abcde");
            Assert.fail("Can not register the email exists already : " + "abcde@email.com");
        } catch (Exception e) {

        }

        try {
            User userRe = client.getUser(user1.getId());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Fail to get User : " + user1.getId());
        }

        try {
            long lastViewTime = System.currentTimeMillis();
            user1.setLastViewTime(new Timestamp(lastViewTime));
            User userUpdated = client.updateLastviewTimeOf(user1.getId(), lastViewTime);
            Assert.assertNotNull(userUpdated);
            Assert.assertEquals(user1.getId(), userUpdated.getId());
            Assert.assertEquals(user1.getLastViewTime().getTime(), userUpdated.getLastViewTime().getTime());

            // compare with retrieved User
            User userGet = client.getUser(user1.getId());
            Assert.assertNotNull(userGet);
            Assert.assertEquals(userUpdated.getLastViewTime().getTime(), userGet.getLastViewTime().getTime());

            client.getUsers();

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Fail to createUser");
        }

    }

    @Test
    public void testFollowing() {
        try {
            long userId = client.createUser("a@email.com", "a").getId();
            User user1 = client.createUser("b@email.com", "b");
            User user2 = client.createUser("c@email.com", "c");
            User user3 = client.createUser("d@email.com", "d");


            Friend friend1 = client.follow(userId, user1.getId());
            Assert.assertNotNull(friend1);
            Friend friend2 = client.follow(userId, user2.getId());
            Assert.assertNotNull(friend2);
            Friend friend3 = client.follow(userId, user3.getId());
            Assert.assertNotNull(friend3);

            List<Long> ids = client.getFollowing(userId);
            Assert.assertEquals(3, ids.size());
            log.info("following ids : " + ids);


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Fail to follow");
        }
    }

    @Test
    public void testActivities() {
        try {
            long userId = client.createUser("azrael30@email.com", "azrael40").getId();
            Activity activity = client.postMessage(userId, "Hello newsfeed 1");
            Assert.assertEquals(userId, activity.getUserId());
            Assert.assertEquals("Hello newsfeed 1", activity.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Fail to post news");
        }
    }

    @Test
    public void testFeed() throws InterruptedException {
        try {
            User user = client.createUser("azrael40@email.com", "azrael40");
            long userId = user.getId();
            Assert.assertNull(user.getLastViewTime());
            log.info("userId :  {}", userId);
            User user1 = client.createUser("x@email.com", "x");
            User user2 = client.createUser("y@email.com", "y");
            User user3 = client.createUser("z@email.com", "z");
            User user4 = client.createUser("xxx@email.com", "xxx");
            User user5 = client.createUser("yyy@email.com", "yyy");
            User user6 = client.createUser("zzz@email.com", "zzz");

            client.follow(userId, user1.getId());
            client.follow(userId, user2.getId());
            client.follow(userId, user3.getId());

            client.postMessage(user1.getId(), "message 1 - by 1");
            Thread.sleep(100);
            client.postMessage(user2.getId(), "message 2 - by 2");
            Thread.sleep(100);
            client.postMessage(user3.getId(), "message 3 - by 5");
            Thread.sleep(100);
            client.postMessage(user6.getId(), "message 4 - by 6");
            long basetime = System.currentTimeMillis();
            Thread.sleep(100);
            client.postMessage(user3.getId(), "message 5 - by 3");
            Thread.sleep(100);
            client.postMessage(user2.getId(), "message 6 - by 2");

            List<Long> followings = client.getFollowing(userId);
            Assert.assertEquals(3, followings.size());

            List<Activity> feeds = client.getFeeds(userId); // desc. the latest first.
            Assert.assertEquals(5, feeds.size());
            Activity previousActivity = null;
            for (int i = 0; i < feeds.size(); i++) {
                Activity activity = feeds.get(i);
                log.info(activity.toString());
                if (previousActivity != null) {
                    Assert.assertTrue(previousActivity.getCreatedTime().after(activity.getCreatedTime()));
                }
                previousActivity = activity;
            }

            // if call getFeeds(), it update lastviewTime of User
            user = client.getUser(userId);
            Assert.assertNotNull(user.getLastViewTime());
            Assert.assertEquals(feeds.get(0).getCreatedTime().getTime(), user.getLastViewTime().getTime());

            // retrieve from lastviewTime
            feeds = client.getFeeds(userId);
            Assert.assertEquals(1, feeds.size());

            // retrieve from specific time
            feeds = client.getFeeds(userId, basetime, true, 10, false);
            Assert.assertEquals(2, feeds.size());

            client.postMessage(user4.getId(), "message 7 - by 4");
            client.postMessage(user3.getId(), "message 8 - by 3");
            client.postMessage(user1.getId(), "message 9 - by 1");
            feeds = client.getFeeds(userId);
            Assert.assertEquals(3, feeds.size());

            client.follow(userId, user6.getId());
            client.postMessage(user6.getId(), "message 10 - by 6");
            client.postMessage(user5.getId(), "message 11 - by 5");
            feeds = client.getFeeds(userId);
            Assert.assertEquals(2, feeds.size());
            long lastviewTime = client.getUser(userId).getLastViewTime().getTime();
            Assert.assertTrue(lastviewTime >= basetime);

            feeds = client.getFeeds(userId, basetime, false, 10, false);
            Assert.assertEquals(4, feeds.size());
            for (int i = 0; i < feeds.size(); i++) {
                Activity activity = feeds.get(i);
                log.info("backward : {}", activity);
                Assert.assertTrue(activity.getCreatedTime().getTime() <= basetime);
            }
            // no need to udpate lastviewTime if setting value is before the current lastviewTime.
            Assert.assertEquals(lastviewTime, client.getUser(userId).getLastViewTime().getTime());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Fail to get Feeds");
        }
    }

}
