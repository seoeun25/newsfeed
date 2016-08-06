package com.nexr.newsfeed.util;

import com.google.inject.Inject;
import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.entity.Friend;
import com.nexr.newsfeed.entity.User;
import com.nexr.newsfeed.jpa.FriendQueryExceutor;
import com.nexr.newsfeed.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LocalFollowCache implements FollowCache {

    private static Logger log = LoggerFactory.getLogger(LocalFollowCache.class);

    private Map<Long, Set<Long>> followingCache = new ConcurrentHashMap<>();
    private FriendQueryExceutor friendQueryExceutor;
    private UserService userService;

    @Inject
    public LocalFollowCache(FriendQueryExceutor friendQueryExceutor, UserService userService) {
        this.friendQueryExceutor = friendQueryExceutor;
        this.userService = userService;
        load();
    }

    public void load() {
        try {
            List<Friend> list = friendQueryExceutor.getList(FriendQueryExceutor.FriendQuery.GET_FOLLOWING_ALL, new Object[]{});
            for (Friend friend : list) {
                put(friend.getUserId(), friend.getFollowingId());
            }
            log.info("LocalFollowCache load complete, followings={}", list.size());
        } catch (Exception e) {
            log.warn("Fail to load from DB", e);
        }

    }

    private void put(long userId, long followingId) {
        Set<Long> followings = followingCache.get(userId);
        if (followings == null) {
            followings = new HashSet<>();
            followings.add(followingId);
            followingCache.put(userId, followings);
        } else {
            followings.add(followingId);
        }
    }

    @Override
    public Friend follow(long userId, long followingId) throws NewsfeedException {
        try {
            // TODO join with user table
            User user1 = userService.getUser(userId);
            User user2 = userService.getUser(followingId);
        } catch (NewsfeedException e) {
            throw e;
        }
        try {
            Friend friend = friendQueryExceutor.get(FriendQueryExceutor.FriendQuery.GET_FOLLOWING,
                    new Object[]{userId, followingId});
            if (friend != null) {
                log.info("{} follows {} already", userId, followingId);
                return friend;
            }
        } catch (NewsfeedException e) {
            // nothing to do
        }

        Friend friend = new Friend(userId, followingId);
        long id = (Long) friendQueryExceutor.insertFriend(friend);
        friend.setId(id);
        // cache
        put(userId, followingId);
        return friend;
    }

    @Override
    public List<Long> getFollowings(long userId) {
        List<Long> list = new ArrayList<>();
        if (!followingCache.containsKey(userId)) {
            return list;
        }

        list.addAll(followingCache.get(userId));
        return list;
    }
}
