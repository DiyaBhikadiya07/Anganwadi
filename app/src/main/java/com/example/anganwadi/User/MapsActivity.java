package com.example.anganwadi.User;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.example.anganwadi.R;
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
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        LatLng awc1 = new LatLng(21.2281588, 72.8579214);
        LatLng awc2 = new LatLng(21.3110013, 72.8476499);
        LatLng awc3 = new LatLng(21.2357064, 72.8375899);
        LatLng awc4 = new LatLng(21.2214056, 72.8109287);
        LatLng awc5 = new LatLng(21.1861143, 72.7889434);
        LatLng awc6 = new LatLng(21.1678914, 72.8621780);
        mMap.addMarker(new MarkerOptions().position(awc1).title("AWC Utran, Surat"));
        mMap.addMarker(new MarkerOptions().position(awc2).title("AWC Kim"));
        mMap.addMarker(new MarkerOptions().position(awc3).title("AWC Katargam"));
        mMap.addMarker(new MarkerOptions().position(awc4).title("AWC Singanpor"));
        mMap.addMarker(new MarkerOptions().position(awc5).title("AWC Adajan"));
        mMap.addMarker(new MarkerOptions().position(awc6).title("AWC Limbayat"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(awc2));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(awc1));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(awc3));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(awc4));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(awc5));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(awc6));
    }
}