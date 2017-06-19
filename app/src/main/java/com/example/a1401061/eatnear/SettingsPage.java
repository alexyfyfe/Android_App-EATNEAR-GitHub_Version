package com.example.a1401061.eatnear;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
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

public class SettingsPage extends AppCompatActivity implements View.OnClickListener {

    //Setting up header bar and menu
    private Button headerQuestionMark;
    private TextView headerTitle;
    private Spinner headerMenu;
    private  ArrayAdapter<String> menuAdapter;
    private int hidingItemIndex;
    //Popup Help
    private PopupWindow settingPageHelp;
    private LayoutInflater layoutInflater;
    private RelativeLayout relativeLayout;
    //Width/Height of device
    private int width;
    private int height;
    private View help_background;

    //Database
    DatabaseUtility databaseUtility;

    //Variables for textviews that will change from database
    private TextView noOfWishlistNum;
    private TextView noOfBeenToNum;
    private TextView noOfHistoryNum;

    private Button deleteButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);

        //Back Button
        backButton = (Button) this.findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        //Delete Button
        deleteButton = (Button) this.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(this);

        //Popup Help
        relativeLayout = (RelativeLayout) findViewById(R.id.activity_settings_page);

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
                    startActivity(new Intent(SettingsPage.this, WishListPage.class));
                }
                if(position == 2){
                    startActivity(new Intent(SettingsPage.this, BeenToPage.class));
                }
                if(position == 3){
                    startActivity(new Intent(SettingsPage.this, HistoryPage.class));
                }
                if(position == 4){
                    startActivity(new Intent(SettingsPage.this, SettingsPage.class));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //DO NOTHING
            }
        });

        //Database
        databaseUtility = new DatabaseUtility(this);

        //Initialising textviews with numbers from database
        noOfWishlistNum = (TextView) findViewById(R.id.noOfWishlistNum);
        noOfBeenToNum = (TextView) findViewById(R.id.noOfBeenToNum);
        noOfHistoryNum = (TextView) findViewById(R.id.noOfHistoryNum);
        noOfWishlistNum.setText(databaseUtility.getNumRestaurantsWishList()+"");
        noOfBeenToNum.setText(databaseUtility.getNumRestaurantsBeenTo()+"");
        noOfHistoryNum.setText(databaseUtility.getNumRestaurantsHistory()+"");

    }

    @Override
    public void onClick(View v) {
        if(v.equals(backButton)){
            this.finish();
        }
        if(v.equals(deleteButton)){
            databaseUtility.deleteAllData();
            finish();
            startActivity(getIntent());
        }
        //Popup Help
        if(v.equals(headerQuestionMark)){
            layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.activity_settings_page_help, null);

            settingPageHelp = new PopupWindow(container, width, height, true);
            settingPageHelp.showAtLocation(relativeLayout, Gravity.NO_GRAVITY, 0, 0);
            help_background = (View) container.findViewById(R.id.help_background);
            //Hide popup if click anywhere
            help_background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    settingPageHelp.dismiss();
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
