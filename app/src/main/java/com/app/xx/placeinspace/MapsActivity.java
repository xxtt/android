package com.app.xx.placeinspace;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.xml.Place;
import com.app.xx.placeinspace.adapter.NavDrawerListAdapter;
import com.app.xx.placeinspace.model.NavDrawerItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.FIFOLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class MapsActivity extends Activity {

    private static final int YOUTUBE = 1;
    private static final int NEWS = 2;
    public final static String PLACE = "PLACE";
    public final static String NEWS_FEED = "NEWS_FEED";
    public static final String LOCATION = "LOCATION";

    private GoogleMap mMap;
    private Place currentMarker;
    private List<Place> mPlaceList;
    private List<Integer> mCategoryList;
    private List<String> mTitleList;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavDrawerListAdapter adapter;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    private CharSequence mTitle;

    GoogleDirection gd;
    Polyline polyline;
    private Document mDirectionDoc = null;

    LatLng myLocation;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private LatLng placeLocation;
    private String language = Locale.getDefault().getLanguage();

    private int mDrawerState;

    MenuItem searchIcon;
    CustomAutoComplete searchBox;
    InputMethodManager imm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setUpMapIfNeeded();
        initDirections();
        initImageLoader();

        initDrawerLayout();

        String http = getIntent().getStringExtra(SplashScreen.HTTP);
        try {
            initPlaceList(http);
        } catch (JSONException e) {
            Toast.makeText(getBaseContext(), R.string.jsonError, Toast.LENGTH_LONG).show();
        }

        initMarkers();
        initDrawerMenu();

        if (savedInstanceState == null) displayView(0);

        animateCameraOnStart();

        initSearch();

    }

    private void initSearch() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        View view = getLayoutInflater().inflate(R.layout.actionbar_search, null);
        searchBox = (CustomAutoComplete) view.findViewById(R.id.search_box);

        searchBox.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mTitleList));

        searchBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                doSearch(false);
                int index = mTitleList.indexOf(((TextView) view).getText().toString());
                showMarker(mTitleList.get(index));
            }
        });

        searchBox.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                searchBox.showDropDown();
                return false;
            }
        });

        searchBox.setVisibility(View.GONE);

        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setCustomView(view);

    }

    private void animateCameraOnStart() {
        LatLng location = null;
        if (checkCurrentLocation()) location = myLocation;
        else {
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            String locationProvider = LocationManager.GPS_PROVIDER;
            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            if (lastKnownLocation != null) {
                location = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            }
        }
        if (location != null) mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12));

    }

    private void initDrawerLayout() {
        mTitle = getTitle();

        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.app_name,
                R.string.app_name
        ) {
            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                mDrawerState = newState;
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);

 /*       this.menu = menu;

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
*/
        searchIcon = menu.findItem(R.id.action_search);

        return super.onCreateOptionsMenu(menu);
    }

    private void initDrawerMenu() {
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
        navDrawerItems = new ArrayList<NavDrawerItem>();

        Set<Integer> categorySet = new HashSet<Integer>();
        categorySet.add(0);           // set "all places" category

        for (Place place : mPlaceList) {
            categorySet.add(place.getCategory());
        }

        mCategoryList = new ArrayList<Integer>(categorySet);
        Collections.sort(mCategoryList);

        for (Integer category : mCategoryList) {
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[category], navMenuIcons.getResourceId(category, -1)));
//            Log.i("init", "category is " + category);
        }
//      navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1), true, "50+"));

        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

    }

    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            displayView(position);
            removePolyLine();
        }
    }

    private void displayView(int position) {
        int category = mCategoryList.get(position);
        boolean visible;

        for (Place place : mPlaceList) {
            visible = (category == 0) || (place.getCategory() == category);
            place.getMarker().setVisible(visible);
        }

        mDrawerList.setItemChecked(position, true);
        mDrawerList.setSelection(position);
        setTitle(navDrawerItems.get(position).getTitle());
        mDrawerLayout.closeDrawer(mDrawerList);

    }

    ////////////// Drawer code starts /////////////////////

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_news:
                if (searchBox.getVisibility() == View.VISIBLE) doSearch(false);
                return startNewsActivity();

            case R.id.action_share:
                if (searchBox.getVisibility() == View.VISIBLE) doSearch(false);
                return startShareActivity();

            case R.id.action_search:
                doSearch(true);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean startNewsActivity() {
        ArrayList<Place> newsList = new ArrayList<Place>();
        for (Place place : mPlaceList) {
            if (!place.getNews().isEmpty()) newsList.add(place);
        }
        Intent intent = new Intent(this, NewsActivity.class);
        intent.putParcelableArrayListExtra(NEWS_FEED, newsList);
        startActivityForResult(intent, NEWS);
        return true;
    }

    private boolean startShareActivity() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getText(R.string.share_app_text));
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_app_with)));
        return true;
    }

    protected void doSearch(boolean option) {
        if (option) {
            searchIcon.setVisible(false);
            searchBox.setVisibility(View.VISIBLE);
            searchBox.requestFocus();
            searchBox.showDropDown();
            imm.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT);
        } else {
            searchBox.setText("");
            searchBox.setVisibility(View.GONE);
            searchIcon.setVisible(true);
            imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
        }
    }


    private void hideMenuItems(Menu menu, boolean visible) {
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(visible);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        boolean visible = mDrawerState != DrawerLayout.STATE_DRAGGING &&
                mDrawerState != DrawerLayout.STATE_SETTLING && !drawerOpen;

        if ((searchBox.getVisibility() == View.VISIBLE) && (!visible)) {
            doSearch(false);
        }

        hideMenuItems(menu, visible);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        } else if (searchBox.getVisibility() == View.VISIBLE) {
            doSearch(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();  // Sync the toggle state after onRestoreInstanceState has occurred.
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);  // Pass any configuration change to the drawer toggls
    }

    ///////////////Drawer code ends //////////////////

    private void initPlaceList(String http) throws JSONException {
        mPlaceList = new ArrayList<Place>();
        mTitleList = new ArrayList<String>();

        JSONObject json = new JSONObject(http);

        if (json.getInt("success") == 1) {

            JSONArray markers = json.getJSONArray("markers");

            for (int i = 0; i < markers.length(); i++) {
                JSONObject marker = markers.getJSONObject(i);
                int id = marker.getInt("id");
                String title = marker.getString("title");
                String about = getJsonAbout(marker);
                double x = marker.getDouble("x");
                double y = marker.getDouble("y");
                String yt = marker.getString("yt");
                int category = marker.getInt("category");
                String address = getJsonAddress(marker);
                String phone = marker.getString("phone");
                String link = marker.getString("link");
                String news = getJsonNews(marker);
                boolean smoking = marker.getInt("smoking") != 0;
                boolean baby = marker.getInt("baby") != 0;
                boolean parking = marker.getInt("parking") != 0;
                boolean music = marker.getInt("music") != 0;
                int bill = marker.getInt("bill");
                Place place = new Place(id, title, about, x, y, yt, category, address, phone, link, news, smoking, baby, parking, music, bill);
                place.setResourceId(navMenuIcons.getResourceId(category, 0));

                mPlaceList.add(place);
                mTitleList.add(title);
            }
        }
    }

    private String getJsonNews(JSONObject marker) throws JSONException {
        if (language.equals("uk")) {
            return marker.getString("news_ua");
        } else if (language.equals("ru")) {
            return marker.getString("news_ru");
        } else {
            return marker.getString("news_us");
        }
    }

    private String getJsonAbout(JSONObject marker) throws JSONException {
        if (language.equals("uk")) {
            return marker.getString("about_ua");
        } else if (language.equals("ru")) {
            return marker.getString("about_ru");
        } else {
            return marker.getString("about_us");
        }
    }

    private String getJsonAddress(JSONObject marker) throws JSONException {
        if (language.equals("uk")) {
            return marker.getString("address_ua");
        } else if (language.equals("ru")) {
            return marker.getString("address_ru");
        } else {
            return marker.getString("address_us");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {

            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMyLocationEnabled(true);

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                if (currentMarker != null) {
                    Marker show = currentMarker.getMarker();
                    if (show != null && show.isInfoWindowShown()) {
                        show.hideInfoWindow();
                        show.showInfoWindow();
                    }
                }
                return null;
            }

            @Override
            public View getInfoContents(final Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);
                TextView title = (TextView) v.findViewById(R.id.info_window_title);
                title.setText(marker.getTitle());
                TextView description = (TextView) v.findViewById(R.id.info_category);
                String text = null;
                String ytId = null;

                for (Place place : mPlaceList) {
                    if (marker.getTitle().equals(place.getTitle())) {
                        text = navMenuTitles[place.getCategory()];
                        ytId = place.getYouTubeId();
                        currentMarker = place;
                    }
                }

                description.setText(text);
                String url1 = String.format("http://img.youtube.com/vi/" + ytId + "/1.jpg");
                String url2 = String.format("http://img.youtube.com/vi/" + ytId + "/2.jpg");
                String url3 = String.format("http://img.youtube.com/vi/" + ytId + "/3.jpg");

                ImageView image1 = (ImageView) v.findViewById(R.id.image1);
                ImageView image2 = (ImageView) v.findViewById(R.id.image2);
                ImageView image3 = (ImageView) v.findViewById(R.id.image3);

                //  imageLoader.displayImage(url1, image1, options);
                //  imageLoader.displayImage(url2, image2, options);
                //    imageLoader.displayImage(url3, image3, options);

                imageLoader.displayImage(url1, image1, options, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        getInfoWindow(marker);
                    }
                });

                imageLoader.displayImage(url2, image2, options, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        getInfoWindow(marker);
                    }
                });

                imageLoader.displayImage(url3, image3, options, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        getInfoWindow(marker);
                    }
                });
                return v;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                mDirectionDoc = null;
                if (searchBox.getVisibility() == View.VISIBLE) doSearch(false);
                removePolyLine();
                placeLocation = new LatLng(currentMarker.getX(), currentMarker.getY());
                boolean directionSet = (isNetworkAvailable() && checkCurrentLocation());

                if (directionSet) {
                    gd.request(myLocation, placeLocation, GoogleDirection.MODE_WALKING);
                }

                Intent intent = new Intent(getBaseContext(), YouTubePlayerActivity.class);
                intent.putExtra(PLACE, currentMarker);
                intent.putExtra(LOCATION, directionSet);
                startActivityForResult(intent, YOUTUBE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case (YOUTUBE): {
                if (resultCode == RESULT_OK) {
                    if (currentMarker != null) {
                        currentMarker.getMarker().hideInfoWindow();
                    }
                    if (mDirectionDoc != null) {
                        polyline = mMap.addPolyline(gd.getPolyline(mDirectionDoc, 5, Color.argb(200, 115, 185, 255)));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
                        mDirectionDoc = null;
                    } else {
                        Toast.makeText(this, getResources().getText(R.string.no_direction), Toast.LENGTH_LONG).show();
                    }
                    break;
                }
            }
            case (NEWS): {
                if (resultCode == RESULT_OK) {
                    String title = ((Place) data.getParcelableExtra(MapsActivity.PLACE)).getTitle();
                    showMarker(title);
                    break;
                }
            }
        }
    }

    private void showMarker(String title) {
        if (currentMarker != null) currentMarker.getMarker().hideInfoWindow();
        for (Place place : mPlaceList) {
            if (place.getTitle().equals(title)) {
                place.getMarker().setVisible(true);
                place.getMarker().showInfoWindow();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(place.getX(), place.getY()), 15));
                currentMarker = place;
                return;
            }
        }
    }

    private boolean checkCurrentLocation() {
        myLocation = null;
        Location location = mMap.getMyLocation();

        if (location != null) {
            double dLatitude = location.getLatitude();
            double dLongitude = location.getLongitude();
            myLocation = new LatLng(dLatitude, dLongitude);
            return true;
        } else {
            return false;
        }
    }

    void removePolyLine() {
        if (polyline != null) {
            polyline.remove();
        }
    }

    private void initDirections() {

        gd = new GoogleDirection(this);

        gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
            public void onResponse(String status, Document doc, GoogleDirection gd) {
                mDirectionDoc = (status.equals(GoogleDirection.STATUS_OK)) ? doc : null;
            }
        });
    }

    private void initMarkers() {
        for (Place place : mPlaceList) {
            MarkerOptions options = new MarkerOptions();
            options.title(place.getTitle());
            options.icon(BitmapDescriptorFactory.fromResource(place.getIconResourceId()));
            options.position(new LatLng(place.getX(), place.getY()));
            place.setMarker(mMap.addMarker(options));
        }
    }

    private void initImageLoader() {
        int memoryCacheSize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            int memClass = ((ActivityManager)
                    getSystemService(Context.ACTIVITY_SERVICE))
                    .getMemoryClass();
            memoryCacheSize = (memClass / 8) * 1024 * 1024;
        } else {
            memoryCacheSize = 2 * 1024 * 1024;
        }

        options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.empty)        //    Display Stub Image
                .showImageForEmptyUri(R.drawable.empty)    //    If Empty image found
                .cacheInMemory()
                .cacheOnDisc().bitmapConfig(Bitmap.Config.RGB_565).build();

        final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this).threadPoolSize(5)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCacheSize(memoryCacheSize)
                .memoryCache(new FIFOLimitedMemoryCache(memoryCacheSize - 1000000))
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO).build();

        ImageLoader.getInstance().init(config);
        imageLoader = ImageLoader.getInstance();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
