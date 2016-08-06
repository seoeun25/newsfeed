package com.nexr.newsfeed.util;

import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.entity.Friend;

import java.util.List;

public interface FollowCache {
    Friend follow(long userId, long followingId) throws NewsfeedException;

    List<Long> getFollowings(long userId);
}
