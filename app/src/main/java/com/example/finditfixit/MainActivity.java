package com.example.finditfixit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;

import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements LocationListener {

    private final int LOCATION_PERMISSION_REQUEST = 1;
    private LocationManager location;
    private Geocoder geocoder;
    private double latitude = 0.0, longitude = 0.0;

    private ImageButton getLocationBtn;
    private ProgressBar locationProgressBar;
    private Handler handler = new Handler();

    EditText fullNameTeT, faultTypeTet, locationTet;

    TextInputLayout fullNameTil, faultTypeTil, locationTil;

    Button addDetailsButton;
    Button mapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fullNameTeT = findViewById(R.id.full_name_et);
        faultTypeTet = findViewById(R.id.fault_et);

        locationTet = findViewById(R.id.location_et);
        locationTet.setFocusable(false);

        fullNameTil = findViewById(R.id.fullName_til);
        faultTypeTil = findViewById(R.id.fault_til);
        locationTil = findViewById(R.id.location_til);

        getLocationBtn = findViewById(R.id.location_btn);
        locationProgressBar = findViewById(R.id.location_progress_bar);

        addDetailsButton = findViewById(R.id.add_button);

        addDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(longitude == 0.0 && latitude == 0.0) {
                    getLatLongFromAddress();
                }


                if(TextUtils.isEmpty(fullNameTeT.getText()) || TextUtils.isEmpty(faultTypeTet.getText()) || TextUtils.isEmpty(locationTet.getText())) {

                    fullNameTil.setError(getText(R.string.set_error_full_name));
                    faultTypeTil.setError(getText(R.string.set_error_fault_type));
                    locationTil.setError(getText(R.string.set_error_location));

                }

                else
                {

                    User user = new User(faultTypeTet.getText().toString(),latitude,longitude,fullNameTeT.getText().toString());

                    FirebaseDatabase.getInstance().getReference().child("markers").push().setValue(user, new DatabaseReference.CompletionListener() {
                        public void onComplete(DatabaseError error, DatabaseReference ref) {
                            Toast.makeText(MainActivity.this,R.string.add_details_successfully,Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });


        mapButton = findViewById(R.id.map_button);

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(),RetrieveMapActivity.class));
            }
        });


        geocoder = new Geocoder(this);

        if (Build.VERSION.SDK_INT >= 23) {
            int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            }
        }


        location = (LocationManager) getSystemService(LOCATION_SERVICE);

        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                    if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
                    } else {
                        locationProgressBar.setVisibility(View.VISIBLE);
                        location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, MainActivity.this);
                    }
                } else {
                    locationProgressBar.setVisibility(View.VISIBLE);
                    location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, MainActivity.this);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST)
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getResources().getString(R.string.label_location_dialog_title))
                        .setMessage(getResources().getString(R.string.label_location_dialog_body))
                        .setPositiveButton(getResources().getString(R.string.label_settings), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                            }
                        }).setNegativeButton(getResources().getString(R.string.label_quit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
            }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        new Thread() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                super.run();

                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            TextInputEditText location = findViewById(R.id.location_et);
                            String address = addresses.get(0).getAddressLine(0);

                            location.setText(address);
                            locationProgressBar.setVisibility(View.GONE);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
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

    /*Set longitude and latitude from a given address*/
    private void getLatLongFromAddress() {
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        try {

            TextInputEditText etLocation = findViewById(R.id.location_et);
            address = coder.getFromLocationName(Objects.requireNonNull(etLocation.getText()).toString(), 5);

            //check for null
            if (address != null) {
                //Lets take first possibility from the all possibilities.
                Address location = address.get(0);
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

