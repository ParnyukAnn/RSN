package com.aparnyuk.rsn;

import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.aparnyuk.rsn.adapter.TabsFragmentAdapter;
import com.aparnyuk.rsn.fragment.dialog.CallDialog;
import com.aparnyuk.rsn.fragment.dialog.NoteDialog;
import com.aparnyuk.rsn.fragment.dialog.RemindDialog;
import com.aparnyuk.rsn.login.CreateAccountActivity;
import com.aparnyuk.rsn.login.LoginActivity;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.ui.auth.core.AuthProviderType;
import com.firebase.ui.auth.core.FirebaseLoginBaseActivity;
import com.firebase.ui.auth.core.FirebaseLoginError;

public class MainActivity extends FirebaseLoginBaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "MainActivity";

    Toolbar toolbar;
    ViewPager viewPager;
    FloatingActionButton fab;
    TabLayout tabLayout;
    NavigationView navigationView;
    TabsFragmentAdapter adapter;

    /* A reference to the Firebase */
    private Firebase mRef;
    private String mName;

    /* Preferences variables. Use for the checking of the first run*/
    private SharedPreferences prefs = null;
    public static final String APP_PREFERENCES = "com.aparnyuk.rsn";

    /* Current tab position*/
    private int position;

    /* Input data dialogs */
    CallDialog callDialog;
    RemindDialog remindDialog;
    NoteDialog noteDialog;
    //SmsDialog smsDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Firebase.setAndroidContext(this);

        mRef = new Firebase(Constants.FIREBASE_URL);
        prefs = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);

        initToolbar();
        initTabs();
        initNavigationDrover();
        initFloatingButton();

        position = 0;
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initTabs() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        adapter = new TabsFragmentAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);


        // !! add animation to floating button and change tabs color
        // state 0 = nothing happen, state 1 = begining scrolling, state 2 = stop at selected tab.

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int state = 0;
            private boolean isFloatButtonHidden = false;
            // private int position = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (!isFloatButtonHidden && state == 1 && positionOffset != 0.0) {
                    isFloatButtonHidden = true;
                    //hide floating button
                    swappingAway();
                }
            }

            @Override
            public void onPageSelected(int pos) {
                //reset floating
                //this.position = position;
                position = pos;
                if (state == 2) {
                    //have end in selected tab
                    isFloatButtonHidden = false;
                    selectedTabs(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //state 0 = nothing happen, state 1 = begining scrolling, state 2 = stop at selected tab.
                this.state = state;
                if (state == 0) {
                    isFloatButtonHidden = false;
                } else if (state == 2 && isFloatButtonHidden) {
                    //this only happen if user is swapping but swap back to current tab (cancel to change tab)
                    selectedTabs(position);
                }
            }
        });
        //!!

    }

    //!!
    private void swappingAway() {
        fab.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.pop_down);
        fab.startAnimation(animation);
    }

    /* Add button animation, change button and tabs color and put icons on button */
    private void selectedTabs(int tab) {
        fab.show();
        //a bit animation of popping up.
        fab.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.pop_up);
        fab.startAnimation(animation);
        int selectedColor;
        // int defaultColor = getResources().getColor(R.color.colorPrimaryLight);
        int tabColor;
        switch (tab) {
            case (Constants.TAB_ONE_SMS): {
                //  selectedColor = (getResources().getColor(R.color.colorFab1));
                //  tabColor = (getResources().getColor(R.color.colorTabLine1));
                fab.setImageResource(android.R.drawable.ic_dialog_email);
                // fab.setImageResource(R.drawable.ic_email_white_36dp);
                // fab.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                // tabLayout.setSelectedTabIndicatorColor(tabColor);
                // tabLayout.setTabTextColors(defaultColor, tabColor);
                break;
            }
            case (Constants.TAB_TWO_CALL): {
                //  selectedColor = getResources().getColor(R.color.colorFab2);
                //  tabColor = (getResources().getColor(R.color.colorTabLine2));
                fab.setImageResource(R.drawable.ic_phone_white_36dp);
                // fab.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                // tabLayout.setSelectedTabIndicatorColor(tabColor);
                // tabLayout.setTabTextColors(defaultColor, tabColor);
                break;
            }
            case (Constants.TAB_THREE_REMIND): {
                //  selectedColor = getResources().getColor(R.color.colorFab3);
                //  tabColor = (getResources().getColor(R.color.colorTabLine3));
                fab.setImageResource(R.drawable.ic_alarm_check_white_36dp);
                //  fab.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                //  tabLayout.setSelectedTabIndicatorColor(tabColor);
                //   tabLayout.setTabTextColors(defaultColor, tabColor);
                break;
            }
            case (Constants.TAB_FOUR_NOTE): {
                //  selectedColor = getResources().getColor(R.color.colorFab4);
                //  tabColor = (getResources().getColor(R.color.colorTabLine4));
                fab.setImageResource(R.drawable.ic_pen_white_36dp);
                //   fab.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                //  tabLayout.setSelectedTabIndicatorColor(tabColor);
                //   tabLayout.setTabTextColors(defaultColor, tabColor);
                break;
            }
        }
    }
//!!

    private void initNavigationDrover() {
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.view_navigation_open, R.string.view_navigation_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);

        changeAuthItem();
        // navigationView.inflateMenu();
    }

    private void initFloatingButton() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getAuth()== null) {
                    showFirebaseLoginPrompt();
                } else {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    switch (position) {
                        case (Constants.TAB_ONE_SMS): {
                            //smsDialog = new SmsDialog();
                            //smsDialog.show(getFragmentManager(), "CreateDialog4");
                            break;
                        }
                        case (Constants.TAB_TWO_CALL): {
                            callDialog = new CallDialog();
                            callDialog.show(fragmentManager, "CreateDialog3");

                            break;
                        }
                        case (Constants.TAB_THREE_REMIND): {
                            remindDialog = new RemindDialog();
                            remindDialog.show(fragmentManager, "CreateDialog2");
                            break;
                        }
                        case (Constants.TAB_FOUR_NOTE): {
                            noteDialog = new NoteDialog();
                            noteDialog.show(fragmentManager, "CreateDialog1");
                            break;
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        menu.findItem(R.id.action_login).setVisible(getAuth() == null);
//        menu.findItem(R.id.action_logout).setVisible(getAuth() != null);
        return true;
    }

    /* Work with Toolbar menu */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        switch (item.getItemId()) {
//            case R.id.action_login:
//                this.showFirebaseLoginPrompt();
//                return true;
//            case R.id.action_logout:
//                this.logout();
//                return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    /*  Work with NavigationDrawer menu */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case (R.id.nav_sign_in): {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
            }
            case (R.id.nav_sign_out): {
                this.logout();
                break;
            }
            case (R.id.nav_calendar): {
                break;
            }
            case (R.id.nav_search): {
                break;
            }
            case (R.id.nav_history): {
                break;
            }
            case (R.id.nav_settings): {
                break;
            }
        }
        // close NavigationDrawer after choosing menu item
        changeAuthItem();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /* Close NavigationDrawer when put back button */
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
    protected void onStart() {
        super.onStart();
        //   setEnabledAuthProvider(AuthProviderType.FACEBOOK);
        //   setEnabledAuthProvider(AuthProviderType.GOOGLE);
        setEnabledAuthProvider(AuthProviderType.PASSWORD);
    }

    @Override
    public void onFirebaseLoggedIn(AuthData authData) {
        Log.i(TAG, "Logged in to " + authData.getProvider());
        switch (authData.getProvider()) {
            case "password":
                mName = (String) authData.getProviderData().get("email");
                break;
            default:
                mName = (String) authData.getProviderData().get("displayName");
                break;
        }

        invalidateOptionsMenu();
        changeAuthItem();
        // mRecycleViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFirebaseLoggedOut() {
        Log.i(TAG, "Logged out");
        mName = "";
        invalidateOptionsMenu();
        changeAuthItem();
        // case tab ->
        // mRecycleViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFirebaseLoginProviderError(FirebaseLoginError firebaseError) {
        Log.e(TAG, "Login provider error: " + firebaseError.toString());
        resetFirebaseLoginPrompt();
    }

    @Override
    public void onFirebaseLoginUserError(FirebaseLoginError firebaseError) {
        Log.e(TAG, "Login user error: " + firebaseError.toString());
        resetFirebaseLoginPrompt();
    }

    @Override
    public Firebase getFirebaseRef() {
        return mRef;
    }

    /* The first launch of the application. Ask user login. */
    @Override
    protected void onResume() {
        super.onResume();
        if (prefs.getBoolean("FirstRun", true)) {
            Toast.makeText(this, "First run", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, CreateAccountActivity.class);
            startActivity(intent);
            prefs.edit().putBoolean("FirstRun", false).apply();
        }
    }

    private void changeAuthItem() {
        navigationView.getMenu().findItem(R.id.nav_sign_in).setVisible(getAuth() == null);
        navigationView.getMenu().findItem(R.id.nav_sign_out).setVisible(getAuth() != null);
        adapter.updateData();
        //adapter.notifyDataSetChanged();
    }
}
