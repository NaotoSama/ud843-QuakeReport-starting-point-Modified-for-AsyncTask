package com.example.android.quakereport;

/**
 * 這個自定義的Class目的是要定義模具和定義模具擺放的位置
 */


public class Earthquake {

    private double mMagnitude; //Another advantage to storing the earthquake magnitude as a double is that we can do math calculations easily. For example, if we wanted to find the average magnitude of all the earthquakes, we can easily sum up all the magnitudes and divide by the number of earthquakes. We wouldn’t be able to do math calculations if the magnitude values were text Strings.

    private String mLocation;

    private long mTimeInMilliseconds;

    private String mUrl;  //Website URL of the earthquake


    /**
     * Constructs a new {@link Earthquake} object.
     * @param magnitude is the magnitude (size) of the earthquake
     * @param location is the city location of the earthquake
     * @param timeInMilliseconds is the time in milliseconds (from the Epoch) when the earthquake happened
     * @param url is the website URL to find more details about the earthquake
     */
    //Call the constructor method to initialize the global member variables at the top based on the values we pass into the constructor.
    public Earthquake(double magnitude, String location, long timeInMilliseconds, String url) {
        mMagnitude = magnitude;
        mLocation = location;
        mTimeInMilliseconds = timeInMilliseconds;
        mUrl = url;
    }


    //The global member variables at the top are private, so we need to create public Getter methods for other classes to access them.
    public double getmMagnitude() {
        return mMagnitude;
    }

    public String getmLocation() {
        return mLocation;
    }

    public long getmTimeInMilliseconds() {
        return mTimeInMilliseconds;
    }

    public String getmUrl() {   //Returns the website URL to find more information about the earthquake.
        return mUrl;
    }

}
