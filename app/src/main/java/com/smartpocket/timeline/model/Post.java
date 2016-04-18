package com.smartpocket.timeline.model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class to be used by Gson to parse Facebook's JSON
 */
public class Post implements Serializable {
    private String id;
    private String story;
    private String message;
    private String created_time;
    private String picture;
    private String description;
    private String object_id;
    private String source;
    private User   from;
    private String type;
    private Attachments attachments;

    public Post() { }

    private String convertTimeFromUtc(String created_time) {
        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+0000");
        inFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date inDate;
        String result = created_time;
        try {
            inDate = inFormat.parse(created_time);
            DateFormat outFormat = DateFormat.getDateTimeInstance();
            outFormat.setTimeZone(TimeZone.getDefault());
            result = outFormat.format(inDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreated_time() {
        return convertTimeFromUtc(created_time);
    }

    public void setCreated_time(String created_time) {

        this.created_time = created_time;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getObject_id() {
        return object_id;
    }

    public void setObject_id(String object_id) {
        this.object_id = object_id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Attachments getAttachments() {
        return attachments;
    }

    public void setAttachments(Attachments attachments) {
        this.attachments = attachments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        return id.equals(post.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Post{" +
                "story='" + story + '\'' +
                '}';
    }
}
