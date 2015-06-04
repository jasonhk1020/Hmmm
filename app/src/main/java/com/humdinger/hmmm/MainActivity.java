package com.humdinger.hmmm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.ParseAnalytics;
import com.parse.PushService;


public class MainActivity extends ActionBarActivity implements BackHandledFragment.BackHandlerInterface {

    // Declaring Your View and Variables
    CustomViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[]={"Hmmm...","Chat","Profile"};
    int Numboftabs =3;
    private SharedPreferences prefs;
    public String uid;
    private BackHandledFragment selectedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles for the Tabs and Number Of Tabs.
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(), Titles, Numboftabs);

        // Assigning ViewPager View and setting the adapter (custom to prevent swiping)
        pager = (CustomViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        //track how many times app has been opened
        PushService.setDefaultPushCallback(this, MainActivity.class);
        ParseAnalytics.trackAppOpened(getIntent());

        //get parse push intents
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String jsonData = extras.getString( "com.parse.Data" );
            if (jsonData != null) {
                pager.setCurrentItem(1);
            }
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        //determine if you want the mainactivity to deal with back presses
        if(selectedFragment == null || !selectedFragment.onBackPressed()) {

            //yes please main activity, deal with back press for me!
            if (pager.getCurrentItem() == 0) {
                //were are inside our home base "match tab", so let's act like we hit the home button and just hide the app
                moveTaskToBack(true);
            } else {
                //since we are either in the chat or profile tab let's go to match tab
                pager.setCurrentItem(0);
            }

        }
    }


    @Override
    public void setSelectedFragment(BackHandledFragment selectedFragment) {
        this.selectedFragment = selectedFragment;
    }
}