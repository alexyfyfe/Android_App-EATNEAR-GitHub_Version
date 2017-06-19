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
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;

public class HistoryPage extends AppCompatActivity implements View.OnClickListener {

    private ViewGroup historyScrollViewLayout;
    private TableLayout history_tablelayout;
    private TableRow row;
    private Button backButton;

    //Hashmap
    private HashMap<Integer, String> restaurantHashMap;

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
    private PopupWindow historyPageHelp;
    private LayoutInflater layoutInflater;
    private RelativeLayout relativeLayout;
    //Width/Height of device
    private int width;
    private int height;
    private View help_background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_page);

        //Popup Help
        relativeLayout = (RelativeLayout) findViewById(R.id.activity_history_page);

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
                    startActivity(new Intent(HistoryPage.this, WishListPage.class));
                }
                if(position == 2){
                    startActivity(new Intent(HistoryPage.this, BeenToPage.class));
                }
                if(position == 3){
                    startActivity(new Intent(HistoryPage.this, HistoryPage.class));
                }
                if(position == 4){
                    startActivity(new Intent(HistoryPage.this, SettingsPage.class));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //DO NOTHING
            }
        });

        databaseUtility = new DatabaseUtility(this);

        //Setting up button with click listner
        backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        //Getting restaurantID:restaurantName HASHMAP from database
        restaurantHashMap = databaseUtility.getAllRestaurantsHistory();
        Log.d("hashsize", restaurantHashMap.size() + "");

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        history_tablelayout = (TableLayout) findViewById(R.id.history_tablelayout);

        //Looping through hashmap and adding to view
        for (final Integer key : restaurantHashMap.keySet()) {
            Log.d("hashmap", key + " " + restaurantHashMap.get(key));
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
                    in = new Intent(HistoryPage.this, RestaurantInfoPage.class);
                    in.putExtras(passedData);
                    startActivity(in);
                }
            });
            View historyRow = inflater.inflate(R.layout.history_row, row);
            TextView restaurantNameText;
            restaurantNameText = (TextView) historyRow.findViewById(R.id.restaurantNameText);
            restaurantNameText.setText(restaurantHashMap.get(key));
            //Adding newly created row to table
            history_tablelayout.addView(row);
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
            ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.activity_history_page_help, null);

            historyPageHelp = new PopupWindow(container, width, height, true);
            historyPageHelp.showAtLocation(relativeLayout, Gravity.NO_GRAVITY, 0, 0);
            help_background = (View) container.findViewById(R.id.help_background);
            //Hide popup if click anywhere
            help_background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    historyPageHelp.dismiss();
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