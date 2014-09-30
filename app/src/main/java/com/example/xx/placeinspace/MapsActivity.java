package com.example.xx.placeinspace;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xml.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapsActivity extends ActionBarActivity {

    private GoogleMap mMap;
    private ArrayList<Marker> markerList = new ArrayList<Marker>();
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Pair<Marker, String> currentMarker = null;
    public final static String YOUTUBE_ID = "YOUTUBE_ID";
    HashMap<Integer, String> items = new HashMap<Integer, String>();
    static Map<String, List<Place>> map = new HashMap<String, List<Place>>();
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        new InitMarkers().execute();
        initImageLoader();
    }

    private void initMarkerMap() throws JSONException {

        JSONObject json = new JSONObject(http());
        int success = json.getInt("success");

        JSONArray markers = null;

        if (success == 1) {

            markers = json.getJSONArray("markers");

            for (int i = 0; i < markers.length(); i++) {
                JSONObject marker = markers.getJSONObject(i);

                String title = marker.getString("title");
                String text = marker.getString("description");
                double x = marker.getDouble("x");
                double y = marker.getDouble("y");
                String yt = marker.getString("yt");
                String category = marker.getString("category");

                Place place = new Place(title, text, x, y, yt, category);

                if (map.containsKey(category)) {
                    map.get(category).add(place);
                } else {
                    ArrayList<Place> list = new ArrayList<Place>();
                    list.add(place);
                    map.put(category, list);
                }
            }
        } else {
            //        Toast.makeText(getBaseContext(), "json file success tag error", Toast.LENGTH_SHORT).show();
        }

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

    private void initActionBarDropDown() throws NullPointerException {

        List<String> categories = new ArrayList<String>();
        String all = "все категории";
        categories.add(all);
        items.put(1, all);

        for (String category : map.keySet()) {
            categories.add(category);
            items.put(items.size() + 1, category);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        ActionBar.OnNavigationListener navigationListener = new ActionBar.OnNavigationListener() {

            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {

                if (itemPosition == 0) {
                    for (List<Place> places : map.values()) {
                        for (Place place : places) {
                            place.getMarker().setVisible(true);
                        }
                    }
                } else {
                    String item = items.get(itemPosition + 1);
                    for (String category : map.keySet()) {
                        boolean b = category.equals(item);
                        for (Place place : map.get(category)) {
                            place.getMarker().setVisible(b);
                        }
                    }
                }

                return true;
            }
        };
        getActionBar().setListNavigationCallbacks(adapter, navigationListener);
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
                    Marker show = currentMarker.first;
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

                for (String category : map.keySet()) {
                    for (Place place : map.get(category)) {
                        if (marker.getTitle().equals(place.getTitle())) {
                            text = place.getText();
                            ytId = place.getYouTubeId();

                        }
                    }
                }

                currentMarker = new Pair<Marker, String>(marker, ytId);

                description.setText(text);
                String url = String.format("http://img.youtube.com/vi/" + ytId + "/1.jpg");
                ImageView image = (ImageView) v.findViewById(R.id.imageView);

                imageLoader.displayImage(url, image, options,
                        new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingComplete(String imageUri,
                                                          View view, Bitmap loadedImage) {
                                super.onLoadingComplete(imageUri, view,
                                        loadedImage);
                                getInfoWindow(marker);
                            }
                        });
                return v;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(getBaseContext(), YouTubePlayerActivity.class);
                intent.putExtra(YOUTUBE_ID, currentMarker.second);
                startActivity(intent);
            }
        });

    }

    private void setUpMarkers() {

        float color = 180.0f;        // HARD CODE !!! magic numbers

        Log.i("setUpMarkers", "items number " + map.size());

        LatLng latLng = null;

        for (String category : map.keySet()) {
            for (Place place : map.get(category)) {
                MarkerOptions options = new MarkerOptions();
                latLng = new LatLng(place.getX(), place.getY());
                options.title(place.getTitle()).position(latLng);
                options.icon(BitmapDescriptorFactory.defaultMarker(color));
                place.setMarker(mMap.addMarker(options));
                Log.i("setUpMarkers", "marker is null " + (place.getMarker() == null));
            }
            color = color + 30f;         // HARD CODE !!! magic numbers
        }
        if (latLng != null)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
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
            initActionBarDropDown();
        }
    }
}
