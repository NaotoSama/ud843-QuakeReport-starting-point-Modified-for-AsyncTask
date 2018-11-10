package com.example.android.quakereport;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

// We need to say that EarthquakeActivity implements the LoaderCallbacks interface,
// along with a generic parameter specifying what the loader will return (in this case an Earthquake).
public class EarthquakeActivity extends AppCompatActivity implements LoaderCallbacks<List<Earthquake>> {

    private static final String LOG_TAG = EarthquakeActivity.class.getName();

    /** URL for earthquake data from the USGS dataset */
    private static final String USGS_REQUEST_URL =
            "http://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&orderby=time&minmag=6&limit=10";

    /**
     * Constant value for the earthquake loader ID. We can choose any integer.
     * We need to specify an ID for our loader. This is only really relevant if we were using multiple loaders in the same activity.
     * We can choose any integer number, so we choose the number 1.
     */
    private static final int EARTHQUAKE_LOADER_ID = 1;

    /** Adapter for the list of earthquakes */
    private EarthquakeAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        // Find a reference to the {@link ListView} in the layout
        ListView earthquakeListView = (ListView) findViewById(R.id.list);

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


        /**To retrieve an earthquake, we need to get the loader manager and tell the loader manager to initialize the loader with the specified ID,
         * the second argument allows us to pass a bundle of additional information, which we'll skip.
         * The third argument is what object should receive the LoaderCallbacks (and therefore, the data when the load is complete!) - which will be this activity.
         * This code goes inside the onCreate() method of the EarthquakeActivity, so that the loader can be initialized as soon as the app opens.
         */
        // Get a reference to the LoaderManager, in order to interact with loaders.
        LoaderManager loaderManager = getLoaderManager();

        // Initialize the loader. Pass in the int ID constant defined above and pass in null for
        // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
        // because this activity implements the LoaderCallbacks interface).
        loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);
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
}