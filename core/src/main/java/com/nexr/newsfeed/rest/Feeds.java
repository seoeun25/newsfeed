package com.nexr.newsfeed.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.common.Utils;
import com.nexr.newsfeed.entity.Activity;
import com.nexr.newsfeed.service.FeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/feeds")
public class Feeds {

    private static Logger log = LoggerFactory.getLogger(Feeds.class);

    private FeedService feedService;

    public Feeds() {

    }

    @Inject
    public void setFeedService(FeedService feedService) {
        this.feedService = feedService;
    }

    @GET
    @Path("/")
    @Produces("application/json")
    public Response getFeedsAll(@QueryParam("basetime") String basetime, @QueryParam("maxResult") int maxResult,
                             @QueryParam("asc") String asc) {
        log.debug(" REST : basetime [{}], maxResult [{}], asc [{}] ", basetime, maxResult, asc);

        try {
            long baseTime = basetime == null ? 0 : Long.valueOf(basetime);
            boolean bAsc = asc == null ? false : Boolean.parseBoolean(asc);
            log.info("bAsc {}", bAsc );
            List<Activity> feeds = feedService.getFeedsAll(baseTime, maxResult, bAsc);

            ObjectMapper mapper = new ObjectMapper();
            String jsonStr = mapper.writeValueAsString(feeds);
            return Response.status(200).entity(jsonStr).build();
        } catch (Exception e) {
            return Response.status(500).entity(Utils.convertErrorObjectToJson(500, e.getMessage())).build();
        }
    }

    @GET
    @Path("/{userId}")
    @Produces("application/json")
    public Response getFeeds(@PathParam("userId") String userId, @QueryParam("basetime") String basetime,
                             @QueryParam("forward") String forward, @QueryParam("maxResult") int maxResult,
                             @QueryParam("asc") String asc) {
        log.debug(" REST : userID [{}], basetime [{}], forward [{}], maxResult [{}], asc [{}] ",
                userId, basetime, forward, maxResult, asc);
        if (userId == null) {
            return Response.status(400).entity(Utils.convertErrorObjectToJson(400, "userId can not be null")).build();
        }

        try {
            long baseTime = basetime == null ? 0 : Long.valueOf(basetime);
            boolean bForward = forward == null ? true : Boolean.parseBoolean(forward);
            boolean bAsc = asc == null ? false : Boolean.parseBoolean(asc);
            log.debug("bforward {}, bAsc {}", bForward, bAsc);
            List<Activity> feeds = feedService.getFeeds(Long.valueOf(userId), baseTime, bForward, maxResult, bAsc);

            ObjectMapper mapper = new ObjectMapper();
            String jsonStr = mapper.writeValueAsString(feeds);
            return Response.status(200).entity(jsonStr).build();
        } catch (NewsfeedException e) {
            return Response.status(404).entity(Utils.convertErrorObjectToJson(404, "User Not Found")).build();
        } catch (Exception e) {
            return Response.status(500).entity(Utils.convertErrorObjectToJson(500, e.getMessage())).build();
        }
    }

}
