package com.icicleWorks.bingooo;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, LocationListener {

    public static final String TAG = "bingooo";
    public static final String EXTRA_ABOUT = "o podniku";
    public static final String EXTRA_NAME = "meno podniku";
    public static final String FAVORITES_MARKERS = "savedMarkers";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    GoogleMap mMap;
    public static LatLng position = null;
    Location myPosition;

    List<Float> vOkoliList = new ArrayList<Float>();
    boolean isLogged;

    EditText rozsahEdit;

    //Facebook
    private static final int SPLASH = 0;
    private static final int MAP = 1;
    private Fragment[] fragments = new Fragment[2];
    private boolean isResumed = false;
    //Facebook

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.icicleWorks.bingooo",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        //Facebook
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences("bingooFB", 0);
        isLogged = preferences.getBoolean("isLogged", isLogged);

        FragmentManager fm = getSupportFragmentManager();
        fragments[SPLASH] = fm.findFragmentById(R.id.splashFragment);
        fragments[MAP] = fm.findFragmentById(R.id.map);
        android.support.v4.app.FragmentTransaction transaction;
        transaction = fm.beginTransaction();
        transaction.hide(fragments[SPLASH]);
        transaction.hide(fragments[MAP]);
        transaction.commit();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //Facebook

        rozsahEdit = (EditText) findViewById(R.id.editText);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        setUpMapIfNeeded();

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                loadMarkers(latLng.latitude, latLng.longitude, "hotel", "you", "Popis podniku ktorý sa zobrazí1 ;1%;3%;6%;mKontak1&");
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                position = marker.getPosition();
                vyberPodnik(marker);
            }
        });


        // Setting a custom info window adapter for the google map
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker arg0) {

                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);

                // Getting the position from the marker
                LatLng latLng = arg0.getPosition();

                // Getting reference to the TextView to set latitude
                TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);

                // Getting reference to the TextView to set longitude
                TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);

                // Setting the latitude
                tvLng.setText(arg0.getTitle());

                // Setting the longitude
                String[] getSnippet = arg0.getSnippet().split(";");
                String snippet = getSnippet[0];
                if (getSnippet[0].length() > 50)    snippet = getSnippet[0].substring(0, 50);
                if (snippet.length() < getSnippet[0].length())  snippet += "...";
                tvLat.setText(snippet);

                // Returning the view containing InfoWindow contents
                return v;

            }
        });

        rozsahEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkDistance();
            }
        });


        mMap.setMyLocationEnabled(true);
        mMap.getMyLocation();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        saveMarkers();

        getMyPosition();
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
        isResumed = true;
        setUpMapIfNeeded();
        saveMarkers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHelper.onPause();
        isResumed = false;
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    public void saveMarkers() {
        String allUsers = "";
        try {
            // Create a URL for the desired page
            URL url = new URL("http://www.bingooo.eu/users/allUsers.bingooo");

            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;

            while ((str = in.readLine()) != null) {
                // str is one line of text; readLine() strips the newline character(s)
                allUsers += str;
            }
            in.close();
            getAllMarkers(allUsers);
        } catch (MalformedURLException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    public void getAllMarkers(String names){
        String[] allUsers = names.split("#");
        String saveUsers = "";
        for (int i = 0; i<allUsers.length; i++){
            try {
                // Create a URL for the desired page
                URL url = new URL("http://www.bingooo.eu/users/" +allUsers[i] +"/" +allUsers[i] +".bingooo");

                // Read all the text returned by the server
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String str;

                while ((str = in.readLine()) != null) {
                    // str is one line of text; readLine() strips the newline character(s)
                    saveUsers += str;
                }
                Log.d(TAG, "allUsers.lenght: " +allUsers.length);
                Log.d(TAG, "saveUsers: " +saveUsers);
                getMarkers(saveUsers);
                in.close();
            } catch (MalformedURLException e) {
                Log.e(TAG, e.toString());
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                showFragment(MAP, false);
                break;
            case 2:
                break;
            case 3:
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setIcon(R.drawable.ic_bingooo);
        actionBar.setTitle("");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.sattelite_map) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            return true;
        }
        else if (id == R.id.normal_map){
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        // Creating a LatLng object for the current location
        // Getting latitude of the current location
        double latitude = location.getLatitude();

        // Getting longitude of the current location
        double longitude = location.getLongitude();

        //myPosition = new LatLng(latitude, longitude);

        // Showing the current location in Google Map
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));

        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private Location getLastBestLocation() {
        LocationManager mLocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public void getMarkers(String allMarkers){
        try {
            String[] stringArray = allMarkers.split("&");
            double latitude = 1, longitude = 1;

            for (int f = 0; f < stringArray.length; f++) {
                String[] read = stringArray[f].split("#");

                latitude = Double.parseDouble(read[0]);
                longitude = Double.parseDouble(read[1]);
                String mName, mSnippet, mCategory;
                mName = read[2];
                mCategory = read[3];
                mSnippet = read[4];
                loadMarkers(latitude, longitude, mCategory, mName, mSnippet);
            }

        }  catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public void loadMarkers(double latitude ,double longitude, String mCategory, String mTitle, String mSnippet){

        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(mTitle)
                .flat(true)
                .snippet(mSnippet);

        if (mCategory.equals("pizza"))  markerOptions = markerOptions
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.c_pizzaria));
        else if (mCategory.equals("bar"))   markerOptions = markerOptions
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.c_bar));
        else if (mCategory.equals("caffe")) markerOptions = markerOptions
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.c_coffee));
        else if (mCategory.equals("restaurant"))    markerOptions = markerOptions
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.c_restaurant));
        else if (mCategory.equals("hotel")) markerOptions = markerOptions
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.c_hotel));
        else if (mCategory.equals("superMarket"))   markerOptions = markerOptions
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.c_supermarket));
        else if (mCategory.equals("gas"))   markerOptions = markerOptions
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.c_gas));

        LatLng mPosition = new LatLng(latitude, longitude);
        myPosition = getLastBestLocation();

        float[] distance = new float[1];
        try{
            Location.distanceBetween(mPosition.latitude, mPosition.longitude,
                    myPosition.getLatitude(), myPosition.getLongitude(), distance);
        }
        catch (Exception e){
            Log.e(TAG, e.toString());
        }
        mMap.addMarker(markerOptions);


        vOkoliList.add(distance[0]);
        checkDistance();
    }

    public void checkDistance(){
        rozsahEdit = (EditText) findViewById(R.id.editText);
        TextView pPodnikov = (TextView) findViewById(R.id.pPodnikov);
        int a = 0;
        float rozsah = 0.1f;
        try {
             rozsah = Float.parseFloat(rozsahEdit.getText().toString()) * 1000;
        }
        catch (Exception e){
            Log.e(TAG, e.toString());
        }
        float[] vOkoli = new float[vOkoliList.size()];

        for (int f = 0; f < vOkoliList.size(); f++){
            vOkoli[f] = vOkoliList.get(f);
            if (vOkoli[f] < rozsah){
                a++;
            }
        }

        switch (a){
            case 1:
                pPodnikov.setText("km je " +a +" podnik");
                break;
            case 2: case 3 : case 4:
                pPodnikov.setText("km sú " +a +" podniky");
                break;
            default:
                pPodnikov.setText("km je " +a +" podnikov");
        }
    }


    // Mapy

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

    }

    public void vyberPodnik (Marker marker){
        Intent intent = new Intent(this, PodnikActivity2.class);
        String message = marker.getSnippet();
        intent.putExtra(EXTRA_ABOUT, message);
        intent.putExtra(EXTRA_NAME, marker.getTitle());
        startActivity(intent);
    }

    public void getMyPosition (){
        try {
            LatLng mP = new LatLng(myPosition.getLatitude(), myPosition.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mP));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }

        catch (Exception e){
            Toast.makeText(this, "Prosím zapnite si GPS", Toast.LENGTH_SHORT).show();
        }
    }

    //Facebook
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback =
            new Session.StatusCallback() {
                @Override
                public void call(Session session,
                                 SessionState state, Exception exception) {
                    onSessionStateChange(session, state, exception);
                }
            };

    private void showFragment(int f, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fm.beginTransaction();
        transaction.show(fragments[f]);
        transaction.setCustomAnimations(R.animator.slide_left, R.animator.slide_left);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    private void removeFragment(int f, boolean addToBackStack){
        FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fm.beginTransaction();
        transaction.hide(fragments[f]);
        transaction.setCustomAnimations(R.animator.slide_left, R.animator.slide_left);
        if (!addToBackStack) transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e(MainActivity.TAG, String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i(MainActivity.TAG, "Success!");
                removeFragment(SPLASH, true);
                showFragment(MAP, false);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        // Only make changes if the activity is visible
        if (isResumed) {
            FragmentManager manager = getSupportFragmentManager();
            // Get the number of entries in the back stack
            int backStackSize = manager.getBackStackEntryCount();
            // Clear the back stack
            for (int i = 0; i < backStackSize; i++) {
                manager.popBackStack();
            }
            if (state.isOpened()) {
                // If the session state is open:
                // Show the authenticated fragment
                removeFragment(SPLASH, true);
                showFragment(MAP, false);
            } else if (state.isClosed()) {
                // If the session state is closed:
                // Show the login fragment
                showFragment(SPLASH, false);
                removeFragment(MAP, true);
            }
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Session session = Session.getActiveSession();

        if (session != null && session.isOpened()) {
            // if the session is already open,
            // try to show the selection fragment
            removeFragment(SPLASH, true);
            showFragment(MAP, false);

        } else {
            // otherwise present the splash screen
            // and ask the person to login.
            showFragment(SPLASH, false);
            removeFragment(MAP, true);
        }
    }

    //Facebook
}
