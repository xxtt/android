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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xml.Place;

public class NewsActivity extends ListActivity {

    CodeLearnAdapter chapterListAdapter;
    ArrayList<Place> placeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_layout);
        Intent mIntent = getIntent();
        placeList = mIntent.getParcelableArrayListExtra(MapsActivity.NEWS_FEED);
        chapterListAdapter = new CodeLearnAdapter();
        setListAdapter(chapterListAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Place place = chapterListAdapter.getPlace(position);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(MapsActivity.PLACE, place);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }

    public class CodeLearnAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return placeList.size();
        }

        @Override
        public Place getItem(int arg0) {
            return placeList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {

            if (arg1 == null) {
                LayoutInflater inflater = (LayoutInflater) NewsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                arg1 = inflater.inflate(R.layout.news_list_item, arg2, false);
            }

            TextView title = (TextView) arg1.findViewById(R.id.news_place_title);
            TextView text = (TextView) arg1.findViewById(R.id.news_place_text);
            ImageView image = (ImageView) arg1.findViewById(R.id.news_category_image);

            Place place = placeList.get(arg0);

            title.setText(place.getTitle());
            text.setText(place.getNews());
            image.setImageResource(place.getResourceId());

            return arg1;
        }

        public Place getPlace(int position) {
            return placeList.get(position);
        }

    }
}