package com.smartpocket.timeline2.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.smartpocket.timeline2.R;
import com.smartpocket.timeline2.activities.ViewImageActivity;
import com.smartpocket.timeline2.backend.ServiceHandler;
import com.smartpocket.timeline2.model.Post;
import com.smartpocket.timeline2.views.OpenLinkOnClickListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Adapter to load information (text, images, links) into the list's cards.
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private final Activity activity;
    private final List<Post> posts = new ArrayList<Post>();

    // Provides a reference to the views for each data item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mStory;
        private final TextView mTime;
        private final TextView mMessage;
        private final ProfilePictureView friendPicture;
        private final ImageView mImageSingle; // used to display single images
        private final ImageView mImage1;      // used to display multiple images
        private final ImageView mImage2;
        private final ImageView mImage3;
        private final ImageView mImage4;
        private final ImageView mImage5;
        private final ImageView[] imageViews;

        public ViewHolder(View v) {
            super(v);
            mStory = (TextView) v.findViewById(R.id.post_story);
            mTime  = (TextView) v.findViewById(R.id.post_time);
            mMessage = (TextView) v.findViewById(R.id.post_message);
            friendPicture = (ProfilePictureView) v.findViewById(R.id.friendProfilePicture);
            mImageSingle = (ImageView) v.findViewById(R.id.imageViewSingle);
            mImage1 = (ImageView) v.findViewById(R.id.imageView1);
            mImage2 = (ImageView) v.findViewById(R.id.imageView2);
            mImage3 = (ImageView) v.findViewById(R.id.imageView3);
            mImage4 = (ImageView) v.findViewById(R.id.imageView4);
            mImage5 = (ImageView) v.findViewById(R.id.imageView5);

            imageViews = new ImageView[]{mImage1, mImage2, mImage3, mImage4, mImage5};
            mImageSingle.setVisibility(View.GONE);
            for (ImageView view : imageViews) {
                view.setVisibility(View.GONE);
            }
        }
    }

    public PostAdapter(Activity activity) {
        this.activity = activity;
    }

    public void clear() {
        this.posts.clear();
        notifyDataSetChanged();
    }

    /**
     * Used to add posts to the adapter.
     */
    public void addPosts(Post... posts) {
        int prevSize = this.posts.size();
        this.posts.addAll(Arrays.asList(posts));

        notifyItemRangeInserted(prevSize, posts.length);
    }

    // Creates new views (invoked by the layout manager)
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new card view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_post, parent, false);

        v.setPadding(0,0,0,0);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from the dataset at this position
        // - replace the contents of the view with that element
        final Post thePost = posts.get(position);

        holder.friendPicture.setProfileId(thePost.getFrom().getId());  // Facebook's API will download the picture and place it in its view
        holder.mTime.setText(thePost.getCreated_time());

        if (thePost.getMessage() != null)
            holder.mMessage.setText(thePost.getMessage());
        else
            holder.mMessage.setText(thePost.getDescription());

        if (thePost.getStory() != null)
            holder.mStory.setText(thePost.getStory());
        else
            holder.mStory.setText(thePost.getFrom().getName());

        if (thePost.getAttachments() != null) {
            final List<String> imageUrls = thePost.getAttachments().getPictureUrls();

            // this is a single image
            if (imageUrls.size() == 1) {
                // load image from cache, or download if necessary
                Picasso.with(activity).load(imageUrls.get(0)).into(holder.mImageSingle);
                holder.mImageSingle.setAdjustViewBounds(true);
                holder.mImageSingle.setVisibility(View.VISIBLE);

                // check if this has a source link
                if (thePost.getSource() != null) {
                    holder.mImageSingle.setOnClickListener(
                            new OpenLinkOnClickListener(activity, thePost.getSource()));
                }
                else if (thePost.getAttachments().getSharedLink() != null) {
                    // this is a Shared link
                    holder.mImageSingle.setOnClickListener(
                            new OpenLinkOnClickListener(activity, thePost.getAttachments().getSharedLink()));
                } else {
                    // display a bigger image on click
                    holder.mImageSingle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ViewImageActivity.launch(activity, holder.mImageSingle, imageUrls.get(0));
                        }
                    });
                }
            } else {
                // this post has several images
                for (int i = 0; i < holder.imageViews.length && i < imageUrls.size(); i++) {
                    // load image from cache, or download if necessary
                    final String url = imageUrls.get(i);
                    final ImageView view = holder.imageViews[i];

                    Picasso.with(activity).load(url).fit().centerCrop().into(view);
                    view.setVisibility(View.VISIBLE);
                    // display a bigger image on click
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ViewImageActivity.launch(activity, view, url);
                        }
                    });
                }
            }
        }

        // if this is the last item in the adapter, download more from the user's timeline feed
        if (position+1 >= getItemCount()) {
            ServiceHandler.getInstance().getUserFeed(false);
        }
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        // clear previous values while the new ones haven't been downloaded
        holder.friendPicture.setProfileId(null);

        // cancel pending downloads
        Picasso.with(activity).cancelRequest(holder.mImageSingle);
        holder.mImageSingle.setVisibility(View.GONE);
        holder.mImageSingle.setImageBitmap(null);
        holder.mImageSingle.setAdjustViewBounds(true);
        holder.mImageSingle.setOnClickListener(null);

        for (ImageView view : holder.imageViews) {
            Picasso.with(activity).cancelRequest(view);
            view.setVisibility(View.GONE);
            view.setImageBitmap(null);
            view.setAdjustViewBounds(true);
            view.setOnClickListener(null);
        }
    }

    // Returns the size of the dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return posts.size();
    }
}