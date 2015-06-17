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

        //track how many times app has been opened and handling push callbacks to this activity
        PushService.setDefaultPushCallback(this, MainActivity.class);
        ParseAnalytics.trackAppOpened(getIntent());
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences statusPrefs = getSharedPreferences("statusPrefs", 0);
        statusPrefs.edit().putBoolean("opened", false).commit();

    }

    @Override
    protected void onNewIntent (Intent intent) {
        super.onNewIntent(intent);
        // getIntent() should always return the most recent
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences statusPrefs = getSharedPreferences("statusPrefs", 0);
        statusPrefs.edit().putBoolean("opened", true).commit();

        //get parse push intents (remember to keep this in on rsume or else, it wont get the intents from the notification)
        //DON"T PUT IT IN THE ONCREATE!
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Boolean isMessageNotification = extras.getBoolean("messageNotification");
            Boolean isRequestNotification = extras.getBoolean("requestNotification");
            Boolean isAcceptNotification = extras.getBoolean("acceptNotification");
            if (isMessageNotification) {
                //go to the chat page and open the user chat message
                pager.setCurrentItem(1);
            } else if (isRequestNotification) {
                //go to the match page so you can decide how to deal with match request
                pager.setCurrentItem(0);
            } else if(isAcceptNotification) {
                //go to the chat page since you probably want to introduce yourselfves
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