package com.nexr.newsfeed.entity;

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
import java.io.IOException;
import java.util.Date;

@Entity
@NamedQueries({

        @NamedQuery(name = "GET_BYID", query = "select a.id, a.name, a.email, a.lastViewTime, a.createdTime " +
                "from User a where a.id = :id "),
        @NamedQuery(name = "GET_USER_ALL", query = "select a.id, a.name, a.email, a.lastViewTime, a.createdTime from User a "),
        @NamedQuery(name = "UPDATE_LASTVIEW_TIME", query = "update User a set a.lastViewTime = :lastViewTime where a.id = :id")

})
@Table(name = "user")
//@XmlRootElement
public class User {

    @Id
    @Index
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonProperty("id")
    private long id;

    @Index
    @Column(name = "email", length = 255, unique = true)
    @JsonProperty("email")
    private String email;

    @Column(name = "name", length = 2048)
    @JsonProperty("name")
    private String name;

    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("createdTime")
    private Date createdTime;


    // TODO lasteViewActivityTime가  맞을 듯.
    @Column(name = "last_view_time")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("lastViewTime")
    private Date lastViewTime;

    public User() {

    }

    public User(@JsonProperty("email") String email, @JsonProperty("name") String name) {
        this.email = email;
        this.name = name;
        this.createdTime = new Date(System.currentTimeMillis());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getLastViewTime() {
        return this.lastViewTime;
    }

    public void setLastViewTime(Date lastViewTime) {
        this.lastViewTime = lastViewTime;
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
        builder.append("name=" + name + ",");
        builder.append("email=" + email + ",");
        builder.append("lastViewTime=" + (createdTime == null ? null : createdTime.getTime()) + ",");
        builder.append("createdTime=" + (createdTime == null ? null : createdTime.getTime()));
        return builder.toString();
    }

}
