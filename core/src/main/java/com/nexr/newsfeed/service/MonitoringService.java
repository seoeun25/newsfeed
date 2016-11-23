package com.nexr.newsfeed.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexr.newsfeed.objs.MonitoringEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by seoeun on 11/22/16.
 */
public class MonitoringService {

    private static ObjectMapper jsonSerde = new ObjectMapper().enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

    public static TypeReference<MonitoringEvent> MONITORING_EVENT_TYPE = new TypeReference<MonitoringEvent>() {
    };

    public MonitoringEvent serialize(String jsonString, TypeReference<MonitoringEvent> responseFormat) throws Exception {
        return jsonSerde.readValue(jsonString, responseFormat);
    }

    public MonitoringEvent serialize(InputStream is, TypeReference<MonitoringEvent> responseFormat) throws IOException {
        MonitoringEvent result = jsonSerde.readValue(is, responseFormat);
        return result;
    }

    public String deserialize(MonitoringEvent event) throws JsonProcessingException{
        return jsonSerde.writeValueAsString(event);
    }

    public static Map<String, ?> mapOf(Object... args) {
        if (args.length == 0) {
            return new HashMap<>();
        }
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Arguments should be even");
        }
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < args.length; i = i + 2) {
            map.put(args[i].toString(), args[i + 1]);
        }
        return map;
    }



}
