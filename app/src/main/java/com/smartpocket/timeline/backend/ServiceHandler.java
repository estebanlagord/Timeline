package com.smartpocket.timeline.backend;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.gson.Gson;
import com.smartpocket.timeline.R;
import com.smartpocket.timeline.adapter.PostAdapter;
import com.smartpocket.timeline.model.Post;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Used to retrieve feed information (plain text) from a user's timeline in Facebook.
 */
public class ServiceHandler {
    private static ServiceHandler instance;
    private GraphRequest nextRequest;
    private PostAdapter adapter;
    private Activity activity;

    private ServiceHandler() {
        super();
    }

    public static ServiceHandler getInstance()
    {
        if(instance == null)
        {
            instance = new ServiceHandler();
        }
        return instance;
    }

    public void initialize(Activity activity, PostAdapter adapter) {
        this.adapter = adapter;
        this.activity = activity;
    }

    /**
     * Retrieves a user's feed information from Facebook.
     * @param retrieveFromStart when true, this will retrieve the contents of the feed from the beginning.
     *                          When false, it will retrieve the current page instead.
     */
    public void getUserFeed(boolean retrieveFromStart) {
        GraphRequest request;

        if (adapter == null || activity == null)
            throw new IllegalStateException("Service handler must be initialized before calling this method");

        if (nextRequest == null || retrieveFromStart) {
            request = GraphRequest.newGraphPathRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/me/feed", new MyGraphRequestCallback());
        } else {
            request = nextRequest;
            request.setCallback(new MyGraphRequestCallback());
        }

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,story,message,created_time,picture,description,object_id,source,from{name,picture},type,attachments");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private class MyGraphRequestCallback implements GraphRequest.Callback {
        @Override
        public void onCompleted(GraphResponse response) {
            JSONObject object = response.getJSONObject();

            // the results are paginated by Facebook
            nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);

            try {
                JSONArray dataArray = object.getJSONArray("data");
                Log.d("JSON data: ",""+ dataArray);

                Gson gson = new Gson();
                Post[] posts = gson.fromJson(dataArray.toString(), Post[].class);
                Log.d("ServiceScheduler: ", "Parsed " + posts.length + " posts");
                adapter.addPosts(posts);

            } catch (Exception e) {
                Log.e("ServiceHandler", "Unable to retrieve feed content from Facebook", e);
                Snackbar.make(activity.findViewById(android.R.id.content),
                        activity.getApplicationContext().getString(R.string.feed_download_error),
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
