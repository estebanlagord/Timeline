package com.smartpocket.timeline.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.smartpocket.timeline.R;
import com.smartpocket.timeline.adapter.PostAdapter;
import com.smartpocket.timeline.backend.ServiceHandler;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private NavigationView mNavigationView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private PostAdapter mAdapter;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // swipe to refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContents();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.timeline_recycler_view);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter for RecyclerView
        mAdapter = new PostAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        ServiceHandler.getInstance().initialize(this, mAdapter);

        // Facebook setup
        callbackManager = CallbackManager.Factory.create();
        AppEventsLogger.activateApp(getApplication());

        LoginButton loginButton = (LoginButton) mNavigationView.getMenu().getItem(0).getActionView().findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_posts");

        // Callback registration for Facebook's login button
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Snackbar.make(MainActivity.this.findViewById(android.R.id.content),
                        MainActivity.this.getApplicationContext().getString(R.string.login_success),
                        Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Snackbar.make(MainActivity.this.findViewById(android.R.id.content),
                        MainActivity.this.getApplicationContext().getString(R.string.login_cancel),
                        Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Snackbar.make(MainActivity.this.findViewById(android.R.id.content),
                        MainActivity.this.getApplicationContext().getString(R.string.login_error),
                        Snackbar.LENGTH_SHORT).show();
            }
        });

        // Track user logging in/out of Facebook
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                updateUserInfo();
            }
        };

        // if the user is not logged in, display the Navigation Drawer
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null) {
            drawer.openDrawer(GravityCompat.START);
        }

        updateUserInfo();
    }

    /**
     * Updates the picture, name, and timeline of the user that is logged in.
     */
    private void updateUserInfo(){
        View headerView = mNavigationView.getHeaderView(0);
        ProfilePictureView myPicture = (ProfilePictureView) headerView.findViewById(R.id.myProfilePicture);
        TextView myUserNameView = (TextView) headerView.findViewById(R.id.myUserName);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken != null) {
            // User is logged in
            myPicture.setProfileId(accessToken.getUserId());
            if (Profile.getCurrentProfile() != null) {
                myUserNameView.setText(Profile.getCurrentProfile().getName());
            }
        } else {
            // user is logged out
            myPicture.setProfileId(null);
            myUserNameView.setText(getResources().getString(R.string.please_sign_in));
        }

        invalidateOptionsMenu();
        refreshContents();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar
        getMenuInflater().inflate(R.menu.menu_main, menu);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null) {
            // user is logged out, disable refresh
            menu.findItem(R.id.action_refresh).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            refreshContents();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Triggers a refresh of the user's timeline.
     */
    private void refreshContents() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        mAdapter.clear();

        if (! isOnline()) {
            Snackbar.make(mRecyclerView, getApplicationContext().getString(R.string.no_connection_error), Snackbar.LENGTH_LONG).show();
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        if (accessToken != null) {
            // user is logged in
            mSwipeRefreshLayout.setRefreshing(true);

            ServiceHandler.getInstance().getUserFeed(true);
            // after the user feed is refreshed, disable the refresh state for the swipe refresh layout
            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }

    /**
     * Checks the device's online status.
     * @return true when an Internet connection is available.
     */
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (accessTokenTracker != null)
            accessTokenTracker.stopTracking();
    }
}
