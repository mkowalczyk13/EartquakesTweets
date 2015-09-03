package com.example.magdalena.json1;

/**
 * Main Activity, where we fetch data from Earthquake website using JSON, Hashmap, Adapter.
 */

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity {

    private ProgressDialog pDialog;

    /**
     *  URL to get JSON data
     */

    private static String url = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.geojson";

    //ID to select 20
    private static String TAG_ID;

    /**
     *  JSON Node names
     */
    private static final String TAG_PROP = "properties";
    private static final String TAG_TYPE = "type";
    private static final String TAG_FEATURES = "features";
    private static final String TAG_MAG = "mag";
    private static final String TAG_DETAIL = "detail";
    private static final String TAG_PLACE = "place";
    private static final String TAG_TIME = "time";
    private static final String TAG_TSUNAMI = "tsunami";
    private static final String TAG_GEOMETRY = "geometry";
    private static final String TAG_COOR = "coordinates";

    // JSONArray features
    JSONArray features = null;
    ListAdapter adapter;
    ListView lv;
    double magn_todouble;
    String mag;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> dataList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataList = new ArrayList<HashMap<String, String>>();
        lv = getListView();

        // Listview on item click listener
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // getting values from selected ListItem
                String PL = ((TextView) view.findViewById(R.id.place))
                        .getText().toString();
                String TIM = ((TextView) view.findViewById(R.id.time))
                        .getText().toString();
                String MAGN = ((TextView) view.findViewById(R.id.mag))
                        .getText().toString();
                String COOR = ((TextView) view.findViewById(R.id.coor))
                        .getText().toString();

                //parsing magnitude to double
                magn_todouble = Double.parseDouble(MAGN);

                // Starting single contact activity
                Intent in = new Intent(getApplicationContext(),
                        MapsActivity.class);
                in.putExtra("place", PL);
                in.putExtra("time", TIM);
                in.putExtra("mag",magn_todouble);
                in.putExtra("coor",COOR );
                startActivity(in);

            }
        });

        // Calling async task to get json
        new GetContacts().execute();
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

            Log.d("Response: ", "> " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Getting JSON Array node

                    // Main node:
                    features = jsonObj.getJSONArray(TAG_FEATURES);

                    // looping through All JSON nodes
                    for (int i = 0; i < 20; i++) {
                        //node
                        JSONObject c = features.getJSONObject(i);
                        String id = String.valueOf(i);
                        //children of "c" JSONObject
                        String type = c.getString(TAG_TYPE);
                        //node of "c" Object
                        JSONObject prop = c.getJSONObject(TAG_PROP);
                        //children of "prop" JSONObject
                        String place = prop.getString(TAG_PLACE);
                        mag = prop.getString(TAG_MAG);
                        String time = prop.getString(TAG_TIME);

                        //Change time from epoch to date
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        String date = sdf.format(new Date(Long.parseLong(time)));

                        String tsunami = prop.getString(TAG_TSUNAMI);


                        // Geometry node is JSON Object
                        JSONObject geomtry = c.getJSONObject(TAG_GEOMETRY);
                        String coor = geomtry.getString(TAG_COOR);

                        // tmp hashmap for single contact
                        HashMap<String, String> contact = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        contact.put(TAG_ID, id);
                        contact.put(TAG_PLACE, place);
                        contact.put(TAG_MAG, mag);
                        contact.put(TAG_TIME, date);
                        contact.put(TAG_COOR, coor);

                        // adding contact to contact list
                        dataList.add(contact);
                    }

                    // Sort by magnitude
                    Collections.sort(dataList, new Comparator(){
                        @Override
                        public int compare(Object o1, Object o2) {
                            HashMap map1=(HashMap)o1;
                            HashMap map2=(HashMap)o2;
                            String s1=(String)map1.get("mag");
                            String s2=(String)map2.get("mag");
                            return s2.compareTo(s1);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
                Toast.makeText(MainActivity.this, "Couldn't get any data from the url. No internet connection.", Toast.LENGTH_SHORT).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            /**
             * Updating parsed JSON data into ListView
             * */
            adapter = new SimpleAdapter(
                    MainActivity.this, dataList,
                    R.layout.list_item, new String[]{TAG_PLACE, TAG_TIME,
                    TAG_MAG, TAG_COOR}, new int[]{R.id.place,
                    R.id.time, R.id.mag, R.id.coor});
            setListAdapter(adapter);

            /**
             * Tried to change color of listview Text.
             * */

         /*   LayoutInflater inflater = getLayoutInflater();
            View aView = inflater.inflate(R.layout.list_item, null);

            TextView messageText = (TextView) aView.findViewById(R.id.place);
              if(Double.parseDouble(mag) > 2) {
                 messageText.setTextColor(Color.RED);
              }*/
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        View menuItemView = findViewById(R.id.action_search);
        PopupMenu popupMenu = new PopupMenu(this, menuItemView);
        popupMenu.inflate(R.menu.menu_items);



        popupMenu.setOnMenuItemClickListener(
                new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch(item.getItemId()){
                            case R.id.action_show:
                                sort();
                                lv.invalidateViews();
                            default:
                                break;
                        }

                        return false;
                    }
                    }

        );

        popupMenu.show();
        return true;
    }

    public void sort(){
        Collections.sort(dataList, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                int i = 0, j = 0;
                HashMap map1 = (HashMap) o1;
                HashMap map2 = null;
                String s1 = (String) map1.get("tsunami");
                while (i < 20) {
                    if (s1.equals("1")) {
                        j++;
                        map2.put(map2, map1);
                    }
                    i++;
                }
                if (j == 0)
                    Toast.makeText(MainActivity.this, "No tsunami caused", Toast.LENGTH_SHORT).show();
                else {
                    dataList.add(map2);
                }
                return 0;
            }
        });
    }



}
