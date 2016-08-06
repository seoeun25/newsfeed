package com.nexr.newsfeed.rest;

import com.google.inject.Inject;
import com.nexr.newsfeed.common.Utils;
import com.nexr.newsfeed.entity.Activity;
import com.nexr.newsfeed.service.FeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/activities")
public class Activities {

    private static Logger log = LoggerFactory.getLogger(Activities.class);

    private FeedService feedService;

    public Activities() {

    }

    @Inject
    public void setFeedService(FeedService feedService) {
        this.feedService = feedService;
    }

    @POST
    @Path("/")
    @Produces("application/json")
    public Response postMessage(@FormParam("userId") String userId, @FormParam("message") String message) {
        log.debug(" REST : postMessage : userId [{}], message [{}] ", userId, message);
        if (userId == null || message == null) {
            return Response.status(400).entity(Utils.convertErrorObjectToJson(400, "name and email can not be null")).build();
        }

        try {
            Activity activity = feedService.postMessage(Long.valueOf(userId), message);
            return Response.status(200).entity(activity.toJson()).build();
        } catch (Exception e) {
            return Response.status(500).entity(Utils.convertErrorObjectToJson(500,
                    "Fail to postMessage : userId [" + userId + "], " + "message [" + message + "]")).build();
        }

    }


}
