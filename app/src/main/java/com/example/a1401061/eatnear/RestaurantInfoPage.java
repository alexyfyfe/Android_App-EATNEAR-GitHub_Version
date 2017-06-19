package com.example.a1401061.eatnear;

import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.concurrent.ExecutionException;

public class RestaurantInfoPage extends AppCompatActivity implements View.OnClickListener {

    private RatingBar ratingBar;
    private TextView restaurantInfoName;
    private TextView restaurantInfoText;
    private Bundle data;
    Intent intent;
    private Integer restaurantID;
    private JSONObject restaurantObj;

    private innerHttpRequest taskGetRestaurantInfo;

    private JSONObject restaurantInfoObj;

    //Restaurant Details
    private String restaurantName;
    private String address;
    private String average_cost_for_two;
    private String currency;
    private String aggregate_rating;
    private String rating_text;
    private String menu_url;
    private String photos_url;

    //Menu buttons at bottom of page
    private ToggleButton wishlistToggleButton;
    private CheckBox beenCheckBox;
    private Button menuButton;
    private Button reviewsButton;
    private Button photosButton;
    private Button backButton;

    //Database
    DatabaseUtility databaseUtility;

    //Setting up header bar and menu
    private Button headerQuestionMark;
    private TextView headerTitle;
    private Spinner headerMenu;
    private ArrayAdapter<String> menuAdapter;
    private int hidingItemIndex;
    //Popup Help
    private PopupWindow restaurantinfoPageHelp;
    private LayoutInflater layoutInflater;
    private RelativeLayout relativeLayout;
    //Width/Height of device
    private int width;
    private int height;
    private View help_background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_info_page);

        intent = getIntent();
        data = intent.getExtras();
        restaurantID = (data.getInt("restaurantID"));

        /*Run Async Task to get information on restaurant selected*/
        if (taskGetRestaurantInfo == null) {
            taskGetRestaurantInfo = new innerHttpRequest();
            taskGetRestaurantInfo.execute("https://developers.zomato.com/api/v2.1/restaurant?res_id=" + restaurantID);
        }

        //Popup Help
        relativeLayout = (RelativeLayout) findViewById(R.id.activity_restaurant_info_page);

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
        CustomAdapterMenu dataAdapter = new CustomAdapterMenu(this, android.R.layout.simple_spinner_item,  getResources().getStringArray(R.array.header_menu_items), hidingItemIndex);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        headerMenu.setAdapter(dataAdapter);
        headerMenu.setDropDownVerticalOffset(210);
        headerMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //Adding where each option takes you to correct page
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView)view).setText(null); //Stop text from displaying in spinner once selected
                if(position == 1){
                    startActivity(new Intent(RestaurantInfoPage.this, WishListPage.class));
                }
                if(position == 2){
                    startActivity(new Intent(RestaurantInfoPage.this, BeenToPage.class));
                }
                if(position == 3){
                    startActivity(new Intent(RestaurantInfoPage.this, HistoryPage.class));
                }
                if(position == 4){
                    startActivity(new Intent(RestaurantInfoPage.this, SettingsPage.class));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //DO NOTHING
            }
        });

        //Database
        databaseUtility = new DatabaseUtility(this);

        restaurantInfoName = (TextView) this.findViewById(R.id.restaurantInfoName);
        ratingBar = (RatingBar) this.findViewById(R.id.ratingBar);
        restaurantInfoText = (TextView) this.findViewById(R.id.restaurantInfoText);

        //Setting button variables and add click listeners
        beenCheckBox = (CheckBox) this.findViewById(R.id.beenCheckBox);
        beenCheckBox.setOnClickListener(this);
        //If restaurantID in database check checkbox
        if(databaseUtility.checkRestaurantsIDBeenTo(restaurantID)==true){
            beenCheckBox.setChecked(true);
        }

        wishlistToggleButton = (ToggleButton) this.findViewById(R.id.wishlistToggleButton);
        wishlistToggleButton.setOnClickListener(this);
        //If restaurantID in database check checkbox
        if(databaseUtility.checkRestaurantsIDWishList(restaurantID)==true){
            wishlistToggleButton.setChecked(true);
        }

        menuButton = (Button) this.findViewById(R.id.menuButton);
        menuButton.setOnClickListener(this);

        photosButton = (Button) this.findViewById(R.id.photosButton);
        photosButton.setOnClickListener(this);

        reviewsButton = (Button) this.findViewById(R.id.reviewsButton);
        reviewsButton.setOnClickListener(this);

        backButton = (Button) this.findViewById(R.id.backButton);
        backButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.equals(wishlistToggleButton)){
            if(wishlistToggleButton.isChecked()){
                databaseUtility.setRestaurantsWishList(restaurantID, restaurantName, ratingBar.getRating());
            }else{
                databaseUtility.deleteRestaurantsIDWishList(restaurantID);
            }
        }
        if(v.equals(beenCheckBox)){
            if(beenCheckBox.isChecked()){
                databaseUtility.setRestaurantsBeenTo(restaurantID, restaurantName);
            }else{
                databaseUtility.deleteRestaurantsIDBeenTo(restaurantID);
            }
        }
        if(v.equals(reviewsButton)){
            Intent in;
            Bundle passedData = new Bundle();
            //Passing restaurantID
            passedData.putInt("restaurantID", restaurantID);
            passedData.putFloat("aggregate_rating", Float.parseFloat(aggregate_rating));
            in = new Intent(this, RatingsPage.class);
            in.putExtras(passedData);
            startActivity(in);
        }
        if(v.equals(backButton)){
            this.finish();
        }
        if(v.equals(menuButton)){
            Intent in;
            Bundle passedData = new Bundle();
            //Passing menu_url
            passedData.putString("menu_url", menu_url);
            in = new Intent(this, MenuPage.class);
            in.putExtras(passedData);
            startActivity(in);
        }
        if(v.equals(photosButton)){
            Intent in;
            Bundle passedData = new Bundle();
            //Passing photos_url
            passedData.putString("photos_url", photos_url);
            in = new Intent(this, PhotosPage.class);
            in.putExtras(passedData);
            startActivity(in);
        }
        //Popup Help
        if(v.equals(headerQuestionMark)){
            layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.activity_restaurant_info_page_help, null);

            restaurantinfoPageHelp = new PopupWindow(container, width, height, true);
            restaurantinfoPageHelp.showAtLocation(relativeLayout, Gravity.NO_GRAVITY, 0, 0);
            help_background = (View) container.findViewById(R.id.help_background);
            //Hide popup if click anywhere
            help_background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    restaurantinfoPageHelp.dismiss();
                }
            });
        }
        //Title
        if(v.equals(headerTitle)){
            Intent in;
            in = new Intent(this, HomePage.class);
            startActivity(in);
        }
    }

    class innerHttpRequest extends AsyncTask<String, Void, Void> {

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
                taskGetRestaurantInfo.get();
                try {
                    restaurantObj = taskGetRestaurantInfo.getResultAsJSON();
                    if (restaurantObj == null) {
                        Log.d("Restaurants", "ERROR");
                        return;
                    }
                    Log.d("restaurantObj obj", restaurantObj.toString());
                    //Setting data to variables
                    restaurantName = restaurantObj.getString("name");
                    restaurantInfoName.setText(restaurantName);
                    address = restaurantObj.getJSONObject("location").getString("address");
                    average_cost_for_two = restaurantObj.getString("average_cost_for_two");
                    currency = restaurantObj.getString("currency");
                    photos_url = restaurantObj.getString("photos_url");
                    menu_url = restaurantObj.getString("menu_url");
                    JSONObject ratingObj = restaurantObj.getJSONObject("user_rating");
                    aggregate_rating = ratingObj.getString("aggregate_rating");
                    rating_text = ratingObj.getString("rating_text");

                    //Setting text to app
                    restaurantInfoText.setText("");
                    restaurantInfoText.append("Average cost for two: "+currency+average_cost_for_two);
                    restaurantInfoText.append("\nRating: "+rating_text);
                    restaurantInfoText.append("\nAddress: "+address);
                    ratingBar.setRating(Float.parseFloat(aggregate_rating));
                    //Add Restaurant to history
                    databaseUtility.setRestaurantsHistory(restaurantID, restaurantName);
                } catch (JSONException ex) {
                    Log.d("Exception", ex.toString());
                }
                //TEST END
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
}



