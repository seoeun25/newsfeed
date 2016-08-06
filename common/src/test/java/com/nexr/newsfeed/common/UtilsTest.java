package com.nexr.newsfeed.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Assert;
import org.junit.Test;

public class UtilsTest {

    @Test
    public void test(){
        String s = "1462953610609";
        long time = Long.valueOf(s);
        String sTime = Utils.formatDateString(time);
        System.out.println(sTime);
    }

    @Test
    public void testUTC(){
        String s = "1462953610609";
        long time = Long.valueOf(s);
        String sTime = Utils.formatTime(time, "yyyy-MM-dd HH:mm:ss", "UTC");
        System.out.println(sTime);
    }

    @Test
    public void testErrorMessage() {
        String message = "hello azrael, you are in trouble";
        String errorJson = Utils.convertErrorObjectToJson(404, message);

        System.out.println(errorJson);
        ObjectMapper jsonDeserializer = new ObjectMapper();
        try {
            ErrorObject errorObject = jsonDeserializer.readValue(errorJson, ErrorObject.class);
            Assert.assertEquals(404, errorObject.getStatus());
            Assert.assertEquals(message, errorObject.getMessage());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testEmailMatch() {
        String a = "abc@email.com";
        String b = "abc.com";
        String c = "abc@";
        String d = "abc@a";

        Assert.assertEquals(true, Utils.validEmail(a));
        Assert.assertEquals(false, Utils.validEmail(b));
        Assert.assertEquals(false, Utils.validEmail(c));
        Assert.assertEquals(false, Utils.validEmail(d));
    }
}
