package com.example.checkitout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class MapsActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    Button seeTweetsBtn;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleClient;
    private double newLat;
    private double newLong;
    private double currentLatitude;
    private double currentLongitude;
    private String location;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private final String TWIT_CONS_KEY = "0PidwQdIP3Yf1oybRPYLal6A5";
    private final String TWIT_CONS_SEC_KEY = "q5hxuA2C7vz8FD8ebt5iG0MeoK9ua1puem43t0Ydh8NPyaKp3h";
    private final String TWIT_TOKEN = "1017676118-HYrdTLTxnWtxc5um9CvooakWknb9PXYIbLxfzeS";
    private final String TWIT_TOKEN_SEC = "yQStWvg3n8JO7wpBN5kgoQ18cYK2t7x4D5TRzGKXtAXxf";
    ListView lstMedia;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
           actionBar.setDisplayHomeAsUpEnabled(true);
           actionBar.setHomeAsUpIndicator(R.mipmap.android_icon);
        }
        seeTweetsBtn = (Button) findViewById(R.id.seeTweetsBtn);

        seeTweetsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_tweets);
                lstMedia = (ListView) findViewById(R.id.tweets);
                new SearchOnTwitter().execute();
            }
        });
        mGoogleClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        //creating locationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(60*1000)       //10 seconds
                .setFastestInterval(1*1000);//1 second

    }



    public void onSearch(View view)
    {

        EditText location_tf = (EditText)findViewById(R.id.TFaddress);
        location = location_tf.getText().toString();
        List<Address> addressList = null;
        if(location !=null || !location.equals(""))
        {
            Geocoder geocoder = new Geocoder(this);
            try{
                addressList = geocoder.getFromLocationName(location,1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            newLat = address.getLatitude();
            newLong = address.getLongitude();

            Toast.makeText(getApplicationContext(),
                    String.valueOf("New Location: " + newLat + "," + newLong),
                    Toast.LENGTH_LONG).show();
            Log.d("New Lat = ", Double.toString(newLat));
            Log.d("New Long = ", Double.toString(newLong));

        }


        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
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
       switch (item.getItemId()) {
           case android.R.id.home:
               // go to previous screen when app icon in action bar is clicked
               Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
               intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
               startActivity(intent);
           case R.id.action_settings:
               return true;
           case R.id.action_search:
               startActivity(new Intent(getApplicationContext(), MapsActivity.class));
           case R.id.action_tweets:
               setContentView(R.layout.activity_tweets);
               lstMedia = (ListView) findViewById(R.id.tweets);
               new SearchOnTwitter().execute();
           break;
       }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        super.onResume();

        setUpMapIfNeeded();

        checkNetwork();

        mGoogleClient.connect();

    }

    private void checkNetwork() {
        LocationManager mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled){
            //notify user
            AlertDialog.Builder gpsAlert = new AlertDialog.Builder(this);
            gpsAlert.setMessage("Please enable location settings on your device. Don't worry, we'll take you there :D");
            gpsAlert.setPositiveButton("Take me there", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent settings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(settings);
                }
            });
            gpsAlert.setCancelable(false);

            gpsAlert.show();
        }

    }

    private void setUpMapIfNeeded() {

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {

                //setUpMap();
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleClient, this);
            mGoogleClient.disconnect();
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }



    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected");

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleClient);

        if(location == null){

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleClient, mLocationRequest, this);
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();


        }else{
            handleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    private void handleNewLocation(Location location){
        Log.d(TAG, location.toString());

        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("Im here!");

        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        //Zooms in on user's current location
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(8)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        //Tells the user their current location in the form of a toast
        Toast.makeText(getApplicationContext(),
                String.valueOf("Current location: " + currentLatitude + "," + currentLongitude),
                Toast.LENGTH_LONG).show();
//Send data to MainActivity
        Intent send = new Intent(MapsActivity.this, MainActivity.class);
        send.putExtra("cLat", currentLatitude);
        send.putExtra("cLong", currentLongitude);

        //   Added to find the exact latitude and longitude
        Log.d("User's current Lat = ", Double.toString(currentLatitude));
        Log.d("User's current Long = ", Double.toString(currentLongitude));

    }
    public double getCurrentLatitude(){
        return this.currentLatitude;
    }
    public double getCurrentLongitude(){
        return this.currentLongitude;
    }
    @Override
    public void onLocationChanged(Location location) {

        handleNewLocation(location);


    }
    class SearchOnTwitter extends AsyncTask<String, Void, Integer> {
        ArrayList<Tweet> tweets;
        final int SUCCESS = 0;
        final int FAILURE = 1;
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(MapsActivity.this, "", getString(R.string.searching));
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
                double latitude;
                double longitude;
                if(location !=null)
                {
                    latitude = newLat;
                    longitude = newLong;
                }
                else{
                    latitude = currentLatitude;
                    longitude = currentLongitude;
                }
                GeoLocation location = new GeoLocation(latitude, longitude);
                Log.d("Lat = ", Double.toString(latitude));
                Log.d("Long = ", Double.toString(longitude));
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
                lstMedia.setAdapter(new TweetAdapter(MapsActivity.this, tweets));
            } else {
                Toast.makeText(MapsActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
            }
        }
    }}
