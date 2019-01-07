package com.example.shivam.places;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    static ArrayList<String> places = new ArrayList<String>();
    static ArrayList<LatLng> locations = new ArrayList<LatLng>();
    static ArrayAdapter adapter;
    ArrayList<String> lats = new ArrayList<String>();
    ArrayList<String> lons = new ArrayList<String>();
    SharedPreferences sharedPreferences;

    public void displayPlaces() {
        places.clear();
        locations.clear();
        lats.clear();
        lons.clear();

        sharedPreferences = this.getSharedPreferences("com.example.shivam.places", Context.MODE_PRIVATE);

        try {
            places = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places", ObjectSerializer.serialize(new ArrayList<String>())));
            lats = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("latitudes", ObjectSerializer.serialize(new ArrayList<String>())));
            lons = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("longitudes", ObjectSerializer.serialize(new ArrayList<String>())));

            if (places.size() > 0 && lats.size() > 0 && lons.size() > 0) {
                if (places.size() == lats.size() && lats.size() == lons.size()) {

                    for (int i=0; i < places.size(); i++) {
                        locations.add(new LatLng(Double.parseDouble(lats.get(i)), Double.parseDouble(lons.get(i))));
                    }
                }
            } else  if (places.size() == 0 && lats.size() == 0){
                places.add("Add a new place...");
                locations.add(new LatLng(0,0));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("places", places.toString());
        Log.i("loc", locations.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);

        displayPlaces();

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, places);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("position", position);

                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                if (position != 0) {

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Delete Item")
                            .setMessage("Are you sure you want to delete this item?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    places.remove(position);
                                    //locations.remove(position);

                                    try {
                                        sharedPreferences.edit().putString("places", ObjectSerializer.serialize(places)).apply();
                                        sharedPreferences.edit().putString("latitudes", ObjectSerializer.serialize(MapsActivity.lats)).apply();
                                        sharedPreferences.edit().putString("longitudes", ObjectSerializer.serialize(MapsActivity.lons)).apply();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
                return true;
            }
        });
    }
}
