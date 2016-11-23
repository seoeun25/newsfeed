package com.nexr.newsfeed.rest;

import com.google.inject.Inject;
import com.nexr.newsfeed.common.Utils;
import com.nexr.newsfeed.entity.User;
import com.nexr.newsfeed.objs.MonitoringEvent;
import com.nexr.newsfeed.service.MonitoringService;
import com.nexr.newsfeed.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by seoeun on 11/22/16.
 */
@Path ("/monitors")
public class Monitors {

    private static final Logger log = LoggerFactory.getLogger(Monitors.class);

    private MonitoringService monitoringService;

    public Monitors() {

    }

    @Inject
    public void setMonitoringService(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
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
            //User user = userService.createUser(email, name);
            User user = new User(email, name);
            return Response.status(200).entity(user.toJson()).build();
        } catch (Exception e) {
            return Response.status(500).entity(Utils.convertErrorObjectToJson(500,
                    "Fail to create user: name [" + name + "], " + "email [" + email + "] : " + e.getMessage())).build();
        }

    }

    @POST
    @Path("/sending")
    @Produces("application/json")
    public Response sendEvent(@FormParam("event") String event) {
        log.debug(" REST : sendEvent: event [{}]", event);
        if (event == null) {
            return Response.status(400).entity(Utils.convertErrorObjectToJson(400, "name and email can not be null")).build();
        }

        try {
            InputStream is = new ByteArrayInputStream(event.getBytes());
            MonitoringEvent monitoringEvent = monitoringService.serialize(is, MonitoringService.MONITORING_EVENT_TYPE);
            log.debug("---- monitoringEvent.toString = {}", monitoringEvent.toString());
            log.debug("---- monitoringEvent .deserialize = {}", monitoringService.deserialize(monitoringEvent));
        } catch (Exception e) {
            log.warn("fail to convert ", e);

        }

//        try {
//            //User user = userService.createUser(email, name);
//            User user = new User(email, name);
//            return Response.status(200).entity(user.toJson()).build();
//        } catch (Exception e) {
//            return Response.status(500).entity(Utils.convertErrorObjectToJson(500,
//                    "Fail to create user: name [" + name + "], " + "email [" + email + "] : " + e.getMessage())).build();
//        }

        return Response.status(200).entity("hello").build();

    }
}
