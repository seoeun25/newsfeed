package com.nexr.newsfeed.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.common.ErrorObject;
import com.nexr.newsfeed.entity.Activity;
import com.nexr.newsfeed.entity.Friend;
import com.nexr.newsfeed.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsfeedWebClient {

    private static Logger log = LoggerFactory.getLogger(NewsfeedWebClient.class);

    private static TypeReference<User> USER_TYPE = new TypeReference<User>() {
    };

    private static TypeReference<List<User>> USER_LIST_TYPE = new TypeReference<List<User>>() {
    };

    private static TypeReference<Friend> FRIEND_TYPE = new TypeReference<Friend>() {
    };

    private static TypeReference<List<Long>> FOLLOWING_LIST_TYPE = new TypeReference<List<Long>>() {
    };

    private static TypeReference<Activity> ACTIVITY_TYPE = new TypeReference<Activity>() {
    };

    private  static TypeReference<List<Activity>> ACTIVITY_LIST_TYPE = new TypeReference<List<Activity>>() {
    };

    private static TypeReference<String> ID_TYPE = new TypeReference<String>() {
    };

    private static TypeReference<String> STRING_TYPE = new TypeReference<String>() {
    };

    private static ObjectMapper jsonDeserializer = new ObjectMapper();

    private final String baseUrl;

    public NewsfeedWebClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public User createUser(String email, String name) throws IOException, NewsfeedException {
        String path = String.format("/users");
        String formData = "name=" + name + "&email=" + email;
        User user = httpRequest(path, "POST", formData.getBytes(), new HashMap<String, String>(), USER_TYPE);
        return user;
    }

    public User getUser(long id) throws IOException, NewsfeedException {
        String path = String.format("/users/%s", new Object[]{id});
        User user = httpRequest(path, "GET", null, new HashMap<String, String>(), USER_TYPE);
        return user;
    }

    public User updateLastviewTimeOf(long id, long lastviewTime) throws IOException, NewsfeedException {
        String path = String.format("/users/%s", new Object[]{id});
        String formData = "lastviewTime="+lastviewTime;
        User userUpdated = httpRequest(path, "PUT", formData.getBytes(), new HashMap<String, String>(), USER_TYPE);
        return userUpdated;
    }

    public List<User> getUsers() throws IOException, NewsfeedException {
        String path = String.format("/users");
        List<User> users = httpRequest(path, "GET", null, new HashMap<String, String>(), USER_LIST_TYPE);
        return users;
    }

    public Friend follow(long userId, long followingId) throws IOException, NewsfeedException {
        String path = String.format("/followings");
        String formData = "userId=" + userId + "&followingId=" + followingId;
        Friend friend = httpRequest(path, "POST", formData.getBytes(), new HashMap<String, String>(), FRIEND_TYPE);
        return friend;
    }

    public List<Long> getFollowing(long userId) throws IOException, NewsfeedException {
        String path = String.format("/followings/%s", new Object[]{userId});
        List<Long> ids = httpRequest(path, "GET", null, new HashMap<String, String>(), FOLLOWING_LIST_TYPE);
        return ids;
    }

    public Activity postMessage(long userId, String message) throws IOException, NewsfeedException {
        String path = String.format("/activities");
        String formData = "userId=" + userId + "&message=" + message;
        Activity activity = httpRequest(path, "POST", formData.getBytes(), new HashMap<String, String>(), ACTIVITY_TYPE);
        return activity;
    }

    public List<Activity> getFeeds(long userId) throws IOException, NewsfeedException {
        String path = String.format("/feeds/%s", new Object[]{userId});
        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(userId));
        List<Activity> activities = httpRequest(path, "GET", null, params, ACTIVITY_LIST_TYPE);
        return activities;
    }

    public List<Activity> getFeeds(long userId, long basetime, int maxResult, boolean asc)
            throws IOException, NewsfeedException {
        String path = String.format("/feeds/%s", new Object[]{userId});
        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(userId));
        params.put("basetime", String.valueOf(basetime));
        params.put("maxResult", String.valueOf(maxResult));
        params.put("asc", String.valueOf(asc));
        path = path + prepareParam(params);
        List<Activity> activities = httpRequest(path, "GET", null, new HashMap<String, String>(), ACTIVITY_LIST_TYPE);
        return activities;
    }

    public List<Activity> getFeedsAll(long basetime, int maxResult, boolean asc)
            throws IOException, NewsfeedException {
        String path = String.format("/feeds");
        Map<String, String> params = new HashMap<>();
        params.put("basetime", String.valueOf(basetime));
        params.put("maxResult", String.valueOf(maxResult));
        params.put("asc", String.valueOf(asc));
        path = path + prepareParam(params);
        List<Activity> activities = httpRequest(path, "GET", null, new HashMap<String, String>(), ACTIVITY_LIST_TYPE);
        return activities;
    }

    public String prepareParam(Map<String,String> parameters) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (parameters.size() > 0) {
            String separator = "?";
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                if (param.getValue() != null) {
                    sb.append(separator).append(URLEncoder.encode(param.getKey(), "UTF-8")).append("=").append(
                            URLEncoder.encode(param.getValue(), "UTF-8"));
                    separator = "&";
                }
            }
        }
        return sb.toString();
    }


    private <T> T httpRequest(String path, String method,
                              byte[] requestBodyData, Map<String, String> requestProperties,
                              TypeReference<T> responseFormat) throws IOException, NewsfeedException {
        for (int i = 0, n = 3; i < n; i++) {
            try {
                return sendHttpRequest(baseUrl + path, method, requestBodyData, requestProperties, responseFormat);
            } catch (IOException e) {
                log.warn("Fail to sendHttpRequest {}, try {}", baseUrl, n);
                if (i == n - 1) {
                    throw e; // try n(3) times.
                }
            }
        }
        throw new IOException("Internal HTTP retry error"); // Can't get here
    }

    private <T> T sendHttpRequest(String baseUrl, String method, byte[] requestBodyData,
                                  Map<String, String> requestProperties,
                                  TypeReference<T> responseFormat)
            throws IOException, NewsfeedException {
        log.debug(String.format("Sending %s with input %s to %s",
                method, requestBodyData == null ? "null" : new String(requestBodyData),
                baseUrl));

        HttpURLConnection connection = null;
        try {
            URL url = new URL(baseUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);

            connection.setDoInput(true);

            for (Map.Entry<String, String> entry : requestProperties.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            connection.setUseCaches(false);
            if (requestBodyData != null) {
                connection.setDoOutput(true);
                OutputStream os = null;
                try {
                    os = connection.getOutputStream();
                    os.write(requestBodyData);
                    os.flush();
                } catch (IOException e) {
                    log.error("Failed to send HTTP request to endpoint: " + url, e);
                    throw e;
                } finally {
                    if (os != null) os.close();
                }
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream is = connection.getInputStream();
                T result = jsonDeserializer.readValue(is, responseFormat);
                is.close();
                return result;
            } else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                return null;
            } else {
                InputStream es = connection.getErrorStream();
                ErrorObject errorObj;
                try {
                    errorObj = jsonDeserializer.readValue(es, ErrorObject.class);
                } catch (JsonProcessingException e) {
                    errorObj = new ErrorObject(responseCode, e.getMessage());
                }
                es.close();
                throw new NewsfeedException(String.format("[%s, %s]", new Object[]{errorObj.getStatus(), errorObj.getMessage()}));
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


}
