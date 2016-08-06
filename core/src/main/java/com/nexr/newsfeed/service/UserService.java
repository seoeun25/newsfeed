package com.nexr.newsfeed.service;

import com.google.inject.Inject;
import com.nexr.newsfeed.Context;
import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.common.Utils;
import com.nexr.newsfeed.entity.User;
import com.nexr.newsfeed.jpa.UserQueryExceutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class UserService {

    private final Context context;
    private Logger log = LoggerFactory.getLogger(UserService.class);
    private UserQueryExceutor userQueryExceutor;

    @Inject
    public UserService(Context context) {
        this.context = context;
    }

    @Inject
    public void setUserQueryExceutor(UserQueryExceutor userQueryExceutor) {
        this.userQueryExceutor = userQueryExceutor;
    }

    /**
     * Create user.
     *
     * @param email
     * @param name
     * @return created user
     * @throws NewsfeedException if an persistence exception occurs
     */
    public User createUser(String email, String name) throws NewsfeedException {
        try {
            if (!Utils.validEmail(email)) {
                throw new NewsfeedException("No valid email: "+ email);
            }
            User user = new User(email, name);
            userQueryExceutor.insertUser(user);
            return user;
        } catch (NewsfeedException e) {
            log.debug("Fail to create User: {}, {}", email, name, e);
            throw e;
        }
    }

    /**
     * Gets a user
     *
     * @param id a user id
     * @return a user
     * @throws NewsfeedException if no user corresponding a given {@code id}
     */
    public User getUser(long id) throws NewsfeedException {
        User user = userQueryExceutor.get(UserQueryExceutor.UserQuery.GET_BYID, new Object[]{id});
        return user;
    }

    /**
     * Gets all user
     *
     * @return a list of user
     * @throws NewsfeedException if an persistence exception occurs
     */
    public List<User> getUsers() throws NewsfeedException {
        return userQueryExceutor.getList(UserQueryExceutor.UserQuery.GET_USER_ALL, new Object[]{});
    }

    /**
     * Update the lastviewTime of user
     *
     * @param lastviewTime
     * @return a user
     * @throws NewsfeedException if no user representing a given {@code user}
     */
    public User updateLastviewTime(long userId, long lastviewTime) throws NewsfeedException {
        User userGet = userQueryExceutor.get(UserQueryExceutor.UserQuery.GET_BYID, new Object[]{userId});
        userGet.setLastviewTime(new Date(lastviewTime));
        int updated = userQueryExceutor.executeUpdate(UserQueryExceutor.UserQuery.UPDATE_LASTVIEW_TIME, userGet);
        if (updated != 1) {
            throw new NewsfeedException("Fail to updateLastViewTime, user: " + userGet + ", updatedRow=" + updated);
        }
        return userGet;
    }

}
