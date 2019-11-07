package com.example.fireextinguisher_03;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.GetExtinguisherbyLocationQuery;
import com.amazonaws.amplify.generated.graphql.ListLocationsQuery;

import java.util.List;

public class MyAdapter extends BaseAdapter {

    private final Context mContext;
    private LayoutInflater mInflater;
    private List<ListLocationsQuery.Item> locations;


    // data is passed into the constructor
    public MyAdapter(Context context, List<ListLocationsQuery.Item> locations) {
        this.mContext = context;
        this.locations = locations;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setItems(List<ListLocationsQuery.Item> locations) { this.locations = locations; }

    @Override
    public int getCount() { return locations.size(); }

    @Override
    public Object getItem(int i) { return locations.get(i); }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.location_list_view, parent, false);
            holder = new ViewHolder();
            holder.locationNameTextView = (TextView) convertView.findViewById(R.id.locationName);
            holder.locationAddressTextView = (TextView) convertView.findViewById(R.id.locationAddress);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final ListLocationsQuery.Item post = (ListLocationsQuery.Item) getItem(i);

        holder.locationNameTextView.setText(post.fragments().location().name());
        holder.locationAddressTextView.setText(post.fragments().location().address());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               AddExtinguisherActivity.startActivity(v.getContext(), post.fragments().location());
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        public TextView locationNameTextView;
        public TextView locationAddressTextView;
    }
}
