package com.example.android.quakereport;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity); //導入settings_activity佈局(Fragment)，整個settings頁面就變成一個Fragment
                                                    //以下再透過EarthquakePreferenceFragment導入外掛或Widget(settings_main佈局/PreferenceScreen)
    }


    /** It's not a great experience for our user that they have to click on the minimum magnitude preference to find out what its current setting is.
     It'd be much nicer if, when we opened our Setting Activity, we could see the value of our preference right below the preference name, and when we change it, we see the summary update immediately.
     It's often useful for the app to know immediately when a preference is changed, especially when that preference change should have some visible impact on the UI.
     To do this, our PreferenceFragment can implement the OnPreferenceChangeListener interface to get notified when a preference changes.
     Then when a single Preference has been changed by the user and is about to be saved, the onPreferenceChange() method will be invoked with the key of the preference that was changed.
     Note that this method returns a boolean, which allows us to prevent a proposed preference change by returning false.
     First declare that the EarthquakePreferenceFragment class should implement the OnPreferenceChangeListener interface, and override the onPreferenceChange() method.
     The code in onPreferenceChange() method takes care of updating the displayed preference summary after it has been changed.
     因為 OnPreferenceChangeListener 是一個接口，所以需要在 EarthquakePreferenceFragment 類名後添加 implements 表示在這個類內實現接口*/
    public static class EarthquakePreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        //Override the onCreate() method to use the settings_main XML resource that we defined earlier.
        //在onCreate中把剛剛做好的PreferenceScreen (settings_main.xml)加進去
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            //We need to update the preference summary when the settings activity is launched.
            //Given the key of a preference, we can use PreferenceFragment's findPreference() method to get the Preference object,
            //and setup the preference using a helper method called bindPreferenceSummaryToValue().
            //在啟動設置活動後，仍需要更新偏好摘要。如果給定偏好項的key，就可以用 PreferenceFragment 的 findPreference() 方法來獲取偏好對象，然後使用 bindPreferenceSummaryToValue()來設置偏好。
            //在 onCreate 內透過 findPreference 找到偏好項並導入輔助方法(bindPreferenceSummaryToValue)
            Preference minMagnitude = findPreference(getString(R.string.settings_min_magnitude_key)); //透過 findPreference取得偏好項的識別key，找到minMagnitude這個偏好項的欄位
            bindPreferenceSummaryToValue(minMagnitude); // 叫bindPreferenceSummaryToValue輔助方法去套用到minMagnitude這個偏好項的欄位上
        }

        @Override
        //在這裡將下面bindPreferenceSummaryToValue輔助方法傳入的偏好項的預設數值透過setSummary 顯示在偏好項標題的下方。
        public boolean onPreferenceChange(Preference preference, Object value) {  //宣告onPreferenceChange方法並導入兩個arguments:偏好項以及數值物件
            String stringValue = value.toString(); //onPreferenceChange已透過下面的bindPreferenceSummaryToValue輔助方法載入了監聽器監聽到的(用戶輸入的)預設數值，所以只須把數值(value)轉換成字符，並命名為stringValue
            preference.setSummary(stringValue);  //透過setSummary把預設數值顯示在偏好項標題的下方。
            return true;
        }

        //we need to define the bindPreferenceSummaryToValue() helper method to set the current EarthquakePreferenceFragment instance as the listener on each preference.
        //We also read the current value of the preference stored in the SharedPreferences on the device,
        //and display that in the preference summary (so that the user can see the current value of the preference).
        //在此輔助方法內，設置導入的偏好項的監聽器，創建偏好項的SharedPreferences對象並通過 getString 獲取偏好項的值，最後傳給上面的onPreferenceChange方法來處理。
        private void bindPreferenceSummaryToValue(Preference preference) {  //這是輔助方法
            preference.setOnPreferenceChangeListener(this); //對偏好項綁定監聽器
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext()); //叫監聽器去監聽偏好項的內容
            String preferenceString = preferences.getString(preference.getKey(), ""); //去取得偏好項的識別key，獲取該識別key下的預設數值，將該值命名為preferenceString
            onPreferenceChange(preference, preferenceString); //把預設數值傳給上面的onPreferenceChange方法
        }
    }

}