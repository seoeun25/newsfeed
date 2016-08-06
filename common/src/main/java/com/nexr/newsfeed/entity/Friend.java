package com.nexr.newsfeed.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.openjpa.persistence.jdbc.Index;

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
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@NamedQueries({
        @NamedQuery(name = "GET_FOLLOWING_ALL", query = "select a.userId, a.followingId, a.createdTime " +
                "from Friend a order by a.userId"),
        @NamedQuery(name = "GET_FOLLOWINGS", query = "select a.userId, a.followingId, a.createdTime " +
                "from Friend a where a.userId = :userId "),
        @NamedQuery(name = "GET_FOLLOWING", query = "select a.userId, a.followingId, a.createdTime " +
                "from Friend a where a.userId = :userId and a.followingId = :followingId")

})
@Table(name = "friend")
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id")
    @JsonProperty("id")
    private long id;

    @Index
    @Column(name = "user_id")
    @JsonProperty("userId")
    private long userId;

    @Index
    @Column(name = "following_id")
    @JsonProperty("followingId")
    private long followingId;

    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("createdTime")
    private Date createdTime;

    public Friend() {
        this.createdTime = new Timestamp(System.currentTimeMillis());
    }

    public Friend(@JsonProperty("userId") long userId, @JsonProperty("followingId") long followingId) {
        this.userId = userId;
        this.followingId = followingId;
        this.createdTime = new Date(System.currentTimeMillis());
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

    public long getFollowingId() {
        return followingId;
    }

    public void setFollowingId(long followingId) {
        this.followingId = followingId;
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
        builder.append("followingId=" + followingId + ",");
        builder.append("createdTime=" + (createdTime == null ? null : createdTime.getTime()));
        return builder.toString();
    }

}
