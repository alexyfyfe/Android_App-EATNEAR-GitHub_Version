package com.example.a1401061.eatnear;

import android.content.Context;
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
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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

public class RatingsPage extends AppCompatActivity implements View.OnClickListener {

    private LayoutInflater inflater;
    private ViewGroup linearLayoutReviews;
    private TableLayout reviews_tablelayout;
    private TableRow row;

    //HTTP Request
    private innerHttpRequestReviews taskGetReviews;
    private JSONObject reviewObj;

    //Variables from previous page
    private Bundle data;
    Intent intent;
    private int restaurantID;

    private TextView pendingText;

    //Variables on ratings page
    private Button backButton;
    private RatingBar averageRatingBar;
    //Value of the average rating for rating bar
    private Float aggregate_rating;

    //Setting up header bar and menu
    private Button headerQuestionMark;
    private TextView headerTitle;
    private Spinner headerMenu;
    private ArrayAdapter<String> menuAdapter;
    private int hidingItemIndex;
    //Popup Help
    private PopupWindow ratingsPageHelp;
    private LayoutInflater layoutInflater;
    private RelativeLayout relativeLayout;
    //Width/Height of device
    private int width;
    private int height;
    private View help_background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings_page);

        //Getting data from RestaurantInfoPage
        intent = getIntent();
        data = intent.getExtras();
        restaurantID = (data.getInt("restaurantID"));
        aggregate_rating = (data.getFloat("aggregate_rating"));

        /*Run Async Task to get information on restaurant selected*/
        if (taskGetReviews == null) {
            taskGetReviews = new innerHttpRequestReviews();
            taskGetReviews.execute("https://developers.zomato.com/api/v2.1/reviews?res_id=" + restaurantID);
        }

        //Popup Help
        relativeLayout = (RelativeLayout) findViewById(R.id.activity_ratings_page);

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
                    startActivity(new Intent(RatingsPage.this, WishListPage.class));
                }
                if(position == 2){
                    startActivity(new Intent(RatingsPage.this, BeenToPage.class));
                }
                if(position == 3){
                    startActivity(new Intent(RatingsPage.this, HistoryPage.class));
                }
                if(position == 4){
                    startActivity(new Intent(RatingsPage.this, SettingsPage.class));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //DO NOTHING
            }
        });

        pendingText = (TextView) this.findViewById(R.id.pendingText);
        averageRatingBar = (RatingBar) this.findViewById(R.id.averageRatingBar);
        averageRatingBar.setRating(aggregate_rating);

        //Setting up button and lisener
        backButton = (Button) this.findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        //Setting up tablelayout
        reviews_tablelayout = (TableLayout) this.findViewById(R.id.reviews_tablelayout);

    }

    @Override
    public void onClick(View v) {
        if(v.equals(backButton)){
            this.finish();
        }
        //Popup Help
        if(v.equals(headerQuestionMark)){
            layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.activity_ratings_page_help, null);

            ratingsPageHelp = new PopupWindow(container, width, height, true);
            ratingsPageHelp.showAtLocation(relativeLayout, Gravity.NO_GRAVITY, 0, 0);
            help_background = (View) container.findViewById(R.id.help_background);
            //Hide popup if click anywhere
            help_background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ratingsPageHelp.dismiss();
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


    class innerHttpRequestReviews extends AsyncTask<String, Void, Void> {

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
                taskGetReviews.get();
                try {
                    reviewObj = taskGetReviews.getResultAsJSON();
                    if (reviewObj == null) {
                        Log.d("Restaurants", "ERROR");
                        return;
                    }
                    Log.d("reviewObj", reviewObj.toString());
                    int numOfReviews = reviewObj.getInt("reviews_shown");
                    if (numOfReviews == 0){
                        pendingText.setText(R.string.sorryNoReviews);
                        return;
                    }
                    //Hidding pendingtext
                    pendingText.setVisibility(View.GONE) ;

                    //Setting up variables
                    JSONArray jsonReviewsArray = reviewObj.getJSONArray("user_reviews");
                    Log.d("reviewArray", jsonReviewsArray.toString());
                    JSONObject userReviewObj;
                    String reviewer;

                    //DYNAMICALLY ADDING REVIEWS INTO SCROLLVIEW
                    inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    reviews_tablelayout = (TableLayout) findViewById(R.id.reviews_tablelayout);

                    for (int i = 0; i < jsonReviewsArray.length(); i++) {
                        //Variables inside review_row.xml
                        RatingBar reviewRatingBar;
                        TextView reviewerNameText;
                        TextView reviewText;

                        userReviewObj = jsonReviewsArray.getJSONObject(i).getJSONObject("review");
                        Log.d("userReviewObj", userReviewObj.toString());
                        row = new TableRow(RatingsPage.this);
                        row.setGravity(Gravity.CENTER);
                        View reviewRow = inflater.inflate(R.layout.review_row, row);
                        reviewRatingBar = (RatingBar) reviewRow.findViewById(R.id.reviewRatingBar);
                        float rating = (float)userReviewObj.getDouble("rating");
                        Log.d("rating",rating+"");
                        if(rating==0.0){
                            reviewRatingBar.setVisibility(View.GONE);
                        }
                        reviewRatingBar.setRating(rating);
                        reviewText = (TextView) reviewRow.findViewById(R.id.reviewText);
                        reviewText.setText(userReviewObj.getString("review_text"));
                        reviewerNameText = (TextView) reviewRow.findViewById(R.id.reviewerNameText);
                        reviewer = userReviewObj.getJSONObject("user").getString("name");
                        if(reviewer!=null){
                            reviewerNameText.setText(reviewer);
                        }else{
                            reviewer = userReviewObj.getJSONObject("user").getString("zomato_handle");
                            if(reviewer!=null){
                                reviewerNameText.setText(reviewer);
                            }
                        }
                        reviews_tablelayout.addView(row);
                    }
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
}
