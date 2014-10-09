package com.example.xx.placeinspace;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.xml.Place;
import com.example.xx.placeinspace.adapter.NavDrawerListAdapter;
import com.example.xx.placeinspace.model.NavDrawerItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.FIFOLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MapsActivity extends ActionBarActivity {

    private GoogleMap mMap;
    private ArrayList<Marker> markerList = new ArrayList<Marker>();
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Place currentMarker = null;
    public final static String YOUTUBE_ID = "YOUTUBE_ID";
    public final static String NEWS_FEED = "NEWS_FEED";

    static Map<Integer, List<Place>> mCategoryList = new HashMap<Integer, List<Place>>();
    private ProgressDialog pDialog;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavDrawerListAdapter adapter;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    private CharSequence mTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        initDrawerLayout();

        new InitMarkers().execute();

        initImageLoader();
        //      if (savedInstanceState == null) {
        // on first time display view for first nav item
        //      }
    }

    private void initDrawerLayout() {
        mTitle = getTitle();

        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.drawer_open, // nav drawer open - description for accessibility
                R.string.drawer_close // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
//                getActionBar().setTitle(mDrawerTitle);

                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    private void initDrawerList() {
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
        navDrawerItems = new ArrayList<NavDrawerItem>();

        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));

        for (Integer category : mCategoryList.keySet()) {
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[category], navMenuIcons.getResourceId(category, -1)));
        }
//      navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1), true, "50+"));

        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
        displayView(0);
    }

    /**
     * Slide menu item click listener
     */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
     */
    private void displayView(int position) {
        if (position == 0) {
            for (List<Place> places : mCategoryList.values()) {
                for (Place place : places) {
                    place.getMarker().setVisible(true);
                }
            }
        } else {
            ArrayList<Integer> list = new ArrayList<Integer>(mCategoryList.keySet());
            for (Integer category : list) {
                boolean b = category.equals(list.get(position - 1));
                for (Place place : mCategoryList.get(category)) {
                    place.getMarker().setVisible(b);
                }
            }
        }

        mDrawerList.setItemChecked(position, true);
        mDrawerList.setSelection(position);
        setTitle(navMenuTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);

    }

    ////////////// Drawer code starts /////////////////////

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_news:
                ArrayList<Place> list = new ArrayList<Place>();
                for (List<Place> places : mCategoryList.values()) {
                    for (Place place : places) {
                        if (!place.getNews().isEmpty()) list.add(place);
                    }
                }

                Intent intent = new Intent(getBaseContext(), NewsActivity.class);
                intent.putParcelableArrayListExtra(NEWS_FEED, list);
                startActivity(intent);
                return true;
            case R.id.action_share:
                share();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mDrawerList != null) {
            // if nav drawer is opened, hide the action items
            boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
            menu.findItem(R.id.action_search).setVisible(!drawerOpen);
            menu.findItem(R.id.action_share).setVisible(!drawerOpen);
            menu.findItem(R.id.action_news).setVisible(!drawerOpen);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null) mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        if (mDrawerToggle != null) mDrawerToggle.onConfigurationChanged(newConfig);
    }

    ///////////////Drawer code ends //////////////////


    private void share() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "PlaceInSpace is awesome !!");
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
    }

    private void initMarkerMap() throws JSONException {

        JSONObject json = new JSONObject(http());
        int success = json.getInt("success");

        JSONArray markers = null;

        Log.i("initMarkerMap", "success is  " + success);

        if (success == 1) {

            markers = json.getJSONArray("markers");

            for (int i = 0; i < markers.length(); i++) {
                JSONObject marker = markers.getJSONObject(i);

                String title = marker.getString("title");
                String text = marker.getString("description");
                double x = marker.getDouble("x");
                double y = marker.getDouble("y");
                String yt = marker.getString("yt");
                int category = marker.getInt("category");
                String address = marker.getString("address");
                String phone = marker.getString("phone");
                String link = marker.getString("link");
                String news = marker.getString("news");
                boolean smoking = marker.getInt("smoking") != 0;
                boolean baby = marker.getInt("baby") != 0;
                boolean parking = marker.getInt("parking") != 0;
                boolean music = marker.getInt("music") != 0;

                Log.i("initMarkerMap", "title is  " + title);

                Place place = new Place(title, text, x, y, yt, category, address, phone, link, news, smoking, baby, parking, music);

                place.setResourceId(navMenuIcons.getResourceId(category, 0));

                if (mCategoryList.containsKey(category)) {
                    mCategoryList.get(category).add(place);
                } else {
                    ArrayList<Place> list = new ArrayList<Place>();
                    list.add(place);
                    mCategoryList.put(category, list);
                }

            }

        }
//       navMenuIcons.recycle();
    }


    public String http() {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://place-in-space.esy.es/get.php");
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                //        Toast.makeText(getBaseContext(), "failed to download file", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            //   Toast.makeText(getBaseContext(), "json read exception", Toast.LENGTH_SHORT).show();
        }
        return builder.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }


    private void setUpMapIfNeeded() {
        if (mMap == null) {

            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
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
                TextView description = (TextView) v.findViewById(R.id.info_window_description);
                String text = null;
                String ytId = null;

                for (int category : mCategoryList.keySet()) {
                    for (Place place : mCategoryList.get(category)) {
                        if (marker.getTitle().equals(place.getTitle())) {
                            text = place.getText();
                            ytId = place.getYouTubeId();
                            currentMarker = place;

                        }
                    }
                }


                description.setText(text);
                String url1 = String.format("http://img.youtube.com/vi/" + ytId + "/1.jpg");
                String url2 = String.format("http://img.youtube.com/vi/" + ytId + "/2.jpg");
                String url3 = String.format("http://img.youtube.com/vi/" + ytId + "/3.jpg");

                ImageView image1 = (ImageView) v.findViewById(R.id.image1);
                ImageView image2 = (ImageView) v.findViewById(R.id.image2);
                ImageView image3 = (ImageView) v.findViewById(R.id.image3);

                imageLoader.displayImage(url1, image1, options);
                imageLoader.displayImage(url2, image2, options);
                imageLoader.displayImage(url3, image3, options);


   /*             imageLoader.displayImage(url1, image1, options, new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                super.onLoadingComplete(imageUri, view, loadedImage);
                                getInfoWindow(marker);
                            }
                        });
                */
                return v;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(getBaseContext(), YouTubePlayerActivity.class);
                intent.putExtra(YOUTUBE_ID, currentMarker);
                startActivity(intent);
            }
        });

    }

    private void setUpMarkers() {
        Log.i("setUpMarkers", "items number " + mCategoryList.size());
        LatLng latLng = null;

        for (int category : mCategoryList.keySet()) {
            for (Place place : mCategoryList.get(category)) {
                MarkerOptions options = new MarkerOptions();
                latLng = new LatLng(place.getX(), place.getY());
                options.title(place.getTitle()).position(latLng);
                options.icon(BitmapDescriptorFactory.fromResource(place.getResourceId()));
                place.setMarker(mMap.addMarker(options));
                Log.i("setUpMarkers", "marker is null " + (place.getMarker() == null));
            }
        }
        if (latLng != null)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

        directions();
    }

    private void directions() {
        LatLng start = new LatLng(13.744246499553903, 100.53428772836924);
        LatLng end = new LatLng(13.751279688694071, 100.54316081106663);

        //    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(start, 16));
        GoogleDirection gd = new GoogleDirection(this);

        gd.setLogging(true);
        gd.request(start, end, GoogleDirection.MODE_WALKING);


        gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
            public void onResponse(String status, Document doc, GoogleDirection gd) {
                LatLng start = new LatLng(13.744246499553903, 100.53428772836924);
                LatLng end = new LatLng(13.751279688694071, 100.54316081106663);
                Document mDoc = doc;
                mMap.addPolyline(gd.getPolyline(doc, 3, Color.RED));

                mMap.addMarker(new MarkerOptions().position(end)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.rubric_accessories)));
            }
        });
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
//                .showStubImage(R.drawable.ic_launcher)        //    Display Stub Image
//                .showImageForEmptyUri(R.drawable.ic_launcher)    //    If Empty image found
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

    class InitMarkers extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Loading. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            try {
                initMarkerMap();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            setUpMarkers();
            initDrawerList();

        }
    }
}
