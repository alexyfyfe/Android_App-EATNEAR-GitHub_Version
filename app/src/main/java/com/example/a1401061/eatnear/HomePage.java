package com.example.a1401061.eatnear;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class HomePage extends AppCompatActivity implements View.OnClickListener {

    private Button goButton;
    private ToggleButton settingsButton;
    private PopupWindow homePageMenu;
    private Spinner typeDropDown;
    private ArrayList<String> cuisineArray = new ArrayList<String>();

    private TextView header;
    private TextView home_page_menu_wishlist;
    private TextView home_page_menu_beento;
    private TextView home_page_menu_history;
    private TextView home_page_menu_settings;

    private int permissionRequestIdentifier;
    private int permissionLocation;
    private boolean permissionUpdate;

    //Location Manager
    private LocationManager locationManager;
    private android.location.LocationListener locationListener;

    //Lat and Long Variables
    private Double latVal;
    private Double longVal;

    //HttpRequest
    innerHttpRequestGetCuisines taskCuisines;
    JSONArray jsonCuisineArray;
    ArrayAdapter<CharSequence> typeDropDownAdapter;
    //Key Value pair cuisine_id:cuisine_name
    HashMap<Integer, String> cuisine_hashmap = new HashMap<>();

    //Database
    DatabaseHelper myDB;

    //Popup Help
    private PopupWindow homePageHelp;
    private LayoutInflater layoutInflater;
    private RelativeLayout relativeLayout;
    private Button homeQuestionMark;
    //Width/Height of device
    private int width;
    private int height;
    private View help_background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        //Creating the database and tables
        myDB = new DatabaseHelper(this);
        myDB.close();

        /*Width & Height of device*/
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;

        /*DROP DOWN LIST has to be initialised before Async task taskCuisines is run*/
        cuisineArray.add(0, "Loading...");
        typeDropDown = (Spinner) this.findViewById(R.id.typeDropDown);
        //Setting dropdown width to match screen width
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            typeDropDown.setDropDownWidth(width);
        }
        // Create an ArrayAdapter using the string array and a default spinner
        typeDropDownAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, cuisineArray);
        // Specify the layout to use when the list of choices appears
        typeDropDownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        typeDropDown.setAdapter(typeDropDownAdapter);
        typeDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });


        /*GO BUTTON has to run before async task to disable it will be enabled once async task finished*/
        goButton = (Button) this.findViewById(R.id.goButton);
        goButton.setEnabled(false);
        goButton.setOnClickListener(this);

        /*Run Async Task to get dropdown cuisines based on location of user*/
        //TESTING LONG AND LAT ABERDEEN LOCATION
        //latVal = 57.149651;
        //longVal = -2.099075;


        //Getting GPS location
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //OnLocationChange find nearby restaurant cuisines from API and update dropdown list
                latVal = location.getLatitude();
                longVal = location.getLongitude();
                taskCuisines = new innerHttpRequestGetCuisines();
                taskCuisines.execute("https://developers.zomato.com/api/v2.1/cuisines?lat=" + latVal.toString() + "&lon=" + longVal.toString());

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, permissionRequestIdentifier);
                return;
            } else {
                locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
            }
        }



        relativeLayout = (RelativeLayout) findViewById(R.id.activity_home_page);

        /*SETTINGS BUTTON*/
        settingsButton = (ToggleButton) this.findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(this);

        /*HEADER*/
        header = (TextView) this.findViewById(R.id.homePageTitle);
        header.setOnClickListener(this);

        /*Help QuestionMark*/
        homeQuestionMark = (Button) this.findViewById(R.id.homeQuestionMark);
        homeQuestionMark.setOnClickListener(this);

    }

    public void onClick(View v) {
        if (v.equals(goButton)) {
            Intent in;
            Bundle passedData = new Bundle();
            //Passing selected cuisine type
            passedData.putString("cuisine_name_choice", (String) typeDropDown.getSelectedItem());
            passedData.putDouble("last_know_lat", latVal);
            passedData.putDouble("last_know_long", longVal);
            in = new Intent(this, MapPage.class);
            //Passing cuisine Hashmap
            in.putExtra("cuisine_hashmap", cuisine_hashmap);
            in.putExtras(passedData);
            startActivity(in);
        }
        //Popup Help
        if(v.equals(homeQuestionMark)){
            layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.activity_home_page_help, null);

            homePageHelp = new PopupWindow(container, width, height, true);
            homePageHelp.showAtLocation(relativeLayout, Gravity.NO_GRAVITY, 0, 0);
            help_background = (View) container.findViewById(R.id.help_background);
            //Hide popup if click anywhere
            help_background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    homePageHelp.dismiss();
                }
            });
        }
        if (v.equals(settingsButton)) {
            if (settingsButton.isChecked()) {
                layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.activity_home_page_menu, null);

                homePageMenu = new PopupWindow(container, width, (int) (height * 0.60));
                homePageMenu.showAtLocation(relativeLayout, Gravity.NO_GRAVITY, 0, (int) (height * 0.24));

                /*Popup Menu*/
                home_page_menu_wishlist = (TextView) container.findViewById(R.id.home_page_menu_wishlist);
                home_page_menu_wishlist.setOnClickListener(this);
                home_page_menu_beento = (TextView) container.findViewById(R.id.home_page_menu_beento);
                home_page_menu_beento.setOnClickListener(this);
                home_page_menu_history = (TextView) container.findViewById(R.id.home_page_menu_history);
                home_page_menu_history.setOnClickListener(this);
                home_page_menu_settings = (TextView) container.findViewById(R.id.home_page_menu_settings);
                home_page_menu_settings.setOnClickListener(this);

            } else {
                homePageMenu.dismiss();
            }
        }
        if (v.equals(header)) {
            Intent in;
            in = new Intent(this, HomePage.class);
            startActivity(in);
        }
        if (v.equals(home_page_menu_wishlist)) {
            Intent in;
            in = new Intent(this, WishListPage.class);
            startActivity(in);
        }
        if (v.equals(home_page_menu_beento)) {
            Intent in;
            in = new Intent(this, BeenToPage.class);
            startActivity(in);
        }
        if (v.equals(home_page_menu_history)) {
            Intent in;
            in = new Intent(this, HistoryPage.class);
            startActivity(in);
        }
        if (v.equals(home_page_menu_settings)) {
            Intent in;
            in = new Intent(this, SettingsPage.class);
            startActivity(in);
        }
    }


    //HTTP REQUEST TO GET LIST OF CUISINES
    class innerHttpRequestGetCuisines extends AsyncTask<String, Void, Void> {

        private String returnEntry;
        private boolean finished;

        public void readResponse(BufferedReader in) {
            String tmp = "";
            StringBuffer response = new StringBuffer();
            do {
                try {
                    tmp = in.readLine();
                } catch (IOException ex) {

                }
                if (tmp != null) {
                    response.append(tmp);
                }
            } while (tmp != null);
            returnEntry = response.toString();
        }

        public void sendPostRequest(String where) {
            URL loc = null;
            HttpURLConnection conn = null;
            InputStreamReader is;
            BufferedReader in;
            try {
                loc = new URL(where);
            } catch (MalformedURLException ex) {
                return;
            }
            try {
                conn = (HttpURLConnection) loc.openConnection();
                conn.setRequestProperty("Accept", " application/json");
                conn.setRequestProperty("user-key", "API"/*ADD ZOMATO API KEY HERE*/);
                is = new InputStreamReader(conn.getInputStream(), "UTF-8");
                in = new BufferedReader(is);
                readResponse(in);
            } catch (IOException ex) {
                Log.e("IOException", ex.toString());
            } finally {
                conn.disconnect();
            }
        }

        public String getReturnEntry() {
            if (!finished) {
                return "Hold tight!";
            }
            return returnEntry;
        }

        public JSONObject getResultAsJSON() {
            JSONArray jarr = null;
            JSONObject object = null;
            if (finished == false) {
                return null;
            }
            try {
                object = new JSONObject(returnEntry);
                //jarr = object.getJSONArray("cuisines");
                //Log.d ("JSONARRAY",jarr.toString());
            } catch (JSONException ex) {
                Log.d("Output", "Error is " + ex.getMessage());
            }
            return object;
        }

        @Override
        protected void onPostExecute(Void result) {
            finished = true;
            Log.d("Output", returnEntry+"");
            //After request is back
            if(returnEntry!=null) {
                try {
                    taskCuisines.get();
                    JSONObject jsonArrayObject;
                    JSONObject tmp;
                    JSONObject tmp2;
                    try {
                        jsonArrayObject = taskCuisines.getResultAsJSON();
                        if(jsonArrayObject!=null) {
                            jsonCuisineArray = jsonArrayObject.getJSONArray("cuisines");
                            cuisineArray.clear();
                            for (int i = 0; i < jsonCuisineArray.length(); i++) {
                                tmp = jsonCuisineArray.getJSONObject(i);
                                Log.d("tmp obj", tmp.toString());
                                tmp2 = tmp.getJSONObject("cuisine");
                                //Storing id:name into cuisine_hashmap
                                cuisine_hashmap.put(tmp2.getInt("cuisine_id"), tmp2.getString("cuisine_name"));
                                cuisineArray.add(tmp2.getString("cuisine_name"));
                            }
                            Log.d("cuisine_hashmap", cuisine_hashmap.toString());
                            //Updating adapter for spinner with new cuisines
                            typeDropDownAdapter.notifyDataSetChanged();
                            goButton.setEnabled(true);
                        }
                    } catch (JSONException ex) {
                        Log.d("Exception", ex.toString());
                    }
                    //TEST END
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }else{
                cuisineArray.clear();
                cuisineArray.add(0,"There are no restaurants nearby");
                typeDropDownAdapter.notifyDataSetChanged();
                //Toast.makeText(getApplicationContext(), "There are no restaurants in your area", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            finished = false;
            sendPostRequest(params[0]);
            return null;
        }
    }


        @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        //Check to see if permission is granted for location. If not close app and toast why
        if ((grantResults[0] == PackageManager.PERMISSION_DENIED)) {
            Toast.makeText(this, "Location permissions is a must for the app", Toast.LENGTH_SHORT).show();
            finish();
            Intent in = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(in);
        }
        //If permission granted reload the app
        else {
            finish();
            startActivity(getIntent());
        }
    }
}
