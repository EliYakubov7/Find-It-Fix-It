package com.example.finditfixit;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class RetrieveMapActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String MY_PREFS_NAME = "MyPrefsFile";

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve_map);

        FirebaseApp.initializeApp(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);

        FirebaseDatabase.getInstance().getReference().child("markers").addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    String key = ds.getKey();

                    String nameFirebase = ds.child("name").getValue(String.class);
                    Double longitudeFirebase = ds.child("longitude").getValue(Double.class);
                    Double latitudeFirebase = ds.child("latitude").getValue(Double.class);
                    String faultFirebase = ds.child("fault").getValue(String.class);

                    SharedPreferences.Editor editor3 = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor3.putString("name", nameFirebase);
                    editor3.apply();

                    SharedPreferences.Editor editor4 = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor4.putString("fault", faultFirebase);
                    editor4.apply();


                    if (latitudeFirebase != null && longitudeFirebase != null) {
                        LatLng location = new LatLng(latitudeFirebase, longitudeFirebase);

                        SharedPreferences prefs = getApplicationContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                        String name = prefs.getString("name", "No name");

                        SharedPreferences prefs2 = getApplicationContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                        String fault = prefs2.getString("fault", "No fault");


                        Marker marker = mMap.addMarker(new MarkerOptions().position(location).title((String) getText(R.string.details)).
                                snippet((String) getText(R.string.name) + " " + name + " " + "," + " " + (String) getText(R.string.fault) + " " + fault));

                        marker.showInfoWindow();

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 5F));
                    }

                    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                        @Override
                        public boolean onMarkerClick(Marker marker) {

                            AlertDialog alertDialog = new AlertDialog.Builder(RetrieveMapActivity.this,R.style.AlertDialogCustom)

                                    .setIcon(R.drawable.ic_baseline_warning_24)

                                    .setTitle(R.string.title_alert_dialog)

                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            marker.remove();
                                            marker.setVisible(false);
                                            assert key != null;
                                            FirebaseDatabase.getInstance().getReference().child("markers").child(key).setValue(null);
                                            Toast.makeText(getApplicationContext(),R.string.marker_and_data_deleted, Toast.LENGTH_LONG).show();

                                        }
                                    })

                                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    })
                                    .show();

                            return false;
                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
