package com.example.shivam.places;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;
    static ArrayList<String> lats = new ArrayList<String>();
    static ArrayList<String> lons = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void setMap(Location location, String description) {

        LatLng userloc = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(userloc).title(description));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userloc, 15));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();

        if (intent.getIntExtra("position", 0) == 0) {
            // Get user's current position
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    setMap(location, "Your location");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
                //Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //setMap(lastKnown,"Your location");
            }
            else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else {
            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(MainActivity.locations.get(intent.getIntExtra("position", 0)).latitude);
            location.setLongitude(MainActivity.locations.get(intent.getIntExtra("position", 0)).longitude);

            setMap(location, MainActivity.places.get(intent.getIntExtra("position", 0)));
        }

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        String address = "";
        sharedPreferences = this.getSharedPreferences("com.example.shivam.places", Context.MODE_PRIVATE);


        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addresses.size() > 0 && addresses != null) {
                if (addresses.get(0).getThoroughfare() != null) {
                    address += addresses.get(0).getThoroughfare();
                    if (addresses.get(0).getSubThoroughfare() != null)
                        address = addresses.get(0).getSubThoroughfare()+ " " +address;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (address == "") {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm dd-MM-yyyy");
            address = simpleDateFormat.format(new Date());
        }

        mMap.addMarker(new MarkerOptions().position(latLng).title(address).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        MainActivity.places.add(address);
        MainActivity.locations.add(latLng);

        MainActivity.adapter.notifyDataSetChanged();
        Toast.makeText(this, "Location saved!", Toast.LENGTH_LONG).show();

        try {
            sharedPreferences.edit().putString("places", ObjectSerializer.serialize(MainActivity.places)).apply();

            Log.i("places", MainActivity.places.toString());
            //Log.i("loc", locations.toString());

            for (LatLng cord : MainActivity.locations) {
                lats.add(Double.toString(cord.latitude));
                lons.add(Double.toString(cord.longitude));
            }


            sharedPreferences.edit().putString("latitudes", ObjectSerializer.serialize(lats)).apply();
            sharedPreferences.edit().putString("longitudes", ObjectSerializer.serialize(lons)).apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}