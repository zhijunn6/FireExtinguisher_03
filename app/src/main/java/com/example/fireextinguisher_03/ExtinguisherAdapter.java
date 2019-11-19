package com.example.fireextinguisher_03;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.GetExtinguisherbyLocationQuery;

import java.util.List;

public class ExtinguisherAdapter extends BaseAdapter {
    private final Context mContext;
    private LayoutInflater mInflater;
    private List<GetExtinguisherbyLocationQuery.Item> extinguishers;

    // ata is passed into the constructor
    public ExtinguisherAdapter(Context context, List<GetExtinguisherbyLocationQuery.Item> extinguishers) {
        this.mContext = context;
        this.extinguishers = extinguishers;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setItems(List<GetExtinguisherbyLocationQuery.Item> extinguishers) { this.extinguishers = extinguishers; }

    @Override
    public int getCount() { return extinguishers.size(); }

    @Override
    public Object getItem(int i) { return extinguishers.get(i); }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.extinguisher_list_view, parent, false);
            holder = new ViewHolder();
            holder.extinguisherNumberTextView = (TextView) convertView.findViewById(R.id.extinguisher_number);
            holder.extinguisherSublocationTextView = (TextView) convertView.findViewById(R.id.extinguisher_subLocation);
            holder.extinguisherExpiryDateTextView = (TextView) convertView.findViewById(R.id.extinguisher_expiryDate);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final GetExtinguisherbyLocationQuery.Item post = (GetExtinguisherbyLocationQuery.Item) getItem(i);

        holder.extinguisherNumberTextView.setText(post.fragments().extinguisher().extinguisherNumber());
        holder.extinguisherSublocationTextView.setText(post.fragments().extinguisher().subLocation());
        holder.extinguisherExpiryDateTextView.setText(post.fragments().extinguisher().expiryDate());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditExtinguisherActivity.startActivity(v.getContext(), post.fragments().extinguisher());
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        public TextView extinguisherNumberTextView;
        public TextView extinguisherExpiryDateTextView;
        public TextView extinguisherSublocationTextView;
    }
}
