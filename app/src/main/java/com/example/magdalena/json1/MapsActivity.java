package com.example.magdalena.json1;
/**
 * Second Activity, which contains map with place marker and tweets from this location
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.List;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

public class MapsActivity extends Activity implements OnMapReadyCallback {

    // Twitter keys, needed for application
    private final String TWIT_CONS_KEY = "WZJFNl49Ry8Ba3SA62O5mfO08";
    private final String TWIT_CONS_SEC_KEY = "t7g7F0tZ7wh28i399v6AWZPSkuMKExXozypUjwkOmNevVmdw7J";
    private ProgressDialog pDialog;

    //URL for using Twitter API.
    private static String url = "https://api.twitter.com/1.1/search/tweets.json?geocode=-37.8143,144.963,1000km&count=100";

    //variables
    ListView lv;
    String coor, place ;
    String coords[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //MAP
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        lv = (ListView) findViewById(R.id.listView);


        Intent in = getIntent();
        // Get JSON values from previous intent
        place = in.getStringExtra("place");
        System.out.println("pl" + place);
        long tim = in.getLongExtra("time", 0);
        System.out.println("ti" + tim);
        double mag = in.getDoubleExtra("mag", 0);
        System.out.println("ma" + mag);
        String cor = in.getStringExtra("coor");
        System.out.println("cor" + cor);

        //split coordinates
        coor = cor.replace('[',' ');
        coor = coor.replace(']',' ');
        coor = coor.replace(',',' ');
        coords = coor.split(" ");

        //String which is used to locate tweets (the same value )
        String search = "geocode:"+coords[2]+","+coords[1]+",150km";
        System.out.println("search"+search);
        new SearchOnTwitter().execute(search);
    }

    class SearchOnTwitter extends AsyncTask<String, Void, Integer> {

        ArrayList<Tweet> tweets;
        final int SUCCESS = 0;
        final int FAILURE = SUCCESS + 1;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Integer doInBackground(String... params) {
            try {

                //configure Twitter connection
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setApplicationOnlyAuthEnabled(true);

                builder.setOAuthAuthenticationURL("https://api.twitter.com/oauth/request_token");
                builder.setOAuthAccessTokenURL("https://api.twitter.com/oauth/access_token");
                builder.setOAuthAuthorizationURL("https://api.twitter.com/oauth/authorize");
                builder.setOAuthRequestTokenURL("https://api.twitter.com/oauth/request_token");
                builder.setRestBaseURL("https://api.twitter.com/1.1/");
                builder.setOAuthConsumerKey(TWIT_CONS_KEY);
                builder.setOAuthConsumerSecret(TWIT_CONS_SEC_KEY);

                OAuth2Token token = new TwitterFactory(builder.build()).getInstance().getOAuth2Token();

                builder = new ConfigurationBuilder();
                builder.setApplicationOnlyAuthEnabled(true);
                builder.setOAuthConsumerKey(TWIT_CONS_KEY);
                builder.setOAuthConsumerSecret(TWIT_CONS_SEC_KEY);
                builder.setOAuth2TokenType(token.getTokenType());
                builder.setOAuth2AccessToken(token.getAccessToken());

                Twitter twitter = new TwitterFactory(builder.build()).getInstance();
                Query query = new Query(params[0]);

                // maximum records
                query.setCount(10);
                QueryResult result;
                result = twitter.search(query);

                //get text for tweets
                List<twitter4j.Status> tweets = result.getTweets();
                StringBuilder str = new StringBuilder();
                if (tweets != null) {
                    this.tweets = new ArrayList<Tweet>();
                    for (twitter4j.Status tweet : tweets) {
                        str.append("@" + tweet.getUser().getScreenName() + " - " + tweet.getText() + "\n");
                        if(str==null)
                            this.tweets.add(new Tweet("App", "No tweets in this location."));
                        else
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
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            lv.setAdapter(new TweetAdapter(MapsActivity.this, tweets));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
     //   setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
 /*   private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.3030, 0.0732), 9));
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }*/

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link # m Map} is not null.
     */
  /*  private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }*/


    @Override
    public void onMapReady(GoogleMap googleMap) {
        //configure MAP
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(coords[2]),Double.parseDouble(coords[1]) ), 9));
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(Double.parseDouble(coords[2]),Double.parseDouble(coords[1])))
                .title("Earthquake in: "+place));
    }
}
