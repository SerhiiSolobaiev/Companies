package com.myandroid.companiestest.activity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
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

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.barcode.Barcode;
import com.myandroid.companiestest.DBHelper;
import com.myandroid.companiestest.GeocodeJSONParser;
import com.myandroid.companiestest.R;
import com.myandroid.companiestest.entity.Company;
import com.myandroid.companiestest.entity.CompanyAddress;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AddCompanyActivity extends AppCompatActivity {

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
        geocoder = new Geocoder(getApplicationContext(), new Locale("ru", "RU"));
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
                runBackgroudThread(editTextAddress.getText().toString());
                //addMarker(getLatLngFromAddress(editTextAddress.getText().toString()));
                editTextAddress.setText("");
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.address_added), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addCompany() {
        if (editTextName.getText().toString().equals("")) {
            Toast.makeText(this, getResources().getString(R.string.enter_name), Toast.LENGTH_SHORT).show();
            return;
        }
        DBHelper dbHelper = new DBHelper(this);
        idCompany = dbHelper.insertCompany(editTextName.getText().toString(),
                editTextDescription.getText().toString());
        for (CompanyAddress address : companyAddresses) {
            dbHelper.insertCompanyAddress(idCompany,
                    address.getAddress(), address.getLatitude(), address.getLongitude());
        }
        if (idCompany != -1) {
            startActivity(new Intent(AddCompanyActivity.this, MainActivity.class));
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

    private void setOnMarkerDragListener() {
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
                Log.v(LOG_TAG, "marker.getTitle() = " + marker.getTitle());
                int title = Integer.valueOf(marker.getSnippet());
                String newAddress = getAddressFromLatLng(
                        new LatLng(position.latitude, position.longitude));
                editTextAddress.setText(newAddress);
                companyAddresses.set(title, new CompanyAddress(newAddress,
                        idCompany, position.latitude, position.longitude));
            }
        });
    }

    private String getAddressFromLatLng(LatLng latLng) {
        List<Address> addressFromLatLng = null;
        try {
            addressFromLatLng = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addressFromLatLng.get(0).getAddressLine(0);
    }

    private void runBackgroudThread(String location) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?";

        String address = null;
        try {
            address = "address=" + URLEncoder.encode(location, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String sensor = "sensor=false";
        url = url + address + "&" + sensor;
        DownloadTask downloadTask = new DownloadTask();

        // Start downloading the geocoding places
        downloadTask.execute(url);

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

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.e("Exception downloading", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {
        String data = null;

        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            GeocodeJSONParser parser = new GeocodeJSONParser();

            try {
                jObject = new JSONObject(jsonData[0]);

                /** Getting the parsed data as a an ArrayList */
                places = parser.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {

            HashMap<String, String> hmPlace = list.get(0);
            double lat = Double.parseDouble(hmPlace.get("lat"));
            double lng = Double.parseDouble(hmPlace.get("lng"));
            String name = hmPlace.get("formatted_address");

            googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title(name)
                            .snippet(String.valueOf(addressCount))
                            .draggable(true)
            );
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(lat, lng), 13));
            companyAddresses.add(new CompanyAddress(addressCount, name,
                    idCompany, lat, lng));
            addressCount++;
        }
    }
}
