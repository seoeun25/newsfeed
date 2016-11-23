package com.nexr.newsfeed.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.entity.Friend;
import com.nexr.newsfeed.entity.User;
import com.nexr.newsfeed.jpa.JPAService;
import com.nexr.newsfeed.objs.MonitoringEvent;
import com.nexr.newsfeed.objs.MyObject;
import com.nexr.newsfeed.server.NewsfeedServer;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by seoeun on 11/23/16.
 */
public class MonitoringServiceTest {

    private static Logger log = LoggerFactory.getLogger(MonitoringServiceTest.class);

    private static MonitoringService monitoringService;




    @BeforeClass
    public static void setupClass() {
        try {
            System.setProperty("persistenceUnit", "newsfeed-test-hsql");
            Injector injector = Guice.createInjector(new NewsfeedServer());

            Thread.sleep(1000);
            monitoringService = injector.getInstance(MonitoringService.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDown() {
    }

    @Test
    public void testSerde() {

        String jsonString = "{\"timestamp\" : \"1479433943169\",\"requestId\" : \"lezhin-request-id\",\"message\" : \"hello, seoeun\"}";

        try {
            MonitoringEvent event = monitoringService.serialize(jsonString, MonitoringService.MONITORING_EVENT_TYPE);
            log.info("---- event, event.toString={}", event.toString());
            log.info("---- event, event deserialize={}", monitoringService.deserialize(event));



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testSerde2() {

        try {
            String message = "This is gagamel";
            long timestamp = System.currentTimeMillis();
            MonitoringEvent event = new MonitoringEvent.Builder(MonitoringEvent.Service.LOGIN, message)
                    .setRequestId("request-id-" + toString())
                    .setData((Map<String, Object>) MonitoringService.mapOf("key-1", "value-1", "key-2", "value-2")).build();


            log.info("---- event, event.toString={}", event.toString());
            log.info("---- envet, payload = {}", event.getPayload().toString());
            log.info("---- event, event deserialize={}", monitoringService.deserialize(event));



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testSerde3() {

        try {
            TypeReference<MyObject> MY_OBJECT_TYPE = new TypeReference<MyObject>() {
            };

            MyObject myObject = new MyObject();
            myObject.setId("seoeun01");
            myObject.setData((Map<String, Object>) MonitoringService.mapOf("key-1", "value-1", "key-2", "value-2"));

            ObjectMapper objectMapper = new ObjectMapper();
//            TypeFactory typeFactory = objectMapper.getTypeFactory();
//            MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, Object.class);
//            HashMap<String, Theme> map = mapper.readValue(json, mapType);


            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(myObject);
            System.out.println("---- : " + jsonString);
            System.out.println("---- :: " + objectMapper.writeValueAsString(myObject));

            String abc = "{\"id\":\"id=0\",\"data\":{\"key-1\":\"value-1\",\"key-2\":\"value-2\"}}";


            MyObject converted = objectMapper.readValue(abc, MY_OBJECT_TYPE);
            System.out.println(converted.getId());
            Map<String,Object> helloMap = converted.getData();
            for(Map.Entry<String,Object> entry: helloMap.entrySet()) {
                System.out.println(entry.getKey() +  " = " + entry.getValue());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testSerde4() {

        try {
            TypeReference<MyObject> MY_OBJECT_TYPE = new TypeReference<MyObject>() {
            };

            MyObject myObject = new MyObject();
            myObject.setId("seoeun01");
            myObject.setData((Map<String, Object>) MonitoringService.mapOf("key-1", "value-1", "key-2", "value-2"));

            String pojoAsString = PojoMapper.toJson(myObject, true) ;

            System.out.println("---- : " + pojoAsString);

            String abc = "{\"id\":\"id=0\",\"data\":{\"key-1\":\"value-1\",\"key-2\":\"value-2\"}}";


            MyObject converted = PojoMapper.fromJson(abc, MyObject.class);
            System.out.println(converted.getId());
            Map<String,Object> helloMap = converted.getData();
            for(Map.Entry<String,Object> entry: helloMap.entrySet()) {
                System.out.println(entry.getKey() +  " = " + entry.getValue());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
