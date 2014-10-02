package com.example.xx.placeinspace.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xx.placeinspace.R;

public class NewsArrayAdapter extends BaseAdapter {
    private Context context;
    private final String[] titles;
    private final String[] texts;

    public NewsArrayAdapter(Context context, String[] titles, String[] texts) {
        this.context = context;
        this.titles = titles;
        this.texts = texts;
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public Object getItem(int position) {
        return texts[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.news_list_item, null);
        }

 //       TextView txtTitle = (TextView) convertView.findViewById(R.id.news_title);
 //       TextView txtText = (TextView) convertView.findViewById(R.id.news_text);

 //       txtTitle.setText(titles[position]);
 //       txtText.setText(texts[position]);
        return convertView;
    }
}