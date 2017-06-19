package com.example.a1401061.eatnear;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by alexy on 06/03/2017.
 */

public class CustomAdapterMenu extends ArrayAdapter<String> {
    //Position to hide element in dropdown.
    private int hidingItemIndex;

    public CustomAdapterMenu(Context context, int textViewResourceId, String[] objects, int hidingItemIndex) {
        super(context, textViewResourceId, objects);
        this.hidingItemIndex = hidingItemIndex;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = null;
        //Hiding element in dropdown
        if (position == hidingItemIndex) {
            TextView tv = new TextView(getContext());
            tv.setHeight(0);
            tv.setVisibility(View.GONE);
            v = tv;
        } else {
            v = super.getDropDownView(position, null, parent);
            ((TextView)v).setGravity(Gravity.CENTER);
        }
        return v;
    }
}
