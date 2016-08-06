package com.nexr.newsfeed.service;

import com.google.inject.Inject;
import com.nexr.newsfeed.Context;
import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.entity.Friend;
import com.nexr.newsfeed.util.FollowCache;

import java.util.List;

public class FollowingService {

    private final Context context;
    private FollowCache followingCache;

    @Inject
    public FollowingService(Context context) {
        this.context = context;
    }

    @Inject
    public void setFollowingCache(FollowCache followingCache) {
        this.followingCache = followingCache;
    }

    /**
     * <code>userId</code> 에 해당하는 user가 <code>followingId</code> 에 해당하는 user를 follow 한다.
     *
     * @param userId      a id of user who follows
     * @param followingId a id of user who followed
     * @throws NewsfeedException
     */
    public Friend follow(long userId, long followingId) throws NewsfeedException {
        return followingCache.follow(userId, followingId);
    }

    /**
     * Gets the following users by a given user.
     *
     * @param userId a id of user
     * @return a list of user id
     */
    public List<Long> getFollowings(long userId) throws NewsfeedException {
        return followingCache.getFollowings(userId);
    }

}
