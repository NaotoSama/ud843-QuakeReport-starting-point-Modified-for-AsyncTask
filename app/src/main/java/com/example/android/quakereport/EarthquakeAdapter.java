package com.example.android.quakereport;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 這個Adapter(調度器)的Class目的是要對接自定義的custom_earthquake_list_item佈局，以及對接ArrayList，並把ArrayList中擺放好的各模具設置到(對接到)相對應的View上。
 * 這其中另外包括將"時間"要素的秒數格式化成常人可理解的時間日期格式。
 */


public class EarthquakeAdapter extends ArrayAdapter<Earthquake> {

    /**
     * Two helper methods, formatDate() and formatTime(), that we created to accept a Date object and return an appropriately formatted date string using SimpleDateFormat.
     * These two methods are used in the lower part of the getView method below where we produce the formatted date and time strings to display in the corresponding TextViews
     **/
    // Return the formatted date string (i.e. "Mar 3, 1984") from a Date object. 利用SimpleDateFormat來自定義日期格式
    private String formatDate(Date dateObject) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy"); // stand-alone month => L:1 LL:01 LLL:Jan LLLL:January LLLLL:J
        return dateFormat.format(dateObject);
    }

    // Return the formatted date string (i.e. "4:30 PM") from a Date object. 利用SimpleDateFormat來自定義時間格式
    private String formatTime(Date dateObject) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a"); //a代表AM或PM
        return timeFormat.format(dateObject);
    }


    /**
     * Create a helper method called formatMagnitude() that takes a double value as input and returns the formatted string.
     * The helper method initializes a DecimalFormat object instance with the pattern string “0.0”.
     * Then in the getView() method of the adapter, we can read the magnitude value from the current Earthquake object,
     * format the decimal into a string, and then update the TextView to display the value.
     * Return the formatted magnitude string showing 1 decimal place (i.e. "3.2") from a decimal magnitude value.
     */
    private String formatMagnitude(double magnitude) {
        DecimalFormat magnitudeFormat = new DecimalFormat("0.0");
        return magnitudeFormat.format(magnitude);
    }

    /**
     * Define a private helper method called getMagnitudeColor(double magnitude) that returns the correct color value based on the current earthquake’s magnitude value.
     **/
    //将颜色 资源 ID 转换为颜色整数值int。Need to convert the color resource ID into a color integer value.
    //The method would take a double magnitude value as input and return a color integer value.
    //the switch statement cannot accept a double value, so we should convert our decimal magnitude value into an integer.
    private int getMagnitudeColor(double magnitude) {       //宣告一個獲取震度顏色的方法(getMagnitudeColor)，設定什麼震度對接什麼顏色，並將獲取的顏色屬性轉換成int。在方法的括弧中導入一個argument(素材)，也就是屬性為double的magnitude變數，將會在接下來的statement中使用這個變數。
        int magnitudeColorResourceId;
        int magnitudeFloor = (int) Math.floor(magnitude);  //Math.floor()最大整數，取小於這個數的最大整數。We can use the Math class to do some handy mathematical calculations. In this case, we can take the “floor” of the decimal magnitude value. This means finding the closest integer less than the decimal value. The floor of the value 1.2 would be the integer 1. Informally, for a positive decimal number, you can think of it as truncating the part of the number after the decimal point.
        switch (magnitudeFloor) {                          //Within each case, we set the value of the magnitudeColorResourceId variable to be one of the color resources that we defined the colors.xml file.
            case 0:
            case 1:
                magnitudeColorResourceId = R.color.magnitude1;  //In Java code, you can refer to the colors that you defined in the colors.xml file using the color resource ID such as R.color.magnitude1, R.color.magnitude2.
                break;                                          //For case 0 and 1, we fall through to the same logic, which is to use the R.color.magnitude1 color. This was a design decision to use the same color for earthquakes with magnitude less than 2.
            case 2:
                magnitudeColorResourceId = R.color.magnitude2;
                break;
            case 3:
                magnitudeColorResourceId = R.color.magnitude3;
                break;
            case 4:
                magnitudeColorResourceId = R.color.magnitude4;
                break;
            case 5:
                magnitudeColorResourceId = R.color.magnitude5;
                break;
            case 6:
                magnitudeColorResourceId = R.color.magnitude6;
                break;
            case 7:
                magnitudeColorResourceId = R.color.magnitude7;
                break;
            case 8:
                magnitudeColorResourceId = R.color.magnitude8;
                break;
            case 9:
                magnitudeColorResourceId = R.color.magnitude9;
                break;
            default:
                magnitudeColorResourceId = R.color.magnitude10plus;  //We also have a default case where any earthquake with magnitude higher than 10 will use the R.color.magnitude10plus color resource.
                break;
        }
        return ContextCompat.getColor(getContext(), magnitudeColorResourceId);  //ContextCompat.getColor()返回的值都是整数，它返回的int实际上就是你想要的颜色（一个十六进制颜色作为整数），在大多数情况下，你会被要求输入该颜色。 R.color.xxx int实际上只是一个ID，引用了你的资源中的十六进制/整数颜色，依赖于你正在使用的API可能会被要求提供该ID，但在幕后，肯定会有ContextCompat.getColor()那个ID。
        /**
         *Once we find the right color resource ID, we still have one more step to convert it into an actual color value.
         * Remember that color resource IDs just point to the resource we defined, but not the value of the color.
         * For example, R.layout.earthquake_list_item is a reference to tell us where the layout is located. It’s just a number, not the full XML layout.
         * You can call ContextCompat getColor() to convert the color resource ID into an actual integer color value, and return the result as the return value of the getMagnitudeColor() helper method.
         **/
    }


    /**
     * We will be using the split(String string) method in the String class to split the original string at the position where the text “ of “ occurs.
     * The result will be a String containing the characters PRIOR to the “ of ” text and a String containing the characters AFTER the “ of “ text.
     * Since we’ll frequently need to refer to the “ of “ text, we can define a static final String constant (that is a global variable) at the top of the EarthquakeAdapter class.
     **/
    private static final String LOCATION_SEPARATOR = " of ";


    public EarthquakeAdapter(Activity context, ArrayList<Earthquake> earthquakeArrayList) {
        super(context, 0, earthquakeArrayList);
    }


    /**
     * Create a getView method to control how the listView gets created.
     * */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {                                                                  // When getView is called, we can check to see if we can use a recycled view.
            listItemView = LayoutInflater.from(getContext()).inflate(                               // Otherwise, we inflate a new ListItem layout defined in the custom_earthquake_list_item.xml file.
                    R.layout.custom_earthquake_list_item, parent, false);
        }

        Earthquake currentEarthquake = getItem(position);                                           // We can use the position parameter passed in to get a reference to the appropriate Earthquake object from the list of earthquakes.

        TextView magnitudeTextView = (TextView) listItemView.findViewById(R.id.earthquake_magnitude); //Bind the data from the Earthquake object to the views in the custom_earthquake_list_item layout, and set the corresponding data onto them.
        // Format the magnitude to show 1 decimal place
        String formattedMagnitude = formatMagnitude(currentEarthquake.getmMagnitude());
        // Display the magnitude of the current earthquake in that TextView
        magnitudeTextView.setText(formattedMagnitude);

        // Set the proper background color on the magnitude circle.
        // Fetch the background from the TextView, which is a GradientDrawable.
        GradientDrawable magnitudeCircle = (GradientDrawable) magnitudeTextView.getBackground();  // 从 TextView 获取背景，该背景屬性是一个 GradientDrawable。我們先前已在自定義佈局中的震度View中設置了背景，因此要透過JAVA語法去抓震度View (magnitudeTextView)的背景(getBackground())

        // Get the appropriate background color based on the current earthquake magnitude
        int magnitudeColor = getMagnitudeColor(currentEarthquake.getmMagnitude());                 // 在各位階(currentEarthquake)上抓取個個震度(getmMagnitude)，然後把各震度代入getMagnitudeColor方法，根据自定义Switch逻辑获取相应的背景颜色，getMagnitudeColor()返回颜色的整数值

        // Set the color on the magnitude circle     // 设置视图背景的颜色
        magnitudeCircle.setColor(magnitudeColor);
        /**
         * 設置震度顏色的整個流程：
         * 1.先用findViewById找到震度的View
         * 2.用getBackground()獲取震度View的背景
         * 3.在最上面宣告獲取震度顏色的方法(getMagnitudeColor)，並將顏色ID轉換成數值int
         * 4.對各個位階上的震度(currentEarthquake.getmMagnitude())套用getMagnitudeColor所設置的對應顏色int
         * 5.將顏色(int)設置到震度的背景(magnitudeCircle)上。此時震度的背景就和顏色int對接完成
         **/



        /**
         * We get the original location String from the Earthquake object and store that in a variable.
         * We also create new variables (primary location and location offset) to store the resulting Strings.
         **/
        String originalLocation = currentEarthquake.getmLocation();
        String primaryLocation;
        String locationOffset;


        /**
         * Let’s dive into the details of the split. If the original location String is “74km NW of Rumoi, Japan” and we split the string using the LOCATION_SEPARATOR,
         * then we will get a String array as the return value. In the String array, the 0th element of the array is “74km NW” and the 1st element of the array is “Rumoi, Japan”.
         * Note that we also add the “ of “ text back to the 0th element of the array, so the locationOffset will say “74km NW of “.
         * There is still the issue that some location Strings don’t have a location offset. Hence, we should check if the original location String contains the LOCATION_SEPARATOR first,
         * before we decide to split the string with that separator. If there is no LOCATION_SEPARATOR in the original location String,
         * then we can assume that we should we use the “Near the” text as the location offset, and just use the original location String as the primary location.
         **/
        if (originalLocation.contains(LOCATION_SEPARATOR)) {
            String[] parts = originalLocation.split(LOCATION_SEPARATOR);
            locationOffset = parts[0] + LOCATION_SEPARATOR;
            primaryLocation = parts[1];
        } else {
            locationOffset = getContext().getString(R.string.near_the);
            primaryLocation = originalLocation;
        }


        /**
         * Once we have the 2 separate Strings, we can display them in the 2 TextViews in the list item layout.
         **/
        TextView primaryLocationView = (TextView) listItemView.findViewById(R.id.primary_location);
        primaryLocationView.setText(primaryLocation);

        TextView locationOffsetView = (TextView) listItemView.findViewById(R.id.location_offset);
        locationOffsetView.setText(locationOffset);



        /**
         * Within the EarthquakeAdapter, we write the following code to produce the formatted strings to display in the corresponding TextViews.
         * We get the time from the current Earthquake object, using currentEarthquake.getTimeInMilliseconds(),
         * and pass that into the Date constructor to form a new Date object.
         **/
        // First create a new Date object from the time in milliseconds of the earthquake. The Date object requires a long (here it is "getmTimeInMilliseconds") as input.
        Date dateObject = new Date(currentEarthquake.getmTimeInMilliseconds());
        // Then Format the date string (i.e. "Mar 3, 1984") 呼叫formatDate method並導入dateObject，並使之變成名為formattedDate的String
        String formattedDate = formatDate(dateObject);  // formatDate method的定義在最上方，利用SimpleDateFormat來自定義日期格式
        // Find the TextView with view ID date
        TextView dateTextView = (TextView) listItemView.findViewById(R.id.earthquake_date);
        // Display the date of the current earthquake in that TextView
        dateTextView.setText(formattedDate);
        // Then Format the time string (i.e. "4:30PM") 呼叫formatTime method並導入dateObject，並使之變成名為formattedTime的String
        String formattedTime = formatTime(dateObject);   // formatDate method的定義在最上方，利用SimpleDateFormat來自定義時間格式
        // Find the TextView with view ID time
        TextView timeView = (TextView) listItemView.findViewById(R.id.time);
        // Display the time of the current earthquake in that TextView
        timeView.setText(formattedTime);


        //Once everything is set, return the view to the caller, which is the ListView that will take all the list items and display them on the screen.
        return listItemView;
    }

}
