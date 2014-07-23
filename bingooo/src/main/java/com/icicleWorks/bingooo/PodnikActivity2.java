package com.icicleWorks.bingooo;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.OpenGraphAction;
import com.facebook.model.OpenGraphObject;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;



public class PodnikActivity2 extends ActionBarActivity {

    TextView tvAbout, tvName, tvZlavy;
    String name;
    String[] pAbout;
    List<String> savedlist;
    String csvList;

    //facebook
    private UiLifecycleHelper uiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.podnik_layout);

        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String about = intent.getStringExtra(MainActivity.EXTRA_ABOUT);
        name = intent.getStringExtra(MainActivity.EXTRA_NAME);
        pAbout = about.split(";");

        tvAbout = (TextView) findViewById(R.id.popisPodniku);
        tvName = (TextView) findViewById(R.id.nazovPodniku);
        tvZlavy = (TextView) findViewById(R.id.zlavy);
        tvName.setText(name);
        tvAbout.setText(pAbout[0]);
        tvZlavy.setText(pAbout[1] +"..." +pAbout[2] +"..." +pAbout[3]);
    }

    public void generate(View v){
        publishFeedDialog(name, pAbout[0]);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("Activity", String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("Activity", "Success!");
            }
        });
    }

    /**public void shareStory(){
        OpenGraphObject meal = OpenGraphObject.Factory.createForPost("icicle_works:meal");
        meal.setProperty("title", name);
        meal.setProperty("description", pAbout[0]);

        OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
        action.setProperty("meal", meal);

        FacebookDialog shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(this, action, "icicle_works:cook", "meal")
                .build();
        uiHelper.trackPendingDialogCall(shareDialog.present());
    }
    */

    public void publishFeedDialog(String name, String popis) {
        Bundle params = new Bundle();
        params.putString("type", "icicle_works:bar");
        params.putString("description", popis);
        params.putString("title", name);
        params.putString("name", name);
        params.putString("caption", popis);
        params.putString("link", "https://drive.google.com/file/d/0BwYDWD6_ZpGLd0NMYjdydC11eDg/edit?usp=sharing");
        params.putString("picture", "http://i58.tinypic.com/23049l.png");

        Request request = new Request(
                Session.getActiveSession(),
                "me/objects/icicle_works:bar",
                params,
                HttpMethod.POST
        );

        Response response = request.executeAndWait();

        WebDialog feedDialog = (
                new WebDialog.FeedDialogBuilder(this,
                        Session.getActiveSession(),
                        params))
                .setOnCompleteListener(new WebDialog.OnCompleteListener() {

                    @Override
                    public void onComplete(Bundle values,
                                           FacebookException error) {
                        if (error == null) {
                            // When the story is posted, echo the success
                            // and the post Id.
                            final String postId = values.getString("post_id");
                            if (postId != null) {
                                TextView zlava = (TextView) findViewById(R.id.textView3);
                                TextView date = (TextView) findViewById(R.id.zlava_datum);
                                date.setVisibility(View.VISIBLE);

                                Calendar c = Calendar.getInstance();
                                date.setText("Zľava je platná len " +c.get(Calendar.DATE) +"." +c.get(Calendar.MONTH) +"." +c.get(Calendar.YEAR));
                                int i = (int) (Math.random() * ( 101 - 0 ));
                                Log.v(MainActivity.TAG, "Akcia: " +i);
                                if (i <= 50) zlava.setText("GRATULUJEME\nZískal si " +pAbout[1] +" zľavu");
                                else if (50 < i && i <= 85)  zlava.setText("GRATULUJEME\nZískal si " +pAbout[2] +" zľavu");
                                else    zlava.setText("GRATULUJEME\nZískal si " +pAbout[3] +" zľavu");

                            } else {
                                // User clicked the Cancel button
                                Toast.makeText(getApplicationContext(),
                                        "Pre získanie zľavy musíš zdielať",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else if (error instanceof FacebookOperationCanceledException) {
                            // User clicked the "x" button
                            Toast.makeText(getApplicationContext(),
                                    "Pre získanie zľavy musíš zdielať",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Generic, ex: network error
                            Toast.makeText(getApplicationContext(),
                                    "Chyba siete",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                })
                .build();
        feedDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.podnik_activity2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_favorite) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public  void onStop(){
        super.onStop();
        saveFavorites();
    }
    public void saveFavorites(){
        /** cez Map a sharedPreferences.getAll */
    }


}
