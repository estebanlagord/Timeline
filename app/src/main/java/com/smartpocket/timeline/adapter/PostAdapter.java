package com.smartpocket.timeline.adapter;

import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.smartpocket.timeline.R;
import com.smartpocket.timeline.backend.DownloadImageTask;
import com.smartpocket.timeline.backend.ServiceHandler;
import com.smartpocket.timeline.model.Post;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> posts = new ArrayList<Post>();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mStory;
        private final TextView mTime;
        private final TextView mMessage;
        private final ProfilePictureView friendPicture;
        private final ImageView mImage1;
        private final ImageView mImage2;
        private final ImageView mImage3;
        private final ImageView mImage4;
        private final ImageView mImage5;
        private final ImageView[] imageViews;
        private final List<DownloadImageTask> downloadImageTasks;

        public ViewHolder(View v) {
            super(v);
            mStory = (TextView) v.findViewById(R.id.post_story);
            mTime = (TextView) v.findViewById(R.id.post_time);
            mMessage = (TextView) v.findViewById(R.id.post_message);
            friendPicture = (ProfilePictureView) v.findViewById(R.id.friendProfilePicture);
            mImage1 = (ImageView) v.findViewById(R.id.imageView1);
            mImage2 = (ImageView) v.findViewById(R.id.imageView2);
            mImage3 = (ImageView) v.findViewById(R.id.imageView3);
            mImage4 = (ImageView) v.findViewById(R.id.imageView4);
            mImage5 = (ImageView) v.findViewById(R.id.imageView5);
            imageViews = new ImageView[]{mImage1, mImage2, mImage3, mImage4, mImage5};
            for (int i=0; i<imageViews.length; i++) {
                imageViews[i].setVisibility(View.GONE);
            }

            downloadImageTasks = new ArrayList<DownloadImageTask>();
        }
    }

    public PostAdapter() { }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PostAdapter(List<Post> posts) {
        this.posts = posts;
    }

    public void clear() {
        this.posts.clear();
        notifyDataSetChanged();
    }

    public void addPosts(Post... posts) {
        int prevSize = this.posts.size();
        this.posts.addAll(Arrays.asList(posts));

        //TODO runOnUiThread ?
        notifyItemRangeInserted(prevSize, posts.length);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_post, parent, false);
        // set the view's size, margins, paddings and layout parameters
        v.setPadding(0,0,0,0);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Post thePost = posts.get(position);

        holder.mTime.setText(thePost.getCreated_time());
        holder.mMessage.setText(thePost.getMessage());
        holder.friendPicture.setProfileId(thePost.getFrom().getId());

        if (thePost.getStory() != null)
            holder.mStory.setText(thePost.getStory());
        else
            holder.mStory.setText(thePost.getFrom().getName());

        if (thePost.getAttachments() != null) {
            List<String> imageUrls = thePost.getAttachments().getPictureUrls();

            for (int i = 0; i < holder.imageViews.length && i < imageUrls.size(); i++) {
                DownloadImageTask task = new DownloadImageTask(holder.imageViews[i]);
                holder.downloadImageTasks.add(task);
                task.execute(imageUrls.get(i));
                holder.imageViews[i].setVisibility(View.VISIBLE);
            }
        }

        // if this is the last item, retrieve more
        if (position+1 >= getItemCount()) {
            ServiceHandler.getInstance().getUserFeed(false);
        }
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        // clear previous values while the new ones haven't been downloaded
        holder.friendPicture.setProfileId(null);
        // cancel pending image downloads from recycled view
        for (AsyncTask task : holder.downloadImageTasks) {
            task.cancel(true);
        }
        holder.downloadImageTasks.clear();

        for (int i=0; i<holder.imageViews.length; i++){
            holder.imageViews[i].setVisibility(View.GONE);
            holder.imageViews[i].setImageBitmap(null);
            holder.imageViews[i].setAdjustViewBounds(true);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return posts.size();
    }
}