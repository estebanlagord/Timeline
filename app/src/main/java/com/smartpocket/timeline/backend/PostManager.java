package com.smartpocket.timeline.backend;

import com.smartpocket.timeline.model.Post;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Deprecated
public class PostManager {
    private static PostManager instance;
    private List<Post> posts = new ArrayList<Post>();

    private PostManager() { super(); };

    public static PostManager getInstance()
    {
        if(instance == null)
        {
            instance = new PostManager();
        }
        return instance;
    }

    public int getSize() {
        return posts.size();
    }

    public Post get(int i) {
        // TODO check if it exists
        return posts.get(i);
    }

    public void add(Post... posts) {
        this.posts.addAll(Arrays.asList(posts));
    }

}
