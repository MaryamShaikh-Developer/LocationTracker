package com.example.moitt_task;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.moitt_task.databinding.ActivityMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MapsActivity extends FragmentActivity {

    private DatabaseReference mDatabaseRef;
    private ActivityMapsBinding binding;
    FusedLocationProviderClient client;
    SupportMapFragment smf;
    private int GPS_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


            smf = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            client = LocationServices.getFusedLocationProviderClient(this);

            checkPermission();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Location Data");

    }


    private void checkPermission() {
        Dexter.withContext(getApplicationContext()).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                isGPSenabled();
                getmyLocation();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.cancelPermissionRequest();
            }
        }).check();

    }



    private void getmyLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess( Location location) {
                smf.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        if (location != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            Log.d("TAG", "onMapReady: " + latLng);
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                            googleMap.addMarker(markerOptions);
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                            mDatabaseRef.push().setValue(latLng);
                            Log.d("TAG", "addDataToFirebase: " + latLng);
                        } else {
                            Toast.makeText(MapsActivity.this, "" +
                                    "please turn on the device location", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
    }

    private Boolean isGPSenabled(){
        LocationManager locationManager =(LocationManager) getSystemService(LOCATION_SERVICE);
        boolean providerEnable =locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (providerEnable){
            return true;
        }
        else{
            AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("GPS Permission")
                    .setMessage("GPS is required. Please turn on GPS")
                    .setPositiveButton("yes",((dialogInterface, i) ->{
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent,GPS_REQUEST_CODE);
                    } )).setCancelable(false).show();
        }
        return false;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==GPS_REQUEST_CODE){
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean providerEnable =locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (providerEnable){
                Toast.makeText(this, "GPS has been enabled", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "GPS is not enabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

}