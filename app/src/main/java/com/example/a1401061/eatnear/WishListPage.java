package com.example.a1401061.eatnear;

import android.content.Context;
import android.content.Intent;
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

import java.util.HashMap;

//Ratings on this page will be from when the restaurant was added to the wishlist and not live
//as this would require a lot of calls to the API and need a lot of internet data.

public class WishListPage extends AppCompatActivity implements View.OnClickListener {

    private ViewGroup historyScrollViewLayout;
    private TableLayout wishlist_tablelayout;
    private TableRow row;
    private Button backButton;

    //Hashmap
    private HashMap<Integer, RestaurantObj> restaurantHashMap;

    //Database
    DatabaseUtility databaseUtility;
    private LayoutInflater inflater;

    //Setting up header bar and menu
    private Button headerQuestionMark;
    private TextView headerTitle;
    private Spinner headerMenu;
    private ArrayAdapter<String> menuAdapter;
    private int hidingItemIndex;
    //Popup Help
    private PopupWindow wishlistPageHelp;
    private LayoutInflater layoutInflater;
    private RelativeLayout relativeLayout;
    //Width/Height of device
    private int width;
    private int height;
    private View help_background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish_list_page);

        databaseUtility = new DatabaseUtility(this);

        //Popup Help
        relativeLayout = (RelativeLayout) findViewById(R.id.activity_wish_list_page);

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
                    startActivity(new Intent(WishListPage.this, WishListPage.class));
                }
                if(position == 2){
                    startActivity(new Intent(WishListPage.this, BeenToPage.class));
                }
                if(position == 3){
                    startActivity(new Intent(WishListPage.this, HistoryPage.class));
                }
                if(position == 4){
                    startActivity(new Intent(WishListPage.this, SettingsPage.class));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //DO NOTHING
            }
        });

        //Setting up button with click listner
        backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        //Getting restaurantID:restaurantName HASHMAP from database
        restaurantHashMap = databaseUtility.getAllRestaurantsWishList();
        Log.d("hashsize", restaurantHashMap.size() + "");

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        wishlist_tablelayout = (TableLayout) findViewById(R.id.wishlist_tablelayout);

        //Looping through hashmap and adding to view
        for (final Integer key : restaurantHashMap.keySet()) {
            Log.d("hashmap", key + " " + restaurantHashMap.get(key).getRestaurantName()+" "+restaurantHashMap.get(key).getRating());
            row = new TableRow(this);
            row.setGravity(Gravity.CENTER);
            row.setTag(key);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in;
                    Bundle passedData = new Bundle();
                    //Passing restaurantID
                    passedData.putInt("restaurantID", key);
                    in = new Intent(WishListPage.this, RestaurantInfoPage.class);
                    in.putExtras(passedData);
                    startActivity(in);
                }
            });
            View wishlistRow = inflater.inflate(R.layout.wishlist_row, row);
            TextView restaurantNameText;
            restaurantNameText = (TextView) wishlistRow.findViewById(R.id.restaurantNameText);
            restaurantNameText.setText(restaurantHashMap.get(key).getRestaurantName());
            RatingBar wishlistRatingBar;
            wishlistRatingBar = (RatingBar) wishlistRow.findViewById(R.id.wishlistRatingBar);
            wishlistRatingBar.setNumStars(5);
            if (restaurantHashMap.get(key).getRating()>0){
                wishlistRatingBar.setRating(restaurantHashMap.get(key).getRating());
            }else{
                wishlistRatingBar.setVisibility(View.GONE);
            }

            //Adding newly created row to table
            wishlist_tablelayout.addView(row);
        }

    }

    @Override
    public void onClick(View v) {
        if (v.equals(backButton)) {
            this.finish();
        }
        //Popup Help
        if(v.equals(headerQuestionMark)){
            layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.activity_wish_list_page_help, null);

            wishlistPageHelp = new PopupWindow(container, width, height, true);
            wishlistPageHelp.showAtLocation(relativeLayout, Gravity.NO_GRAVITY, 0, 0);
            help_background = (View) container.findViewById(R.id.help_background);
            //Hide popup if click anywhere
            help_background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    wishlistPageHelp.dismiss();
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
}