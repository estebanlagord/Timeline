package com.smartpocket.timeline.backend;

import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.gson.Gson;
import com.smartpocket.timeline.adapter.PostAdapter;
import com.smartpocket.timeline.model.Post;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServiceHandler {
    private static ServiceHandler instance;
    private GraphRequest nextRequest;
    private PostAdapter adapter;

    private ServiceHandler() { super(); };

    public static ServiceHandler getInstance()
    {
        if(instance == null)
        {
            instance = new ServiceHandler();
        }
        return instance;
    }

    public PostAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(PostAdapter adapter) {
        this.adapter = adapter;
    }

    public void getUserFeed(boolean retrieveFromStart) {
        GraphRequest request;
        if (nextRequest == null || retrieveFromStart) {
            request = GraphRequest.newGraphPathRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/me/feed", new MyGraphRequestCallback());
        } else {
            request = nextRequest;
            request.setCallback(new MyGraphRequestCallback());
        }
        /*GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/feed",
                //"/me/feed?fields=story,message,created_time,picture,description,object_id,source",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        JSONObject object = response.getJSONObject();

                        nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);

                        try {
                            JSONArray dataArray = object.getJSONArray("data");
                            Log.d("JSON data: ",""+ dataArray);

                            Gson gson = new Gson();
                            Post[] posts = gson.fromJson(dataArray.toString(), Post[].class);
                            Log.d("ServiceScheduler: ", "Parsed " + posts.length + " posts");
                            adapter.addPosts(posts);

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });*/



        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,story,message,created_time,picture,description,object_id,source,from{picture},type,attachments");
        request.setParameters(parameters);
        request.executeAsync();
    }

    class MyGraphRequestCallback implements GraphRequest.Callback {
        @Override
        public void onCompleted(GraphResponse response) {
            JSONObject object = response.getJSONObject();

            nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);

            try {
                JSONArray dataArray = object.getJSONArray("data");
                Log.d("JSON data: ",""+ dataArray);

                Gson gson = new Gson();
                Post[] posts = gson.fromJson(dataArray.toString(), Post[].class);
                Log.d("ServiceScheduler: ", "Parsed " + posts.length + " posts");
                getAdapter().addPosts(posts);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
