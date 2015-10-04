package edu.android.com.sunshine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.sunshine.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.android.com.sunshine.data.WeatherContract;
import edu.android.com.sunshine.sync.SunshineSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    // Identifies a particular Loader being used in this component
    private static final int FORECAST_LOADER = 0;
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };
    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    ListView mForecastListView;
    private ForecastAdapter mForecastAdapter;
    private int mPosition = ListView.INVALID_POSITION;
    private String SELECTED_KEY = "selected_position";
    private boolean mUseTodayLayout;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    public ForecastFragment() {
    }

    public static double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex)
            throws JSONException {

        JSONObject weather = new JSONObject(weatherJsonStr);
        JSONArray list = weather.getJSONArray("list");
        JSONObject dayInfo = list.getJSONObject(dayIndex);
        JSONObject temperatureInfo = dayInfo.getJSONObject("temp");
        return temperatureInfo.getDouble("max");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        getLoaderManager().initLoader(FORECAST_LOADER, null, this);


        // The CursorAdapter will take data from our cursor and populate the ListView
        // However, we cannot use FLAG_AUTO_REQUERY since it is deprecated, so we will end
        // up with an empty list the first time we run.
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);

        mForecastListView.setAdapter(mForecastAdapter);

        mForecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());

                    ((Callback) getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, cursor.getLong(COL_WEATHER_DATE)));

                }
                mPosition = position;
            }
        });

        View emptyView = rootView.findViewById(R.id.listview_forecast_empty);

        mForecastListView.setEmptyView(emptyView);

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        return rootView;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       /* super.onCreateOptionsMenu(menu, inflater);*/
        inflater.inflate(R.menu.forecastfragment, menu);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mPosition != ListView.INVALID_POSITION)
            outState.putInt(SELECTED_KEY, mPosition);

        super.onSaveInstanceState(outState);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_map) {

            openPreferredLocationMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void openPreferredLocationMap() {

        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if (null != mForecastAdapter) {
            Cursor c = mForecastAdapter.getCursor();
            if (null != c) {
                c.moveToPosition(0);
                String posLat = c.getString(COL_COORD_LAT);
                String posLong = c.getString(COL_COORD_LONG);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                }
            }

        }
    }

    // since we read the location when we create the loader, all we need to do is restart things
    void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }


    private void updateWeather() {
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle args) {

        switch (loaderID) {

            case FORECAST_LOADER:
                String locationSetting = Utility.getPreferredLocation(getActivity());

                // Sort order:  Ascending, by date.
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
                Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                        locationSetting, System.currentTimeMillis());

                return new CursorLoader(
                        getActivity(),   // Parent activity context
                        weatherForLocationUri,        // Table to query
                        FORECAST_COLUMNS,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        sortOrder);             // Default sort order
            default:
                return null;

        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mForecastAdapter.swapCursor(data);

        if (mPosition != ListView.INVALID_POSITION) {

            mForecastListView.smoothScrollToPosition(mPosition);

        } else if (!mUseTodayLayout && mForecastAdapter.getCount() != 0) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mPosition = 0;
                    mForecastListView.performItemClick(
                            mForecastListView.getChildAt(mPosition),
                            mPosition,
                            mForecastAdapter.getItemId(mPosition));
                }
            });
        }

        updateEmptyView();

    }

    /*
       Updates the empty list view with contextually relevant information that the user can
       use to determine why they aren't seeing weather.
    */
    private void updateEmptyView() {
        if (mForecastAdapter.getCount() == 0) {
            TextView tv = (TextView) getView().findViewById(R.id.listview_forecast_empty);
            if (null != tv) {
                // if cursor is empty, why? do we have an invalid location
                int message = R.string.empty_forecast_list;
                @SunshineSyncAdapter.LocationStatus int location = Utility.getLocationStatus(getActivity());
                switch (location) {
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                        message = R.string.empty_forecast_list_server_down;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                        message = R.string.empty_forecast_list_server_error;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                        message = R.string.empty_forecast_list_invalid_location;
                        break;
                    default:
                        message = R.string.empty_forecast_list_no_network;
                        break;
                }
                tv.setText(message);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    @Override
    public void onResume() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        super.onResume();

    }

    @Override
    public void onPause() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_location_status_key))) {
            updateEmptyView();
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }
}
