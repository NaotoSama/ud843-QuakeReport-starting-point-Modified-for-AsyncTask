package com.example.android.quakereport;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

// We need to say that EarthquakeActivity implements the LoaderCallbacks interface,
// along with a generic parameter specifying what the loader will return (in this case an Earthquake).
public class EarthquakeActivity extends AppCompatActivity implements LoaderCallbacks<List<Earthquake>> {

    private static final String LOG_TAG = EarthquakeActivity.class.getName();

    /** URL for earthquake data from the USGS dataset */
    private static final String USGS_REQUEST_URL =
            "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&orderby=time&minmag=6&limit=10";

    /**
     * Constant value for the earthquake loader ID. We can choose any integer.
     * We need to specify an ID for our loader. This is only really relevant if we were using multiple loaders in the same activity.
     * We can choose any integer number, so we choose the number 1.
     */
    private static final int EARTHQUAKE_LOADER_ID = 1;

    /** Adapter for the list of earthquakes */
    private EarthquakeAdapter mAdapter;

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;



    /** 以下的App運作流程: (1) 將ListView對接和EmptyView、Adapter和OnItemClickListener
     *                   (2) 檢查網路狀態為連線或斷線==>用if/else statement設置Loader以及斷線時的錯誤訊息。確定有連線時再加載loader來抓數據，若斷線則顯示斷線的錯誤訊息。此外，因為佈局中有添加"加載中"的符號(loading indicator)，所以要讓App在斷線時隱藏"加載中"的符號
     *                   (3) 跑Loader的3個子方法(onCreateLoader、onLoadFinished、onLoaderReset)==>在Loader加載完(onLoadFinished)的階段設置無數據可加載時的錯誤訊息。此外，因為佈局中有添加"加載中"的符號，所以要讓App在加載器加載完數據後隱藏"加載中"的符號。
     *                   (4) 在onLoadFinished階段設置清除舊數據並更新ListView的數據(若有數據可加載的話)
     *                   (5) 在onLoadReset階段設置清除數據，以免用戶重回到App畫面時看到舊的內容
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);


        // Find a reference to the {@link ListView} in the layout
        ListView earthquakeListView = (ListView) findViewById(R.id.list);

        /** Now we need to hook up the empty view to the ListView. We can use the ListView setEmptyView() method.
         *  We can also make the empty state TextView be a global variable (在上面), so we can refer to it in a later method.
         *  The TextView class was also automatically imported into the java file, as soon as we used that class. */
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        earthquakeListView.setEmptyView(mEmptyStateTextView);

        // Create a new adapter that takes an empty list of earthquakes as input
        mAdapter = new EarthquakeAdapter(this, new ArrayList<Earthquake>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        earthquakeListView.setAdapter(mAdapter);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected earthquake.
        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current earthquake that was clicked on
                Earthquake currentEarthquake = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri earthquakeUri = Uri.parse(currentEarthquake.getmUrl());

                // Create a new intent to view the earthquake URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });


        /** 在這裡設置檢查網路連線狀態，若有連線則加載loader並獲取數據，若斷線則在App畫面顯示錯誤訊息*/
        // Get a reference to the ConnectivityManager to check the state of network connectivity
        //透過ConnectivityManager檢查連網狀態(有連線/斷線)
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        // 取得網路狀態為連線或斷線
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        // 若有連線，則透過getLoaderManager()開始加載loader並加載數據
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // 初始化loader
            // To retrieve an earthquake, we need to get the loader manager and tell the loader manager to initialize the loader with the specified ID,
            // the second argument allows us to pass a bundle of additional information, which we'll skip.
            // The third argument is what object should receive the LoaderCallbacks (and therefore, the data when the load is complete!)
            // - which will be this activity because this activity implements the LoaderCallbacks interface.
            // This code goes inside the onCreate() method of the EarthquakeActivity, so that the loader can be initialized as soon as the app opens.
            loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);

        // 若斷線，則顯示錯誤訊息
        // Otherwise, display error
        // First, hide loading indicator so error message will be visible
        } else {
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);  //我們有在佈局中添加"加載中"的符號(loading indicator)，所以要讓App在斷線時隱藏"加載中"的符號，否則該符號會跟下面的錯誤訊息重疊出現在畫面上。

            // Set empty state text to display no connection error message when there is no Internet connection.
            //斷線時的錯誤訊息
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }

    }


    /** Then we need to override the three methods specified in the LoaderCallbacks interface. */
    //We need onCreateLoader(), for when the LoaderManager has determined that the loader with our specified ID isn't running, so we should create a new one.
    @Override
    public Loader<List<Earthquake>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
        return new EarthquakeLoader(this, USGS_REQUEST_URL);
    }

    //We need onLoadFinished(), where we'll do exactly what we did in onPostExecute(), and use the earthquake data to update our UI - by updating the dataset in the adapter.
    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> earthquakes) {

        /** Hide the loading indicator (by setting visibility to View.GONE) after the first load is completed - when onLoadFinished() is called.
         * 我們有在佈局中添加"加載中"的符號(loading indicator)，所以要讓App在加載器加載完數據後隱藏"加載中"的符號。
         * 在程序跑到onLoadFinished之前，加載符號會一直顯示*/
        // Hide loading indicator because the data has been loaded.
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);


        /** To avoid the “No earthquakes found.” message blinking on the screen when the app first launches, we can leave the empty state TextView blank,
         *  until the first load completes. In the onLoadFinished callback method, we can set the text to be the string “No earthquakes found.” */
        // Set empty state text to display "No earthquakes found." when there is no data available to fetch.
        // 無數據可加載時會自動顯示的錯誤訊息。(EmptyView有一套自動化機制，若有下載到數據則會隱藏EmptyView並自動顯示ListView的內容，若無數據則隱藏ListView並顯示EmptyView)
        mEmptyStateTextView.setText(R.string.no_earthquakes);


        // Clear the adapter of previous earthquake data
        mAdapter.clear();


        // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (earthquakes != null && !earthquakes.isEmpty()) {
            mAdapter.addAll(earthquakes);
        }
    }

    //We need onLoaderReset(), we're being informed that the data from our loader is no longer valid.
    //This isn't actually a case that's going to come up with our simple loader, but the correct thing to do is to remove all the earthquake data from our UI by clearing out the adapter’s data set.
    @Override
    public void onLoaderReset(Loader<List<Earthquake>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }


    /** Override a couple methods to inflate the settings menu, and respond when users click on our menu item
     * 在MainActivity中用onCreateOptionsMenu建立選單，並用onOptionItemSelected來控制點擊事件 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {  //這個方法是要用來生成選單頁面
        getMenuInflater().inflate(R.menu.main, menu); // 設置要用哪個menu檔做為選單
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {  //這個方法是要設置用戶在主畫面單擊「settings」鈕時跳轉到「settings」頁面
        int id = item.getItemId();  // 取得點選項目的id
        if (id == R.id.action_settings) {  // 依照id判斷點了哪個項目並做相應事件
            Intent settingsIntent = new Intent(this, SettingsActivity.class); //按下「settings」鈕要做的事：把畫面從EarthquakeActivity跳轉到SettingsActivity
            startActivity(settingsIntent);  //開始上述的動作
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}