package com.example.a1401061.eatnear;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MapPage extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {

    //Google maps variables
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private GoogleMap mMap;
    private Spinner typeDropDown;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MapPage.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;

    private Marker previousMarker;


    //Restaurants Array
    private JSONArray jsonRestaurantsArray;

    //Boolean to run code once
    private Boolean runOnce;

    //Go button
    private Button goButton;

    //Spinner Variables
    private ArrayList<String> cuisineArray = new ArrayList<String>();
    ArrayAdapter<CharSequence> typeDropDownAdapter;

    //Variables to recieve data from homepage
    private String cuisine_name_choice;
    private TextView input;
    private Bundle data;
    private Intent intent;
    private HashMap<Integer, String> cuisine_hashmap = new HashMap<>();


    //cuisine_id selected
    private Integer selected_cuisine_id;

    //HTTP REQUESTS
    innerHttpRequestGetRestaurants taskGetRestaruants;
    innerHttpRequestGetCuisines taskCuisines;
    JSONArray jsonCuisineArray;

    //Setting up header bar and menu
    private Button headerQuestionMark;
    private TextView headerTitle;
    private Spinner headerMenu;
    private ArrayAdapter<String> menuAdapter;
    private int hidingItemIndex;
    //Popup Help
    private PopupWindow mapPageHelp;
    private LayoutInflater layoutInflater;
    private RelativeLayout relativeLayout;
    //Width/Height of device
    private int width;
    private int height;
    private View help_background;

    private int permissionRequestIdentifier;

    //Location Manager
    private LocationManager locationManager;
    private android.location.LocationListener locationListener;

    //Lat and Long Variables
    private Double latVal;
    private Double longVal;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_page);

        //Getting data from homepage
        intent = getIntent();
        data = intent.getExtras();
        cuisine_hashmap = (HashMap<Integer, String>) intent.getSerializableExtra("cuisine_hashmap");
        cuisine_name_choice = data.getString("cuisine_name_choice");
        selected_cuisine_id = getKeyByValue(cuisine_hashmap, cuisine_name_choice);
        latVal = data.getDouble("last_know_lat");
        longVal = data.getDouble("last_know_long");

        //Setting runOnce to false
        runOnce = false;



/************ BELLOW IS HARD CODE LOCATION DATA FOR ABERDEEN IF GPS EMULATION IS NOT WORKING ****************/
        //Getting JSON restaraunts
//        if (taskGetRestaruants == null) {
//            taskGetRestaruants = new innerHttpRequestGetRestaurants();
//            //Need to update location values
//            taskGetRestaruants.execute("https://developers.zomato.com/api/v2.1/search?lat=57.149651&lon=-2.099075&radius=5000&cuisines=" + selected_cuisine_id + "&sort=real_distance&order=asc");
//        }

        /*Run Async Task to get dropdown cuisines based on location of user*/
//        if (taskCuisines == null) {
//            taskCuisines = new innerHttpRequestGetCuisines();
//            taskCuisines.execute("https://developers.zomato.com/api/v2.1/cuisines?lat=57.149651&lon=-2.099075");
//        }
//***********************************************************************************************************************************************//

//Code to run once from previous pages last know lat long values.
        if(runOnce == false){
            runOnce = true;
            //Getting JSON restaraunts
            if (taskGetRestaruants == null) {
                taskGetRestaruants = new innerHttpRequestGetRestaurants();
                //Need to update location values
                taskGetRestaruants.execute("https://developers.zomato.com/api/v2.1/search?lat="+latVal+"+&lon="+longVal+"&radius=5000&cuisines=" + selected_cuisine_id + "&sort=real_distance&order=asc");
            }
            /*Run Async Task to get dropdown cuisines based on location of user*/
            if (taskCuisines == null) {
                taskCuisines = new innerHttpRequestGetCuisines();
                taskCuisines.execute("https://developers.zomato.com/api/v2.1/cuisines?lat="+latVal+"&lon="+longVal);
            }
        }

        // Obtain the SupportMapFragment and get notified when the cuisine_hashmap is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        /*GoButton*/
        goButton = (Button) this.findViewById(R.id.goButton);
        goButton.setOnClickListener(this);


        /*DROP DOWN LIST*/
        if (cuisine_name_choice.equals(null)) {
            cuisineArray.add(0, "Loading...");
        } else {
            cuisineArray.add(0, cuisine_name_choice);
        }
        typeDropDown = (Spinner) this.findViewById(R.id.typeDropDown);
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

        //Popup Help
        relativeLayout = (RelativeLayout) findViewById(R.id.activity_map_page);

        /*Width & Height of device*/
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;

        /*HEADER BAR*/
        headerQuestionMark = (Button) this.findViewById(R.id.headerQuestionMark);
        headerQuestionMark.setOnClickListener(this);
        headerTitle = (TextView) this.findViewById(R.id.headerTitle);
        headerTitle.setOnClickListener(this);

        /*HEADER MENU*/
        hidingItemIndex = 0; //Setting first item to hide
        headerMenu = (Spinner) this.findViewById(R.id.headerMenu);
        CustomAdapterMenu dataAdapter = new CustomAdapterMenu(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.header_menu_items), hidingItemIndex);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        headerMenu.setAdapter(dataAdapter);
        headerMenu.setDropDownVerticalOffset(210);
        headerMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //Adding where each option takes you to correct page
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) view).setText(null); //Stop text from displaying in spinner once selected
                if (position == 1) {
                    startActivity(new Intent(MapPage.this, WishListPage.class));
                }
                if (position == 2) {
                    startActivity(new Intent(MapPage.this, BeenToPage.class));
                }
                if (position == 3) {
                    startActivity(new Intent(MapPage.this, HistoryPage.class));
                }
                if (position == 4) {
                    startActivity(new Intent(MapPage.this, SettingsPage.class));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //DO NOTHING
            }
        });

        //Getting GPS location
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latVal = location.getLatitude();
                longVal = location.getLongitude();
                //Getting JSON restaraunts
                taskGetRestaruants = new innerHttpRequestGetRestaurants();
                //Need to update location values
                taskGetRestaruants.execute("https://developers.zomato.com/api/v2.1/search?lat=" + latVal + "&lon=" + longVal + "&radius=5000&cuisines=" + selected_cuisine_id + "&sort=real_distance&order=asc");
                taskCuisines = new innerHttpRequestGetCuisines();
                taskCuisines.execute("https://developers.zomato.com/api/v2.1/cuisines?lat=" + latVal + "&lon=" + longVal);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.INTERNET
                }, permissionRequestIdentifier);
                return;
            } else {
                locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
            }
        }

    }


    /**
     * Manipulates the cuisine_hashmap once available.
     * This callback is triggered when the cuisine_hashmap is ready to be used.``
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e("MapStyle", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapStyle", "Can't find style. Error: ", e);
        }
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

        //Setting previous marker as a random marker
        previousMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Hello world"));
        previousMarker.setVisible(false);


        //Enable my location button and marker
        if (ActivityCompat.checkSelfPermission(MapPage.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapPage.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //Adding animated camera movement to zoom into last known location
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latVal, longVal))
                .zoom(12)
                .bearing(0)
                .tilt(40)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.setMyLocationEnabled(true);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static <T, E> T getKeyByValue(HashMap<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        if (v == goButton) {
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
        if (v.equals(headerQuestionMark)) {
            layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.activity_map_page_help, null);

            mapPageHelp = new PopupWindow(container, width, height, true);
            mapPageHelp.showAtLocation(relativeLayout, Gravity.NO_GRAVITY, 0, 0);
            help_background = (View) container.findViewById(R.id.help_background);
            //Hide popup if click anywhere
            help_background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mapPageHelp.dismiss();
                }
            });
        }
        //Title
        if (v.equals(headerTitle)) {
            Intent in;
            in = new Intent(this, HomePage.class);
            startActivity(in);
        }
    }

    //When info window clicked new Intent to restaurant info page
    @Override
    public void onInfoWindowClick(Marker marker) {
        findViewById(android.R.id.content).playSoundEffect(SoundEffectConstants.CLICK);
        Integer retrievedRestaurantID = (Integer) marker.getTag();
        Log.d("ID:", "" + retrievedRestaurantID);
        Intent in;
        Bundle passedData = new Bundle();
        passedData.putInt("restaurantID", retrievedRestaurantID);
        in = new Intent(this, RestaurantInfoPage.class);
        in.putExtras(passedData);
        //Setting previous marker as a random marker
        previousMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Hello world"));
        previousMarker.setVisible(false);
        startActivity(in);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        findViewById(android.R.id.content).playSoundEffect(SoundEffectConstants.CLICK);
        if (marker.equals(previousMarker)){
            Integer retrievedRestaurantID = (Integer) marker.getTag();
            Log.d("ID:", "" + retrievedRestaurantID);
            Intent in;
            Bundle passedData = new Bundle();
            passedData.putInt("restaurantID", retrievedRestaurantID);
            in = new Intent(this, RestaurantInfoPage.class);
            in.putExtras(passedData);
            //Setting previous marker as a random marker
            previousMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(0, 0))
                    .title("Hello world"));
            previousMarker.setVisible(false);
            startActivity(in);
        }else{
            previousMarker = marker;
            marker.showInfoWindow();
            //Adding animated camera movement to move to clicked restaurant
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(marker.getPosition())
                    .zoom(mMap.getCameraPosition().zoom)
                    .bearing(mMap.getCameraPosition().bearing)
                    .tilt(mMap.getCameraPosition().tilt)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        return true;
    }


    //HTTP REQUEST TO GET LOCATION OF RESTAURANTS
    class innerHttpRequestGetRestaurants extends AsyncTask<String, Void, Void> {

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
                conn.setRequestProperty("user-key","API"/*ADD ZOMATO API KEY HERE*/);
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
            Log.d("Output", returnEntry);
            //After request is back
            try {
                taskGetRestaruants.get();
                JSONObject jsonArrayObject;
                JSONObject restaurantObj;
                JSONObject restaurantInfoObj;
                JSONObject locationObj;
                Integer restaurantID;
                Double restaurantLat;
                Double restaurantLon;
                String restaurantName;
                try {
                    jsonArrayObject = taskGetRestaruants.getResultAsJSON();
                    jsonRestaurantsArray = jsonArrayObject.getJSONArray("restaurants");
                    if (jsonRestaurantsArray == null) {
                        Log.d("Restaurants", "No restaurants found");
                        return;
                    }
                    for (int i = 0; i < jsonRestaurantsArray.length(); i++) {
                        restaurantObj = jsonRestaurantsArray.getJSONObject(i);
                        Log.d("restaurantObj obj", restaurantObj.toString());
                        restaurantInfoObj = restaurantObj.getJSONObject("restaurant");
                        Log.d("restaurantInfoObj obj", restaurantInfoObj.toString());
                        restaurantID = restaurantInfoObj.getInt("id");
                        restaurantName = restaurantInfoObj.getString("name");
                        locationObj = restaurantInfoObj.getJSONObject("location");
                        restaurantLat = locationObj.getDouble("latitude");
                        restaurantLon = locationObj.getDouble("longitude");
                        Log.d("Restaurant", "Restaurant Info: " + restaurantID + " Lat:" + restaurantLat + " Lon:" + restaurantLon);
                        LatLng restaurant = new LatLng(restaurantLat, restaurantLon);
                        //Adding marker to map if location is not 0,0 else ignore
                        if (restaurantLat != 0 || restaurantLon != 0) {
                            Marker marker = mMap.addMarker(new MarkerOptions().position(restaurant).title(restaurantName + ""));
                            //Adding restaurant ID to marker
                            marker.setTag(restaurantID);
                            //Changing icon of marker
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant_icon));
                        }
                    }
                    Log.d("cuisine_hashmap", cuisine_hashmap.toString());
                    typeDropDownAdapter.notifyDataSetChanged();
                } catch (JSONException ex) {
                    Log.d("Exception", ex.toString());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            finished = false;
            sendPostRequest(params[0]);
            return null;
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
                        //Setting current spinner selection
                        int choosenCuisinePos = typeDropDownAdapter.getPosition(cuisine_name_choice);
                        typeDropDown.setSelection(choosenCuisinePos);
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

}