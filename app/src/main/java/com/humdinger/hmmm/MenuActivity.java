package com.humdinger.hmmm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MenuActivity extends ActionBarActivity {

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_logout:
                intent = new Intent(this, LoginActivity.class);
                this.setResult(RESULT_OK, intent);
                finish();
                return true;
            case R.id.action_match:
                intent = new Intent(this, MatchActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_chat:
                intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_profile:
                intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_forget:
                intent = new Intent(this, LoginActivity.class);
                this.setResult(RESULT_CANCELED, intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
