package com.aparnyuk.rsn;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.aparnyuk.rsn.adapter.TabsFragmentAdapter;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    Toolbar toolbar;
    ViewPager viewPager;
    FloatingActionButton fab;
    TabLayout tabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initTabs();
        initNavigationDrover();
        initFloatingButton();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initTabs() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        TabsFragmentAdapter adapter = new TabsFragmentAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);


//!!
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int state = 0;
            private boolean isFloatButtonHidden = false;
            private int position = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (!isFloatButtonHidden && state == 1 && positionOffset != 0.0) {
                    isFloatButtonHidden = true;
                    //hide floating action button
                    swappingAway();
                }
            }

            @Override
            public void onPageSelected(int position) {
                //reset floating
                this.position = position;

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

    private void selectedTabs(int tab) {
        fab.show();
        //a bit animation of popping up.
        fab.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.pop_up);
        fab.startAnimation(animation);
        int selectedColor;
        int defaultColor = getResources().getColor(R.color.colorPrimaryLight);
        int tabColor;
        switch (tab) {
            case (Constants.TAB_ONE_SMS): {
                selectedColor = (getResources().getColor(R.color.colorFab1));
                tabColor = (getResources().getColor(R.color.colorTabLine1));
                fab.setImageResource(android.R.drawable.ic_dialog_email);
                //fab.setImageResource(R.drawable.ic_email_white_36dp);
                fab.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                tabLayout.setSelectedTabIndicatorColor(tabColor);
                tabLayout.setTabTextColors(defaultColor,tabColor);
                break;
            }
            case (Constants.TAB_TWO_CALL): {
                selectedColor = getResources().getColor(R.color.colorFab2);
                tabColor = (getResources().getColor(R.color.colorTabLine2));
                fab.setImageResource(R.drawable.ic_phone_white_36dp);
                fab.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                tabLayout.setSelectedTabIndicatorColor(tabColor);
                tabLayout.setTabTextColors(defaultColor,tabColor);
                break;
            }
            case (Constants.TAB_THREE_REMIND): {
                selectedColor = getResources().getColor(R.color.colorFab3);
                tabColor = (getResources().getColor(R.color.colorTabLine3));
                fab.setImageResource(R.drawable.ic_alarm_check_white_36dp);
                fab.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                tabLayout.setSelectedTabIndicatorColor(tabColor);
                tabLayout.setTabTextColors(defaultColor,tabColor);
                break;
            }
            case (Constants.TAB_FOUR_NOTE): {
                selectedColor = getResources().getColor(R.color.colorFab4);
                tabColor = (getResources().getColor(R.color.colorTabLine4));
                fab.setImageResource(R.drawable.ic_pen_white_36dp);
                fab.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                tabLayout.setSelectedTabIndicatorColor(tabColor);
                tabLayout.setTabTextColors(defaultColor,tabColor);
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

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initFloatingButton() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             /*   Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                Toast.makeText(getApplicationContext(), "Test", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Работа с меню в Toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case (R.id.action_items_view): {
                Toast.makeText(getApplicationContext(), "Test", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // Работа с меню NavigationDrover
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case (R.id.nav_auth): {
                Toast.makeText(getApplicationContext(), "Test", Toast.LENGTH_SHORT).show();
                break;
            }
            case (R.id.nav_sync): {
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
        // закрыть шторку после выбора пункта меню
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Закрыть NavigationDrover при нажатии кнопки назад
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
