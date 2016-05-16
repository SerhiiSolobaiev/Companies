package com.myandroid.companiestest.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.myandroid.companiestest.R;
import com.myandroid.companiestest.entity.Company;

import java.util.ArrayList;
import java.util.List;

public class CompanyAdapter extends ArrayAdapter implements Filterable {
    private Context context;
    private int layoutResourceId;
    private List<Company> data = new ArrayList<>();
    private List<Company> dataOrigin  = new ArrayList<>();

    public CompanyAdapter(Context context, int resource, List<Company> objects) {
        super(context, resource, objects);
        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
        dataOrigin.addAll(objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.nameCompany = (TextView) row.findViewById(R.id.text1);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Company item = data.get(position);
        holder.nameCompany.setText(item.getName());
        return row;
    }

    static class ViewHolder {
        TextView nameCompany;
    }

    public void filter(String charText) {
        charText = charText.toLowerCase();
        data.clear();
        if (charText.length() == 0) {
            data.addAll(dataOrigin);
        } else {
            for (Company company : dataOrigin) {
                if (company.getName().toLowerCase().contains(charText)) {
                    data.add(company);
                }
            }
        }
        notifyDataSetChanged();
    }
}
