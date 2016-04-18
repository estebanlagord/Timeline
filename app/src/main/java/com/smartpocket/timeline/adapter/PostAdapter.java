package com.smartpocket.timeline.adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.smartpocket.timeline.R;
import com.smartpocket.timeline.activities.ViewImageActivity;
import com.smartpocket.timeline.backend.ServiceHandler;
import com.smartpocket.timeline.model.Post;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private final Activity activity;
    private List<Post> posts = new ArrayList<Post>();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
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
            mTime = (TextView) v.findViewById(R.id.post_time);
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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from the dataset at this position
        // - replace the contents of the view with that element
        final Post thePost = posts.get(position);

        holder.friendPicture.setProfileId(thePost.getFrom().getId());
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
                    holder.mImageSingle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.addCategory(Intent.CATEGORY_BROWSABLE);
                            intent.setData(Uri.parse(thePost.getSource()));
                            activity.startActivity(intent);
                        }
                    });
                }
                else if (thePost.getAttachments().getSharedLink() != null) {
                    // this is a Shared link
                    holder.mImageSingle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.addCategory(Intent.CATEGORY_BROWSABLE);
                            intent.setData(Uri.parse(thePost.getAttachments().getSharedLink()));
                            activity.startActivity(intent);
                        }
                    });
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

        // if this is the last item, retrieve more
        if (position+1 >= getItemCount()) {
            ServiceHandler.getInstance().getUserFeed(false);
        }
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        // clear previous values while the new ones haven't been downloaded
        holder.friendPicture.setProfileId(null);

        holder.mImageSingle.setVisibility(View.GONE);
        holder.mImageSingle.setImageBitmap(null);
        holder.mImageSingle.setAdjustViewBounds(true);
        holder.mImageSingle.setOnClickListener(null);

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