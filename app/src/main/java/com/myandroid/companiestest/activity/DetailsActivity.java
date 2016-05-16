package com.myandroid.companiestest.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.myandroid.companiestest.DBHelper;
import com.myandroid.companiestest.R;
import com.myandroid.companiestest.entity.Company;
import com.myandroid.companiestest.entity.CompanyAddress;

import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {
    private static final String LOG_TAG = "DetailsActivity";
    private static final String STATE_COMPANY = "companyId";

    private TextView textViewName;
    private TextView textViewDescription;
    private TextView textViewAddress;

    private Company company;
    private GoogleMap googleMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        int idCompany;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                idCompany = 0;
            } else {
                idCompany = extras.getInt("idCompany");
                Log.v(LOG_TAG, "idCompany = " + idCompany);
            }
        } else {
            idCompany = savedInstanceState.getInt(STATE_COMPANY);
        }

        initViews();
        getCompanyById(idCompany);
        setViews(company);
        createMapView(company.getAddresses().get(0));
        for (CompanyAddress address : company.getAddresses()) {
            addMarker(address);
        }
    }

    private void getCompanyById(int idCompany) {
        DBHelper dbHelper = new DBHelper(this);
        company = dbHelper.getCompanyById(idCompany);
    }

    private void initViews() {
        textViewName = (TextView) findViewById(R.id.textView);
        textViewDescription = (TextView) findViewById(R.id.textView2);
        textViewAddress = (TextView) findViewById(R.id.textView3);
    }

    private void setViews(Company company) {
        textViewName.setText(company.getName());
        textViewDescription.setText(company.getDescription());
        String listAddresses = "";
        for (CompanyAddress address : company.getAddresses()) {
            listAddresses += address.getAddress() + "\n";
        }
        textViewAddress.setText(listAddresses);
    }

    private String[] getListAddresses() {
        String[] listAddresses = new String[company.getAddresses().size()];
        for (int i = 0; i < company.getAddresses().size(); i++) {
            listAddresses[i] = company.getAddresses().get(i).getAddress();
        }
        return listAddresses;
    }

    private void createMapView(CompanyAddress address) {
        try {
            if (null == googleMap) {
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(address.getLatitude(), address.getLongitude()), 11));
                if (null == googleMap) {
                    Toast.makeText(getApplicationContext(),
                            "Error creating map", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception) {
            Log.e(LOG_TAG, exception.toString());
        }
    }

    private void addMarker(CompanyAddress address) {
        if (null != googleMap) {
            googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(address.getLatitude(), address.getLongitude()))
                            .title(address.getAddress())
                            .draggable(true)
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_show_on_map) {
            selectAddressForGoogleMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectAddressForGoogleMap() {
        final String[] addresses = getListAddresses();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.choose_address))
                .setItems(addresses, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showOnGoogleMap(addresses[which]);
                    }
                })
                .create()
                .show();
    }

    private void showOnGoogleMap(String location) {
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG, "Couldn't call " + location + ", no receiving apps installed!");
        }
    }
}
