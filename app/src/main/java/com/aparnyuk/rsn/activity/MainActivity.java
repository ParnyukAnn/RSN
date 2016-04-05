package com.aparnyuk.rsn.activity;

import android.content.res.ColorStateList;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.aparnyuk.rsn.Utils.Constants;
import com.aparnyuk.rsn.R;
import com.aparnyuk.rsn.adapter.CallListAdapter;
import com.aparnyuk.rsn.adapter.NoteListAdapter;
import com.aparnyuk.rsn.adapter.RemindListAdapter;
import com.aparnyuk.rsn.adapter.SmsListAdapter;
import com.aparnyuk.rsn.adapter.TabsFragmentAdapter;
import com.aparnyuk.rsn.fragment.dialog.CallDialog;
import com.aparnyuk.rsn.fragment.dialog.NoteDialog;
import com.aparnyuk.rsn.fragment.dialog.RemindDialog;
import com.aparnyuk.rsn.fragment.dialog.SmsDialog;
import com.aparnyuk.rsn.login.CreateAccountActivity;
import com.aparnyuk.rsn.login.LoginActivity;
import com.aparnyuk.rsn.services.MainService;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.ui.auth.core.AuthProviderType;
import com.firebase.ui.auth.core.FirebaseLoginBaseActivity;
import com.firebase.ui.auth.core.FirebaseLoginError;

public class MainActivity extends FirebaseLoginBaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    /*Use to show logs*/
    public static final String TAG = "MainActivity";

    ActionBarDrawerToggle toggle;
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
    SmsDialog smsDialog;

    /* Handle click to fragment with recycler view */
    onDeleteClickListener deleteClickListener;

    public interface onDeleteClickListener {
        void onDeleteClick(boolean delete);
    }

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

    }

    /*----------------------------------TABS AND ANIMATION----------------------------------------*/
    private void initTabs() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        adapter = new TabsFragmentAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        Log.d(TAG, "" + viewPager.getCurrentItem());

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(viewPager);

        // add animation to floating button and change tabs color
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
    }

    private void swappingAway() {
        fab.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.pop_down);
        fab.startAnimation(animation);
    }

    /* Add button animation, put icons on button, set delete interface if it need*/
    private void selectedTabs(int tab) {
        fab.show();
        //a bit animation of popping up.
        fab.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.pop_up);
        fab.startAnimation(animation);
        switch (tab) {
            case (Constants.TAB_ONE_SMS): {
                fab.setImageResource(android.R.drawable.ic_dialog_email);
                setInterface(SmsListAdapter.isDeleteMode());
                break;
            }
            case (Constants.TAB_TWO_CALL): {
                fab.setImageResource(R.drawable.ic_phone_white_36dp);
                setInterface(CallListAdapter.isDeleteMode());
                break;
            }
            case (Constants.TAB_THREE_REMIND): {
                fab.setImageResource(R.drawable.ic_alarm_check_white_36dp);
                setInterface(RemindListAdapter.isDeleteMode());
                break;
            }
            case (Constants.TAB_FOUR_NOTE): {
                fab.setImageResource(R.drawable.ic_pen_white_36dp);
                setInterface(NoteListAdapter.isDeleteMode());
                break;
            }
        }

    }

    /*-----------------------------------DELETE INTERFACE-----------------------------------------*/
    /* This metods needs to change toolbar, tabs and button color when
     * onLongClick is set "delete mode" for current tab */
    public void setInterface(boolean mode) {
        if (mode) {
            setDeleteModeInterface();
        } else {
            setNormalModeInterface();
        }
    }

    public void setDeleteModeInterface() {
        //  getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setBackgroundColor(getResources().getColor(R.color.deleteMode));
        tabLayout.setBackgroundColor(getResources().getColor(R.color.deleteMode));
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.white));
        tabLayout.setTabTextColors(getResources().getColor(R.color.light_grey), getResources().getColor(R.color.white));
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.deleteMode)));
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.deleteModeDark));
        }
        invalidateOptionsMenu();
        toggle.setDrawerIndicatorEnabled(false);
        assert getDrawerToggleDelegate() != null;
        toggle.setHomeAsUpIndicator(getDrawerToggleDelegate().getThemeUpIndicator());
        setDeleteItemsInTitle(position);
    }

    public void setDeleteItemsInTitle(int position) {
        switch (position) {
            case (Constants.TAB_ONE_SMS): {
                setTitle("" + SmsListAdapter.getDeleteItemSet().size());
                break;
            }
            case (Constants.TAB_TWO_CALL): {
                setTitle("" + CallListAdapter.getDeleteItemSet().size());
                break;
            }
            case (Constants.TAB_THREE_REMIND): {
                setTitle("" + RemindListAdapter.getDeleteItemSet().size());
                break;
            }
            case (Constants.TAB_FOUR_NOTE): {
                setTitle("" + NoteListAdapter.getDeleteItemSet().size());
                break;
            }
        }
    }

    public void setNormalModeInterface() {
        //  getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        // toggle.setDrawerIndicatorEnabled(true);
        //  toggle.setHomeAsUpIndicator(((AppCompatActivity) getActivity()).getDrawerToggleDelegate().getThemeUpIndicator());
        setTitle(R.string.app_name);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        tabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorFab4));
        tabLayout.setTabTextColors(getResources().getColor(R.color.colorPrimaryLight), getResources().getColor(R.color.colorTabLine4));
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        invalidateOptionsMenu();
        toggle.setDrawerIndicatorEnabled(true);
        assert getDrawerToggleDelegate() != null;
        toggle.setHomeAsUpIndicator(getDrawerToggleDelegate().getThemeUpIndicator());
    }

    /* Check if the current tab in "delete mode" */
    public boolean checkDeleteMode(int selectedTab) {
        boolean mode = false;
        switch (selectedTab) {
            case (Constants.TAB_ONE_SMS): {
                mode = (SmsListAdapter.isDeleteMode());
                break;
            }
            case (Constants.TAB_TWO_CALL): {
                mode = (CallListAdapter.isDeleteMode());
                break;
            }
            case (Constants.TAB_THREE_REMIND): {
                mode = (RemindListAdapter.isDeleteMode());
                break;
            }
            case (Constants.TAB_FOUR_NOTE): {
                mode = (NoteListAdapter.isDeleteMode());
                break;
            }
        }
        return mode;
    }

    /* Save and restore selected tab, set "delete interface"
     * and number of selected items in title */
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", position);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        position = savedInstanceState.getInt("position");
        if (checkDeleteMode(position)) {
            setDeleteModeInterface();
        }
    }

    /*-------------------------------FLOATING BUTTON----------------------------------------------*/

    private void initFloatingButton() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        p.setAnchorId(View.NO_ID);
        fab.setLayoutParams(p);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getAuth() == null) {
                    showFirebaseLoginPrompt();
                } else {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    switch (position) {
                        case (Constants.TAB_ONE_SMS): {
                            if (!SmsListAdapter.isDeleteMode()) {
                                smsDialog = new SmsDialog();
                                smsDialog.show(fragmentManager, "CreateDialog4");
//                                ArrayList<String> phoneNumbers = new ArrayList<>();
//                                phoneNumbers.add("8947839534");
//                                phoneNumbers.add("5487983721");
//                                Sim sim = new Sim("sim 1", "phone 2");
//                                Sms sms = new Sms(phoneNumbers, sim, "dfasf", new Date());
//
//                                Firebase base = new Firebase(Constants.FIREBASE_URL);
//                                AuthData authData = base.getAuth();
//                                if (authData != null) {
//                                    base = base.child(authData.getUid());
//                                }
//                                base.child("sms").push().setValue(sms);
                            }
                            break;
                        }
                        case (Constants.TAB_TWO_CALL): {
                            if (!CallListAdapter.isDeleteMode()) {
                                callDialog = new CallDialog();
                                callDialog.show(fragmentManager, "CreateDialog3");
//                            ArrayList<String> phoneNumbers = new ArrayList<>();
//                            phoneNumbers.add("8947839534");
//                            phoneNumbers.add("5487983721");
//                            Sim sim = new Sim("sim 1", "phone 2");
//                            Calls call = new Calls(phoneNumbers, sim, new Date());
//                            call.setText("asdfasd");
//                            Firebase base = new Firebase(Constants.FIREBASE_URL);
//                            AuthData authData = base.getAuth();
//                            if (authData != null) {
//                                base = base.child(authData.getUid());
//                            }
//                            base.child("call").push().setValue(call);

                            }
                            break;
                        }
                        case (Constants.TAB_THREE_REMIND): {
                            if (!RemindListAdapter.isDeleteMode()) {
                                remindDialog = new RemindDialog();
                                remindDialog.show(fragmentManager, "CreateDialog2");
                            }
                            break;
                        }
                        case (Constants.TAB_FOUR_NOTE): {
                            if (!NoteListAdapter.isDeleteMode()) {
                                noteDialog = new NoteDialog();
                                noteDialog.show(fragmentManager, "CreateDialog1");
                            }
                            break;
                        }
                    }
                }
            }
        });
    }

    /*------------------------------------TOOLBAR-------------------------------------------------*/
    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /*Create and work with main menu in Toolbar (show or hide basket)*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//       menu.findItem(R.id.action_login).setVisible(getAuth() == null);
//       menu.findItem(R.id.action_logout).setVisible(getAuth() != null);

        // make visible basket if in some fragment set "delete mode"
        menu.findItem(R.id.action_delete).setVisible(checkDeleteMode(position));
        return true;
    }

    /* Work with Toolbar menu */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        try {
            deleteClickListener = adapter.getItem(position);
        } catch (ClassCastException e) {
            throw new ClassCastException(adapter.getItem(position).toString() + " must implement onSomeEventListener");
        }
        switch (item.getItemId()) {
            case R.id.action_delete:
                deleteClickListener.onDeleteClick(true);
                return true;
            case android.R.id.home:
                deleteClickListener.onDeleteClick(false);
                Log.d("Note", "action bar clicked");
                return true;
//            case R.id.action_login:
//                this.showFirebaseLoginPrompt();
//                return true;
//            case R.id.action_logout:
//                this.logout();
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*------------------------------NAVIGATION DROVER---------------------------------------------*/
    private void initNavigationDrover() {
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.view_navigation_open, R.string.view_navigation_close);
        // in "delete mode" click on the arrow remove delete mode
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!toggle.isDrawerIndicatorEnabled()) {
                    onBackPressed();
                }
            }
        });
        assert drawerLayout != null;
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.navigation);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        changeAuthItem();
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
                if (!MainService.state) {
                    stopService(new Intent(this, MainService.class));
                }
                // update date
                startService(new Intent(this, MainService.class));
                break;
            }
            case (R.id.nav_search): {
                if (!MainService.state) {
                    stopService(new Intent(this, MainService.class));
                }
                break;
            }
            case (R.id.nav_history): {
                if (MainService.state) {
                    startService(new Intent(this, MainService.class));
                }

                break;
            }
            case (R.id.nav_settings): {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
        }
        // close NavigationDrawer after choosing menu item
        changeAuthItem();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /* Close NavigationDrawer when put back button */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (checkDeleteMode(position)) {
                try {
                    deleteClickListener = adapter.getItem(position);
                } catch (ClassCastException e) {
                    throw new ClassCastException(adapter.getItem(position).toString() + " must implement onSomeEventListener");
                }
                deleteClickListener.onDeleteClick(false);
            } else {
                super.onBackPressed();
            }
        }
    }

    /*--------------------------------FIREBASE LOGIN/LOGOUT----------------------------------------*/
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
