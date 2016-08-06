package com.nexr.newsfeed.jpa;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.nexr.newsfeed.NewsfeedException;
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

public class UserQueryExecutorTest {

    private static Logger log = LoggerFactory.getLogger(UserQueryExecutorTest.class);

    private static UserQueryExceutor queryExecutor;
    private static JPAService jpaService;

    @BeforeClass
    public static void setupClass() {
        try {
            System.setProperty("persistenceUnit", "newsfeed-test-hsql");
            Injector injector = Guice.createInjector(new NewsfeedServer());

            Thread.sleep(1000);
            queryExecutor = injector.getInstance(UserQueryExceutor.class);
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
            User user1 = new User("seoeun25@gmail.com", "seoeun");
            long id1 = (Long) queryExecutor.insertUser(user1);
            log.info("id1 : {}", id1);
            Assert.assertNotNull(id1);
            Assert.assertEquals(id1, user1.getId());

            User user2 = new User("azrael.park@nexr.com", "azrael");
            long id2 = (Long) queryExecutor.insertUser(user2);
            log.info("id2 : {}", id2);
            Assert.assertNotNull(id2);

            // get by id
            User user2Re = queryExecutor.get(UserQueryExceutor.UserQuery.GET_BYID, new Object[]{id2});
            Assert.assertNotNull(user2Re);
            Assert.assertEquals(user2.getName(), user2Re.getName());
            Assert.assertEquals(id2, user2Re.getId());
            Assert.assertEquals(null, user2Re.getLastviewTime());

            // update
            long lastViewTime = System.currentTimeMillis();
            user2Re.setLastviewTime(new Timestamp(lastViewTime));
            int updated = queryExecutor.executeUpdate(UserQueryExceutor.UserQuery.UPDATE_LASTVIEW_TIME, user2Re);
            Assert.assertEquals(1, updated);
            Thread.sleep(100);
            user2Re = queryExecutor.get(UserQueryExceutor.UserQuery.GET_BYID, new Object[]{id2});
            Assert.assertNotNull(user2Re.getLastviewTime());
            Assert.assertEquals(lastViewTime, user2Re.getLastviewTime().getTime());
            log.info("user2Re : {}", user2Re.toJson());
            log.info("user2Re : {}", user2Re.toString());


            // get all
            Thread.sleep(100);
            List<User> all = queryExecutor.getList(UserQueryExceutor.UserQuery.GET_USER_ALL, new Object[]{});
            for (User user : all) {
                log.info("user : " + user);
            }
            //Assert.assertEquals(2, all.size());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetNegative() {
        try {
            Object[] params = new Object[]{-1};
            User user = queryExecutor.get(UserQueryExceutor.UserQuery.GET_BYID, params);

            Assert.fail("Should there no User");
        } catch (NewsfeedException e) {
            Assert.assertTrue(e.getMessage().contains("Not Found"));
        }
    }

    @Test
    public void testUpdateNegative() {
        try {

            User user = new User("@hello.email", "az");
            long id = (Long)queryExecutor.insertUser(user);
            Assert.assertNotNull(id);
            log.info("id : " + id);

            // lastviewTime = null. updated=1
            int updated = queryExecutor.executeUpdate(UserQueryExceutor.UserQuery.UPDATE_LASTVIEW_TIME, user);
            Assert.assertEquals(1, updated);
            User userRe = queryExecutor.get(UserQueryExceutor.UserQuery.GET_BYID, new Object[]{id});
            Assert.assertNotNull(userRe);
            log.info("updated user : " + userRe);
            Assert.assertEquals(null, userRe.getLastviewTime());

            // not registered user. updated=1
            user = new User("nobody@email.com", "nobody");
            List<User> users = queryExecutor.getList(UserQueryExceutor.UserQuery.GET_USER_ALL, new Object[]{});
            boolean contained = false;
            for (User user1: users)  {
                log.info("user1 : " + user1);
                if (user1.getEmail().equals("nobody@email.com")) {
                    contained = true;
                    break;
                }
            }
            Assert.assertFalse(contained);
            updated = queryExecutor.executeUpdate(UserQueryExceutor.UserQuery.UPDATE_LASTVIEW_TIME, user);
            Assert.assertEquals(1, updated);

        } catch (NewsfeedException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

}

