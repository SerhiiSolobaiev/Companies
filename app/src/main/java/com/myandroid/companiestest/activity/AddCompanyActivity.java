package com.myandroid.companiestest.activity;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.barcode.Barcode;
import com.myandroid.companiestest.DBHelper;
import com.myandroid.companiestest.R;
import com.myandroid.companiestest.entity.Company;
import com.myandroid.companiestest.entity.CompanyAddress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddCompanyActivity extends AppCompatActivity{

    private static final String LOG_TAG = "AddCompanyActivity";
    private EditText editTextName;
    private EditText editTextDescription;
    private EditText editTextAddress;
    private ImageButton buttonAddAddress;

    private GoogleMap googleMap;
    private Geocoder geocoder;

    private List<CompanyAddress> companyAddresses;
    private long idCompany;
    private int addressCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_company);
        geocoder = new Geocoder(this, Locale.getDefault());
        companyAddresses = new ArrayList<>();
        initViews();
        createMapView();
        buttonAddAddressListener();
        setOnMarkerDragListener();
    }

    private void initViews() {
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextDescription = (EditText) findViewById(R.id.editTextDescription);
        editTextAddress = (EditText) findViewById(R.id.editTextAddress);
        buttonAddAddress = (ImageButton) findViewById(R.id.imageButton);
    }

    private void buttonAddAddressListener() {
        buttonAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMarker(getLatLngFromAddress(editTextAddress.getText().toString()));
                editTextAddress.setText("");
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.address_added), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addCompany() {
        if (editTextName.getText().toString().equals("")){
            Toast.makeText(this, getResources().getString(R.string.enter_name), Toast.LENGTH_SHORT).show();
            return;
        }
        DBHelper dbHelper = new DBHelper(this);
        idCompany = dbHelper.insertCompany(editTextName.getText().toString(),
                editTextDescription.getText().toString());
        for (CompanyAddress address: companyAddresses){
            dbHelper.insertCompanyAddress(idCompany,
                    address.getAddress(), address.getLatitude(), address.getLongitude());
        }
        if (idCompany != -1) {
            String message = getResources().getString(R.string.company_inserted_before)
                    + editTextName.getText().toString()
                    + getResources().getString(R.string.company_inserted_after);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, "Error while adding the company", Toast.LENGTH_SHORT).show();
    }

    private void createMapView() {
        try {
            if (null == googleMap) {
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(50.4432054, 30.519168), 11));
                if (null == googleMap) {
                    Toast.makeText(getApplicationContext(),
                            "Error creating map", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception) {
            Log.e(LOG_TAG, exception.toString());
        }
    }

    private void addMarker(LatLng position) {
        if (null != googleMap) {
            googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(position.latitude, position.longitude))
                            .title(String.valueOf(addressCount))
                            .draggable(true)
            );
            companyAddresses.add(new CompanyAddress(addressCount, getAddressFromLatLng(position),
                    idCompany, position.latitude, position.longitude));
            addressCount++;
        }
    }

    private void setOnMarkerDragListener(){
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng position = marker.getPosition();
                Log.v(LOG_TAG,"marker.getTitle() = " + marker.getTitle());
                int title = Integer.valueOf(marker.getTitle());
                editTextAddress.setText(getAddressFromLatLng(position));
                companyAddresses.set(title,new CompanyAddress(getAddressFromLatLng(position),
                        idCompany, position.latitude, position.longitude));
            }
        });
    }

    //TODO:
    //Make this in background thread!!!
    private String getAddressFromLatLng(LatLng latLng) {
        List<Address> addressFromLatLng = null;
        try {
            addressFromLatLng = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addressFromLatLng.get(0).getAddressLine(0);
    }

    //TODO:
    //Make this in background thread!!!
    private LatLng getLatLngFromAddress(String inputAddress) {
        geocoder = new Geocoder(this);
        List<Address> address;
        Address location = null;
        try {
            address = geocoder.getFromLocationName(inputAddress, 5);
            if (address == null) {
                return null;
            }
            location = address.get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_company, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            addCompany();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
