package com.nexr.newsfeed.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.common.Utils;
import com.nexr.newsfeed.entity.Friend;
import com.nexr.newsfeed.service.FollowingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/followings")
public class Followings {

    private static Logger log = LoggerFactory.getLogger(Followings.class);

    private FollowingService followingService;

    public Followings() {

    }

    @Inject
    public void setFollowingService(FollowingService followingService) {
        this.followingService = followingService;
    }

    @POST
    @Path("/")
    @Produces("application/json")
    public Response follow(@FormParam("userId") String userId, @FormParam("followingId") String followingId) {
        log.debug(" REST : follow : userId [{}], followingId [{}] ", userId, followingId);
        if (userId == null || followingId == null) {
            return Response.status(400).entity(Utils.convertErrorObjectToJson(400, "name and email can not be null")).build();
        }

        try {
            Friend friend = followingService.follow(Long.valueOf(userId), Long.valueOf(followingId));
            return Response.status(200).entity(friend.toJson()).build();
        } catch (Exception e) {
            return Response.status(500).entity(Utils.convertErrorObjectToJson(500,
                    "Fail to follow : userId [" + userId + "], " + "followingId [" + followingId + "]")).build();
        }

    }


    @GET
    @Path("/{userId}")
    @Produces("application/json")
    public Response getFollowings(@PathParam("userId") String userId) {
        if (userId == null) {
            return Response.status(400).entity(Utils.convertErrorObjectToJson(400, "userId can not be null")).build();
        }

        try {
            List<Long> ids = followingService.getFollowings(Long.parseLong(userId));
            ObjectMapper mapper = new ObjectMapper();
            String jsonStr = mapper.writeValueAsString(ids);
            return Response.status(200).entity(jsonStr).build();
        } catch (NewsfeedException e) {
            return Response.status(404).entity(Utils.convertErrorObjectToJson(404, "User Not Found")).build();
        } catch (Exception e) {
            return Response.status(500).entity(Utils.convertErrorObjectToJson(500, e.getMessage())).build();
        }
    }

}
