package com.nexr.newsfeed.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.common.Utils;
import com.nexr.newsfeed.entity.User;
import com.nexr.newsfeed.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

@Path("/users")
public class Users {

    private static Logger log = LoggerFactory.getLogger(Users.class);

    private UserService userService;

    public Users() {

    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @POST
    @Path("/")
    @Produces("application/json")
    public Response createUser(@FormParam("name") String name, @FormParam("email") String email) {
        log.debug(" REST : createUser: name [{}], email[{}] ", name, email);
        if (name == null || email == null) {
            return Response.status(400).entity(Utils.convertErrorObjectToJson(400, "name and email can not be null")).build();
        }

        try {
            User user = userService.createUser(email, name);
            return Response.status(200).entity(user.toJson()).build();
        } catch (Exception e) {
            return Response.status(500).entity(Utils.convertErrorObjectToJson(500,
                    "Fail to create user: name [" + name + "], " + "email [" + email + "]")).build();
        }

    }

    @PUT
    @Path("/{id}")
    @Produces("application/json")
    public Response updateLastviewTimeOf(@PathParam("id") String userId, @FormParam("lastviewTime") long lastviewTime) {
        log.debug("REST : updateLastviewTimeOf : id [{}], user [{}]", userId, lastviewTime);
        try {
            User user = new User();
            user.setId(Long.valueOf(userId));
            user.setLastViewTime(new Date(lastviewTime));
            User userUpdated = userService.updateLastviewTime(user);
            return Response.status(200).entity(userUpdated.toJson()).build();
        } catch (Exception e) {
            return Response.status(500).entity(Utils.convertErrorObjectToJson(500,
                    "Fail to update the lastviewTime, user=" + lastviewTime)).build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getUser(@PathParam("id") String userId) {
        //TODO param check를 따로.
        if (userId == null) {
            return Response.status(400).entity(Utils.convertErrorObjectToJson(400, "id can not be null")).build();
        }

        try {
            User user = userService.getUser(Long.parseLong(userId));
            ObjectMapper mapper = new ObjectMapper();
            String jsonStr = mapper.writeValueAsString(user);
            return Response.status(200).entity(jsonStr).build();
        } catch (NewsfeedException e) {
            return Response.status(404).entity(Utils.convertErrorObjectToJson(404, "User Not Found")).build();
        } catch (Exception e) {
            return Response.status(500).entity(Utils.convertErrorObjectToJson(500, e.getMessage())).build();
        }
    }

    @GET
    @Path("/")
    @Produces("application/json")
    public Response getUsers() {
        try {
            List<User> list = userService.getUers();
            ObjectMapper mapper = new ObjectMapper();
            String jsonStr = mapper.writeValueAsString(list);
            return Response.status(200).entity(jsonStr).build();
        } catch (NewsfeedException e) {
            return Response.status(404).entity(Utils.convertErrorObjectToJson(404, "User Not Found")).build();
        } catch (Exception e) {
            return Response.status(500).entity(Utils.convertErrorObjectToJson(500, e.getMessage())).build();
        }
    }


}
