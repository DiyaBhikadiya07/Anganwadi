package com.example.anganwadi.User;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import com.example.anganwadi.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class NearbyCentersActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // Store location details for each marker
    private class AnganwadiCenter {
        String name;
        String address;
        double latitude;
        double longitude;
        Marker marker;

        AnganwadiCenter(String name, String address, double latitude, double longitude) {
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    private AnganwadiCenter[] centers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_centers);

        // Initialize all Anganwadi centers with EXACT coordinates from URLs
        initializeCenters();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initializeCenters() {
        centers = new AnganwadiCenter[] {
                new AnganwadiCenter(
                        "AWC Singanpor",
                        "6RC6+HCC, Industrial Area, Singanpor, Surat, Gujarat 395004",
                        21.2214304, 72.8110059
                ),
                new AnganwadiCenter(
                        "AWC nandghar - Katargam",
                        "S. quarter, E.W, Katargam North Zone Office Rd, opposite Katargam, Hari Darshan Society, Katargam, Surat, Gujarat 395004",
                        21.2357211, 72.8376083
                ),
                new AnganwadiCenter(
                        "AWC Limbayat",
                        "231, Aaspas Nagar, Mora Bhagal, Limbayat, Surat, Gujarat 395012",
                        21.1687846, 72.864361
                ),
                new AnganwadiCenter(
                        "AWC Amroli (V.B.D.C. Office)",
                        "6VV4+Q6V, Circle, Amroli, Surat, Gujarat 394107",
                        21.2444989, 72.8555442
                ),
                new AnganwadiCenter(
                        "AWC Godadra (SMC)",
                        "Shree Jee Nagar 1 Godadra Neher, Gujarat 395012",
                        21.1744904, 72.8658368
                ),
                new AnganwadiCenter(
                        "ICDS Office - Sahara Darwaja",
                        "Shop No-16, S.M.C Shopping Centre, near Smimer Medical College, Sahara Darwaja, Surat, Gujarat 395003",
                        21.2030095, 72.8386417
                ),
                new AnganwadiCenter(
                        "AWC Katargam",
                        "Katargam, Surat, Gujarat",
                        21.1861221, 72.7889698
                )
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();

        // Add all markers to map with clear names using EXACT coordinates
        for (AnganwadiCenter center : centers) {
            LatLng location = new LatLng(center.latitude, center.longitude);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(center.name)
                    .snippet(center.address));
            center.marker = marker;
        }

        // Focus camera to show all Surat locations
        LatLng suratCenter = new LatLng(21.2000, 72.8200);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(suratCenter, 11));

        // Marker click listener - show details dialog with directions option
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Find the center details from marker title
                AnganwadiCenter selectedCenter = null;
                for (AnganwadiCenter center : centers) {
                    if (center.name.equals(marker.getTitle())) {
                        selectedCenter = center;
                        break;
                    }
                }

                if (selectedCenter != null) {
                    showLocationDetailsDialog(selectedCenter);
                }
                return true;
            }
        });
    }

    private void showLocationDetailsDialog(AnganwadiCenter center) {
        // Create custom dialog showing location details
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📍 " + center.name);
        builder.setMessage(
                center.name + "\n\n" +
                         center.address + "\n\n" );

        builder.setPositiveButton("Get Directions", (dialog, which) -> {
            openGoogleMapsDirections(center.latitude, center.longitude, center.name);
        });


        builder.setNeutralButton("View on Map", (dialog, which) -> {
            // Close dialog and zoom to the marker on map
                dialog.dismiss();
            });
        builder.setNegativeButton("Cancel", (dialog, which) -> {

            dialog.dismiss();
            if (center.marker != null) {
                // Animate camera to focus on the selected marker with zoom level 16
                LatLng markerPosition = center.marker.getPosition();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 16));
                // Show marker info window again
                center.marker.showInfoWindow();
                Toast.makeText(NearbyCentersActivity.this, center.name, Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openGoogleMapsDirections(double latitude, double longitude, String locationName) {
        try {
            // Open Google Maps with directions from user's current location to destination
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude + "&mode=d");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Fallback: Open in browser
                Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + latitude + "," + longitude + "&travelmode=driving");
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
                startActivity(webIntent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open maps. Please install Google Maps.", Toast.LENGTH_LONG).show();
        }
    }
}