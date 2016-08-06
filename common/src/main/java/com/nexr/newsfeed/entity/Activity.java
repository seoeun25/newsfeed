package com.nexr.newsfeed.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@NamedQueries({

        @NamedQuery(name = "GET_BYFOLLOWING_FORWARD_ASC", query = "select a.id, a.userId, a.message, a.createdTime " +
                "from Activity a where a.createdTime >= :createdTime and a.userId in :followings order by a.createdTime asc"),
        @NamedQuery(name = "GET_BYFOLLOWING_FORWARD_DESC", query = "select a.id, a.userId, a.message, a.createdTime " +
                "from Activity a where a.createdTime >= :createdTime and a.userId in :followings order by a.createdTime desc"),
        @NamedQuery(name = "GET_BYFOLLOWING_BACKWARD_ASC", query = "select a.id, a.userId, a.message, a.createdTime " +
                "from Activity a where a.createdTime <= :createdTime and a.userId in :followings order by a.createdTime asc"),
        @NamedQuery(name = "GET_BYFOLLOWING_BACKWARD_DESC", query = "select a.id, a.userId, a.message, a.createdTime " +
                "from Activity a where a.createdTime <= :createdTime and a.userId in :followings order by a.createdTime desc"),
        @NamedQuery(name = "GET_ALL_ACTIVITIES_ASC", query = "select a.id, a.userId, a.message, a.createdTime " +
                "from Activity a where a.createdTime >= :createdTime order by a.createdTime asc"),
        @NamedQuery(name = "GET_ALL_ACTIVITIES_DESC", query = "select a.id, a.userId, a.message, a.createdTime " +
                "from Activity a where a.createdTime >= :createdTime order by a.createdTime desc")

})
@Table(name = "activity")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonProperty("id")
    private long id;

    @Column(name = "user_id")
    @JsonProperty("userId")
    private long userId;

    @Column(name = "message", length = 20480)
    @JsonProperty("message")
    private String message;

    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("createdTime")
    private Date createdTime;

    public Activity() {
        this.createdTime = new Timestamp(System.currentTimeMillis());
    }

    public Activity(@JsonProperty("userId") long userId, @JsonProperty("message") String message) {
        this.userId = userId;
        this.message = message;
        this.createdTime = new Date();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String toJson() throws IOException {
        return (new ObjectMapper()).writeValueAsString(this);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("id=" + id + ",");
        builder.append("userId=" + userId + ",");
        builder.append("message=" + message + ",");
        builder.append("createdTime=" + (createdTime == null ? null : createdTime.getTime()));
        return builder.toString();
    }

}
