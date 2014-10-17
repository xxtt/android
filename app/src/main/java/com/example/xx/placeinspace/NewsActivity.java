package com.example.xx.placeinspace;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xml.Place;

public class NewsActivity extends ListActivity {

    ArrayList<Place> placeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_layout);
        Intent mIntent = getIntent();
        placeList = mIntent.getParcelableArrayListExtra(MapsActivity.NEWS_FEED);

        setListAdapter(new CustomAdapter(this, R.layout.news_list_item, placeList));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Place place = placeList.get(position);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(MapsActivity.PLACE, place);
        setResult(RESULT_OK, resultIntent);
        onBackPressed();
    }


    private class CustomAdapter extends ArrayAdapter<Place> {
        Context context;
        int resource;
        List<Place> list;


        public CustomAdapter(Context context, int resource, List<Place> list) {
            super(context, resource, list);
            this.context = context;
            this.resource = resource;
            this.list = list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                convertView = inflater.inflate(resource, parent, false);
            }
            Place place = list.get(position);
            TextView title = (TextView) convertView.findViewById(R.id.news_place_title);
            TextView text = (TextView) convertView.findViewById(R.id.news_place_text);
            ImageView image = (ImageView) convertView.findViewById(R.id.news_category_image);
            title.setText(place.getTitle());
            text.setText(place.getNews());
            image.setImageResource(place.getIconResourceId());
            return convertView;
        }
    }
}