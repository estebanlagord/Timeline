package com.smartpocket.timeline2.activities;

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
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.smartpocket.timeline2.R;
import com.smartpocket.timeline2.adapter.PostAdapter;
import com.smartpocket.timeline2.backend.ServiceHandler;

import java.util.Arrays;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String USER_POSTS = "user_posts";
    private RecyclerView mRecyclerView;
    private NavigationView mNavigationView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private PostAdapter mAdapter;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // swipe to refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);

        //SwipeRefreshLayout indicator does not appear when the setRefreshing(true) is called before the SwipeRefreshLayout.onMeasure()
        //calling setProgressViewOffset() on the SwipeRefreshLayout  invalidates the circle view of the layout, causing SwipeRefreshLayout.onMeasure() to be called immediately.
        TypedValue typed_value = new TypedValue();
        getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
        mSwipeRefreshLayout.setRefreshing(true);

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
        loginButton.setReadPermissions(USER_POSTS);

        // Callback registration for Facebook's login button
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Snackbar.make(MainActivity.this.findViewById(android.R.id.content),
                        MainActivity.this.getApplicationContext().getString(R.string.login_success),
                        Snackbar.LENGTH_SHORT).show();

                if (loginResult.getAccessToken() != null) {
                    Set<String> deniedPermissions = loginResult.getRecentlyDeniedPermissions();
                    if (deniedPermissions.contains(USER_POSTS)) {
                        LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList(USER_POSTS));
                    }
                }
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
        accessTokenTracker.startTracking();

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                updateUserName(currentProfile);
            }
        };
        profileTracker.startTracking();

        // if the user is not logged in, display the Navigation Drawer
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null) {
            drawer.openDrawer(GravityCompat.START);
        }

        updateUserInfo();
    }

    private void updateUserName(Profile profile) {
        View headerView = mNavigationView.getHeaderView(0);
        TextView myUserNameView = (TextView) headerView.findViewById(R.id.myUserName);
        String newValue;

        if (profile != null) {
            newValue = profile.getName();
        } else {
            newValue = getResources().getString(R.string.please_sign_in);
        }

        myUserNameView.setText(newValue);
        Log.i("Timeline updateUserName", "Setting username to: " + newValue);
    }

    /**
     * Updates the picture, name, and timeline of the user that is logged in.
     */
    private void updateUserInfo(){
        View headerView = mNavigationView.getHeaderView(0);
        ProfilePictureView myPicture = (ProfilePictureView) headerView.findViewById(R.id.myProfilePicture);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken != null) {
            // User is logged in
            Set<String> permissions = accessToken.getPermissions();
            Log.i("Facebook permissions", accessToken.getPermissions().toString());
            if (!permissions.contains(USER_POSTS)) {
                Log.e("Facebook permissions", getString(R.string.error_permissions));
                Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.login_error), Snackbar.LENGTH_LONG).show();
            }

            myPicture.setProfileId(accessToken.getUserId());
            if (Profile.getCurrentProfile() != null) {
                updateUserName(Profile.getCurrentProfile());
            } else {
                Log.e("Timeline main", "Unable to set current user name. Facebook profile is null. Fetching new profile...");
                Profile.fetchProfileForCurrentAccessToken();
            }
        } else {
            // user is logged out
            myPicture.setProfileId(null);
            updateUserName(null);
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

        if (profileTracker != null)
            profileTracker.stopTracking();
    }
}
