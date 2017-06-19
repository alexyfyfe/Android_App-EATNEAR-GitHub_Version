package com.example.a1401061.eatnear;

/**
 * Created by alexy on 03/04/2017.
 * Object to Store RestaurantName and Rating
 */

public class RestaurantObj {
    private String restaurantName;
    private Float rating;
    public RestaurantObj(String aRestaurantName, Float aRating){
        restaurantName = aRestaurantName;
        rating = aRating;
    }

    public String getRestaurantName(){
        return restaurantName;
    }

    public Float getRating(){
        return rating;
    }
}
