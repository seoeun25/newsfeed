package com.nexr.newsfeed.service;

import com.google.inject.Inject;
import com.nexr.newsfeed.Context;
import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.NewsfeedRuntimeException;
import com.nexr.newsfeed.common.Utils;
import com.nexr.newsfeed.entity.Activity;
import com.nexr.newsfeed.entity.User;
import com.nexr.newsfeed.jpa.ActivityQueryExceutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FeedService {

    private static Logger log = LoggerFactory.getLogger(FeedService.class);

    private final Context context;
    private UserService userService;
    private ActivityQueryExceutor activityQueryExceutor;
    private FollowingService followingService;
    private int defaultMaxResult = 20; // TODO configuration
    private boolean includeMine = false; //TODO configuration

    @Inject
    public FeedService(Context context) {
        this.context = context;
    }

    @Inject
    public void setActivityQueryExceutor(ActivityQueryExceutor activityQueryExceutor) {
        this.activityQueryExceutor = activityQueryExceutor;
    }

    @Inject
    public void setFollowingService(FollowingService followingService) {
        this.followingService = followingService;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }


    /**
     * Posts message.
     *
     * @param userId  a id of user who post the message
     * @param message a message
     * @throws NewsfeedException
     */
    public Activity postMessage(long userId, String message) throws NewsfeedException {
        Activity activity = new Activity(userId, message);
        return postMessage(activity);
    }

    public Activity postMessage(Activity activity) throws NewsfeedException {
        userService.getUser(activity.getUserId()); // TODO join with user table
        activityQueryExceutor.insertActivity(activity);
        return activity;
    }

    private long getLastviewTime(long userId) throws NewsfeedException {
        Date date = userService.getUser(userId).getLastViewTime();
        long baseTimestamp = date == null ? Utils.add(System.currentTimeMillis(), -1) : date.getTime();
        return baseTimestamp;
    }

    /**
     * <p> {@code baseTimestamp}를 이후에 posting된 message들을 리턴한다.
     * <p> 리턴되는 feed의 수는 {@code maxResult} 이다.
     * <p> Posting message의 created time을 기준으로 {@code asc}가 {@code true} 이면 asc 정렬되어 오래된 posting이 가장 앞에 있고,
     * {@code false}이면 desc 정렬이 되어있기 때문에, 최신의 posting이 가장 앞에 있다.</p>
     *
     * @param baseTimestamp a base time to retrieve
     * @param maxResult     a max number of feed to retrieve
     * @param asc           if true, ordered by created time asc.
     * @return a list of feed
     * @throws NewsfeedException
     */
    public List<Activity> getFeedsAll(long baseTimestamp, int maxResult, boolean asc)
            throws NewsfeedException {
        if (baseTimestamp <= 0) {
            baseTimestamp = Utils.add(System.currentTimeMillis(), -1);
        }
        if (maxResult <= 0) {
            maxResult = defaultMaxResult;
        }
        log.info("baseTimestamp [{}], maxResult [{}], asc [{}]", baseTimestamp, maxResult, asc);

        ActivityQueryExceutor.ActivityQuery query = null;
        if (asc) {
            query = ActivityQueryExceutor.ActivityQuery.GET_ALL_ACTIVITIES_ASC;
        } else {
            query = ActivityQueryExceutor.ActivityQuery.GET_ALL_ACTIVITIES_DESC;
        }
        log.info("queryName {}, baseTimestamp {}, maxResult {}", query.name(), Utils.formatDateString(baseTimestamp), maxResult);

        List<Activity> feeds = activityQueryExceutor.getList(query, new Object[]{new Timestamp(baseTimestamp), maxResult});

        return feeds;
    }



    /**
     * <code>userId</code> 에 해당하는 user가 following하는 user들이 post한 message들을 리턴한다.
     * <p> User의 lastviewTime, 즉 user가 마지막 본 feed의 timestamp가 basetime이 되어 이 시간 이후에
     * posting 된 message들을 리턴한다. 만약 user가 처음으로 feed를 받는 경우, {@code lastviewTime}이 설정되지 않았기 때문에
     * 현재 시간보다 24시간 전을 basetime으로 간주하여 조회한다.
     * 이 메서드가 호출되면 User의 lastviewTime 도 같이 업데이트 된다.
     * <p> 리턴되는 feed의 수는 DefaultMaxResult 값(20) 이다.
     * <p> Posting message의 created time을 기준으로 desc 정렬이 되어있기 때문에, 최신의 posting이 가장 앞에 있다.</p>
     *
     * @param userId a id of user who get the feed
     * @return a list of activity. 오래된 posting 이 맨 앞에.
     * @throws NewsfeedException
     */
    public List<Activity> getFeeds(long userId) throws NewsfeedException {
        return getFeeds(userId, 0, true, defaultMaxResult, false);
    }

    /**
     * <code>userId</code> 에 해당하는 user가 following하는 user들이 post한 message들을 리턴한다.
     * <p> {@code baseTimestamp}를 기준으로 그 이후 posting된 message들을 리턴한다.
     * <p> 리턴되는 feed의 수는 DefaultMaxResult 값(20) 이다.
     * <p> Posting message의 created time을 기준으로 desc 정렬이 되어있기 때문에, 최신의 posting이 가장 앞에 있다.</p>
     *
     * @param userId        a id of user who get the feed
     * @param baseTimestamp a base time to retrieve
     * @return a list of activity
     * @throws NewsfeedException
     */
    public List<Activity> getFeeds(long userId, long baseTimestamp) throws NewsfeedException {
        return getFeeds(userId, baseTimestamp, true, defaultMaxResult, false);
    }

    /**
     * <code>userId</code> 에 해당하는 user가 following하는 user들이 post한 message들을 리턴한다.
     * <p> {@code baseTimestamp}를 기준으로 그 이후 posting된 message들을 리턴한다.
     * <p> 리턴되는 feed의 수는 {@code maxResult} 이다.
     * <p> Posting message의 created time을 기준으로 desc 정렬이 되어있기 때문에, 최신의 posting이 가장 앞에 있다.</p>
     *
     * @param userId        a id of user who get the feed
     * @param baseTimestamp a base time to retrieve
     * @param maxResult     a max number of feed to retrieve
     * @return a list of activity
     * @throws NewsfeedException
     */
    public List<Activity> getFeeds(long userId, long baseTimestamp, int maxResult) throws NewsfeedException {
        return getFeeds(userId, baseTimestamp, true, maxResult, false);
    }

    /**
     * <code>userId</code> 에 해당하는 user가 following하는 user들이 post한 message들을 리턴한다.
     * <p> {@code baseTimestamp}를 기준으로 {@code forward}가 {@code true}이면 그 이후 posting된 message들을,
     * {@code false}이면 그 이전에 posting된 message들을 리턴한다.
     * <p> 리턴되는 feed의 수는 DefaultMaxResult 값(20) 이다.
     * <p> Posting message의 created time을 기준으로 desc 정렬이 되어있기 때문에, 최신의 posting이 가장 앞에 있다.</p>
     *
     * @param userId        a id of user who get the feed
     * @param baseTimestamp a base time to retrieve
     * @param forward       if true, feeds are after <code>baseTimestamp</code>, otherwise feeds are before <code>baseTimestamp</code>
     * @return a list of feed
     * @throws NewsfeedException
     */
    public List<Activity> getFeeds(long userId, long baseTimestamp, boolean forward) throws NewsfeedException {
        return getFeeds(userId, baseTimestamp, forward, defaultMaxResult, false);
    }

    /**
     * <code>userId</code> 에 해당하는 user가 following하는 user들이 post한 message들을 리턴한다.
     * <p> {@code baseTimestamp}를 기준으로 {@code forward}가 {@code true}이면 그 이후 posting된 message들을,
     * {@code false}이면 그 이전에 posting된 message들을 리턴한다.
     * <p> 리턴되는 feed의 수는 {@code maxResult} 이다.
     * <p> Posting message의 created time을 기준으로 {@code asc}가 {@code true} 이면 asc 정렬되어 오래된 posting이 가장 앞에 있고,
     * {@code false}이면 desc 정렬이 되어있기 때문에, 최신의 posting이 가장 앞에 있다.</p>
     *
     * @param userId        a id of user who get the feed
     * @param baseTimestamp a base time to retrieve
     * @param forward       if true, feeds are after <code>baseTimestamp</code>, otherwise feeds are before <code>baseTimestamp</code>
     * @param maxResult     a max number of feed to retrieve
     * @param asc           if true, ordered by created time asc.
     * @return a list of feed
     * @throws NewsfeedException
     */
    public List<Activity> getFeeds(long userId, long baseTimestamp, boolean forward, int maxResult, boolean asc)
            throws NewsfeedException {
        if (baseTimestamp <= 0) {
            baseTimestamp = getLastviewTime(userId);
        }
        if (maxResult <= 0) {
            maxResult = defaultMaxResult;
        }
        log.debug("userId [{}], baseTimestamp [{}], forward [{}], maxResult [{}], asc [{}]", userId, baseTimestamp, forward,
                maxResult, asc);
        List<Long> followings = followingService.getFollowings(userId);
        if (includeMine) {
            // TODO get my Id from login session
            //followings.add(myId);
        }

        log.debug("following list {} by user {}", followings.size(), userId);
        if (followings.size() == 0) {
            log.info("following list {} by user{} = 0", followings.size(), userId);
            return new ArrayList<>();
        }
        ActivityQueryExceutor.ActivityQuery query = null;
        if (forward && asc) {
            query = ActivityQueryExceutor.ActivityQuery.GET_BYFOLLOWING_FORWARD_ASC;
        } else if (forward && !asc) {
            query = ActivityQueryExceutor.ActivityQuery.GET_BYFOLLOWING_FORWARD_DESC;
        } else if (!forward && asc) {
            query = ActivityQueryExceutor.ActivityQuery.GET_BYFOLLOWING_BACKWARD_ASC;
        } else {
            query = ActivityQueryExceutor.ActivityQuery.GET_BYFOLLOWING_BACKWARD_DESC;
        }
        log.debug("queryName {}, baseTimestamp {}, maxResult {}", query.name(), Utils.formatDateString(baseTimestamp), maxResult);

        List<Activity> feeds = activityQueryExceutor.getList(query, new Object[]{new Timestamp(baseTimestamp), followings,
                maxResult});

        // update lastviewTime
        if (feeds.size() > 0) {
            Activity latest = asc ? feeds.get(feeds.size() - 1) : feeds.get(0);
            updateLastviewTime(userId, latest.getCreatedTime().getTime());
        }
        return feeds;
    }

    private boolean updateLastviewTime(long userId, long lastviewTime) {
        try {
            User user = userService.getUser(userId);
            Date date = user.getLastViewTime();
            if (date != null && lastviewTime <= date.getTime()) {
                log.info("Current lastvieTime[{}] is after setting time[{}]. No need to update !! ", date.getTime(), lastviewTime);
                return true;
            }
            userService.updateLastviewTime(userId, lastviewTime);
            return true;
        } catch (Exception e) {
            log.warn("Fail to update lastviewTime after getFeeds, userId {}, lastviewTime {}. It would get feeds duplicated on " +
                    "next time.", userId, lastviewTime);
            throw new NewsfeedRuntimeException("Fail to update lastviewTime after getFeeds : userId " + userId +
                    ", lastviewTime " + ": " + lastviewTime, e);
        }

    }

}

