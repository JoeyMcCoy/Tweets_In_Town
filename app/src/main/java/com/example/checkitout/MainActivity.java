package com.example.checkitout;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends AppCompatActivity {

    ListView lstMedia;
    Button btnSubmit;
    Button btnTestMap;

    private final String TWIT_CONS_KEY = "0PidwQdIP3Yf1oybRPYLal6A5";
    private final String TWIT_CONS_SEC_KEY = "q5hxuA2C7vz8FD8ebt5iG0MeoK9ua1puem43t0Ydh8NPyaKp3h";
    private final String TWIT_TOKEN = "1017676118-HYrdTLTxnWtxc5um9CvooakWknb9PXYIbLxfzeS";
    private final String TWIT_TOKEN_SEC = "yQStWvg3n8JO7wpBN5kgoQ18cYK2t7x4D5TRzGKXtAXxf";
   //long and lat variables
   private double latitude;
   private double longitude;
   private double currentLat;
   private double currentLong;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnSubmit = (Button) findViewById(R.id.btnLocation);
        btnTestMap = (Button) findViewById(R.id.btnTestMap);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_tweets);
                lstMedia = (ListView) findViewById(R.id.tweets);
                new SearchOnTwitter().execute();
            }
        });

        btnTestMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this,
                        MapsActivity.class);
                startActivity(myIntent);

            }
        });
        Intent receiveIntent = this.getIntent();
        currentLat = receiveIntent.getDoubleExtra("cLat",currentLat);
        currentLong = receiveIntent.getDoubleExtra("cLong",currentLong);


                        //MapsActivity object for current lat and long
//                MapsActivity currentLocation = new MapsActivity();
//                double currentLatitude = currentLocation.getCurrentLatitude();
//                double currentLongitude = currentLocation.getCurrentLongitude();
//                //MapsActivity object for newLat and newLong
//                MapsActivity newLocation = new MapsActivity();
//                double newLat = newLocation.getNewLat();
//                double newLong = newLocation.getNewLong();
//                //MapsActivity object for location
//                MapsActivity getUsersLocation = new MapsActivity();
//                String userLocation = getUsersLocation.getLocation();
//                //Checks to see if user has searched for a new location or if are using current location
//                if(userLocation !=null)
//                {
//                    latitude = newLat;
//                    longitude = newLong;
//                }
//                else{
//                    latitude = currentLatitude;
//                    longitude = currentLongitude;
//                }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class SearchOnTwitter extends AsyncTask<String, Void, Integer> {
        ArrayList<Tweet> tweets;
        final int SUCCESS = 0;
        final int FAILURE = 1;
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(MainActivity.this, "", getString(R.string.searching));
        }

        @Override
        protected Integer doInBackground(String... params) {
            try {
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setDebugEnabled(true)
                        .setOAuthConsumerKey(TWIT_CONS_KEY)
                        .setOAuthConsumerSecret(TWIT_CONS_SEC_KEY)
                        .setOAuthAccessToken(TWIT_TOKEN)
                        .setOAuthAccessTokenSecret(TWIT_TOKEN_SEC);

                Twitter twitter = new TwitterFactory(builder.build()).getInstance();

//                //MapsActivity object for current lat and long
//                MapsActivity currentLocation = new MapsActivity();
//                double currentLatitude = currentLocation.getCurrentLatitude();
//                double currentLongitude = currentLocation.getCurrentLongitude();
//                //MapsActivity object for newLat and newLong
//                MapsActivity newLocation = new MapsActivity();
//                double newLat = newLocation.getNewLat();
//                double newLong = newLocation.getNewLong();
//                //MapsActivity object for location
//                MapsActivity getUsersLocation = new MapsActivity();
//                String userLocation = getUsersLocation.getLocation();
//                //Checks to see if user has searched for a new location or if are using current location
//                if(userLocation !=null)
//                {
//                    latitude = newLat;
//                    longitude = newLong;
//                }
//                else{
//                    latitude = currentLatitude;
//                    longitude = currentLongitude;
//                }


                GeoLocation location = new GeoLocation(currentLat, currentLong);
                Log.d("Lat = ", Double.toString(currentLat));
                Log.d("Long = ", Double.toString(currentLong));
                Query query = new Query();
                query.setGeoCode(location, 25, Query.MILES);

                QueryResult result = twitter.search(query);

                List<twitter4j.Status> tweeters = result.getTweets();
                StringBuilder str = new StringBuilder();
                if (tweeters != null) {
                    this.tweets = new ArrayList<Tweet>();
                    for (twitter4j.Status tweet : tweeters) {
                        str.append("@").append(tweet.getUser().getScreenName()).append(" - ").append(tweet.getText()).append("\n");
                        System.out.println(str);
                        this.tweets.add(new Tweet("@" + tweet.getUser().getScreenName(), tweet.getText()));
                    }
                    return SUCCESS;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return FAILURE;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (result == SUCCESS) {
                lstMedia.setAdapter(new TweetAdapter(MainActivity.this, tweets));
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
            }
        }
    }
}