package com.example.rakesh.zomato;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Intent intent = getIntent();
        Double lat = Double.valueOf(intent.getStringExtra("lat"));
        Double lon = Double.valueOf(intent.getStringExtra("lon"));
        String locality = intent.getStringExtra("locality");

        mMap = googleMap;
        LatLng hotels = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(hotels).title(locality));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hotels,14));
    }





}
