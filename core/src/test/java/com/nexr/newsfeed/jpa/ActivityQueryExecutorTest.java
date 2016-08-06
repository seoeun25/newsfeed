package com.nexr.newsfeed.jpa;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.common.Utils;
import com.nexr.newsfeed.entity.Activity;
import com.nexr.newsfeed.server.NewsfeedServer;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActivityQueryExecutorTest {

    private static Logger log = LoggerFactory.getLogger(ActivityQueryExecutorTest.class);

    private static ActivityQueryExceutor queryExecutor;
    private static JPAService jpaService;

    @BeforeClass
    public static void setupClass() {
        try {
            System.setProperty("persistenceUnit", "newsfeed-test-hsql");
            Injector injector = Guice.createInjector(new NewsfeedServer());

            Thread.sleep(1000);
            queryExecutor = injector.getInstance(ActivityQueryExceutor.class);
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
            Activity activity1 = new Activity(1, "hello world 1");
            activity1.setCreatedTime(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:01:01")));
            long id1 = (Long)queryExecutor.insertActivity(activity1);
            Assert.assertNotNull(id1);
            Assert.assertEquals(0, id1);

            Activity activity2 = new Activity(1, "hello world 2");
            activity2.setCreatedTime(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:10:00")));
            long id2 = (Long)queryExecutor.insertActivity(activity2);
            Assert.assertEquals(1, id2);

            Activity activity3 = new Activity(2, "hello world 3");
            activity3.setCreatedTime(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:01:03")));
            long id3 = (Long)queryExecutor.insertActivity(activity3);
            Assert.assertEquals(2, id3);

            Activity activity4 = new Activity(3, "hello world 4");
            activity4.setCreatedTime(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:01:02")));
            long id4 = (Long)queryExecutor.insertActivity(activity4);
            Assert.assertEquals(3, id4);

            Activity activity5 = new Activity(5, "hello world 5");
            activity5.setCreatedTime(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:20:02")));
            long id5 = (Long)queryExecutor.insertActivity(activity5);
            Assert.assertEquals(4, id5);

            List<Long> followings = Arrays.asList(1l, 3l, 5l);

            // forward from
            List<Activity> feeds = queryExecutor.getList(ActivityQueryExceutor.ActivityQuery.GET_BYFOLLOWING_FORWARD_ASC,
                    new Object[]{new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:00:00")), followings});
            Assert.assertEquals(4, feeds.size());
            for (Activity activity: feeds) {
                log.info("forward {} ", activity.toString());
                Assert.assertTrue(activity.getCreatedTime().after(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:00:00"))));
            }
            // maxResult = 2
            feeds = queryExecutor.getList(ActivityQueryExceutor.ActivityQuery.GET_BYFOLLOWING_FORWARD_ASC,
                    new Object[]{new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:00:00")), followings, 2});
            Assert.assertEquals(2, feeds.size());
            for (Activity activity: feeds) {
                log.info("forward2 {} ", activity.toString());
                Assert.assertTrue(activity.getCreatedTime().after(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:00:00"))));
            }

            feeds = queryExecutor.getList(ActivityQueryExceutor.ActivityQuery.GET_BYFOLLOWING_FORWARD_ASC,
                    new Object[]{new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:20:00")), followings});
            Assert.assertEquals(1, feeds.size());
            Assert.assertEquals(id5, feeds.get(0).getId());
            log.info("--- id5 : " + id5);

            // backward from
            feeds = queryExecutor.getList(ActivityQueryExceutor.ActivityQuery.GET_BYFOLLOWING_BACKWARD_DESC,
                    new Object[]{new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:20:00")), followings});
            Assert.assertEquals(3, feeds.size());
            for(Activity activity: feeds) {
                log.info("backward {}",  activity.toString());
                Assert.assertTrue(activity.getCreatedTime().before(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:20:00"))));
            }

            // maxResult = 1
            feeds = queryExecutor.getList(ActivityQueryExceutor.ActivityQuery.GET_BYFOLLOWING_BACKWARD_DESC,
                    new Object[]{new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:20:00")), followings, 1});
            Assert.assertEquals(1, feeds.size());
            for(Activity activity: feeds) {
                Assert.assertTrue(activity.getCreatedTime().before(new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:20:00"))));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetNegative() throws ParseException{
        try {
            Object[] params = new Object[]{new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:20:00")), Arrays.asList(-1l)};
            List<Activity> list = queryExecutor.getList(ActivityQueryExceutor.ActivityQuery.GET_BYFOLLOWING_BACKWARD_DESC, params);

            Assert.assertEquals(0, list.size());
        } catch (NewsfeedException e) {
            Assert.assertTrue(e.getMessage().contains("Not Found"));
        }
    }

    @Test
    public void testEmptyFollowing() throws ParseException, NewsfeedException {
        try {
            List<Activity> feeds = queryExecutor.getList(ActivityQueryExceutor.ActivityQuery.GET_BYFOLLOWING_BACKWARD_DESC,
                    new Object[]{new Timestamp(Utils.parseTimeInMillis("2016-07-31 00:20:00")), new ArrayList<Integer>(), 1});
            Assert.fail();
        } catch (IllegalArgumentException e) {

        }

    }

}

