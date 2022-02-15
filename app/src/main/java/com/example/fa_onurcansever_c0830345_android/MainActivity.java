package com.example.fa_onurcansever_c0830345_android;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.fa_onurcansever_c0830345_android.room.Place;
import com.example.fa_onurcansever_c0830345_android.viewmodel.PlaceViewModel;
import com.example.fa_onurcansever_c0830345_android.volley.NearbyPlace;
import com.example.fa_onurcansever_c0830345_android.volley.VolleySingleton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int UPDATE_INTERVAL = 5000; // 5 seconds
    private static final int FASTEST_INTERVAL = 3000; // 3 seconds
    private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int REQUEST_CODE = 1;
    private static final String TAG = "MainActivity";
    private static final int RADIUS = 1500;
    private static final String API_KEY = "AIzaSyCIL4e3YEsTg3wxm5vCdD9jNXcLDo2ANt8";

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LatLng userLocation;
    private FragmentManager fragmentManager;
    private MapsFragment mapsFragment;
    private FloatingActionButton favoritesButton;
    private PlaceViewModel placeViewModel;
    private RequestQueue requestQueue;
    private EditText nearbyPlaceName;
    private Button searchButton, refreshButton, changeMapButton;
    private List<NearbyPlace> nearbyPlacesList;
    private List<Place> placeList;
    private boolean isTypeChanged = false;
    private ActivityResultLauncher<Intent> resultLauncher;
    private LatLng intentLatLng;
    private Location userCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        nearbyPlaceName = findViewById(R.id.nearbyPlaceText);
        searchButton = findViewById(R.id.searchButton);
        refreshButton = findViewById(R.id.refreshButton);
        changeMapButton = findViewById(R.id.changeLayoutButton);
        favoritesButton = findViewById(R.id.favoritesButton);
        nearbyPlacesList = new ArrayList<>();
        configureFragment();

        placeViewModel = new ViewModelProvider.AndroidViewModelFactory(this.getApplication()).create(PlaceViewModel.class);

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    double lat = data.getDoubleExtra("lat", 0);
                    double lng = data.getDoubleExtra("lng", 0);
                    intentLatLng = new LatLng(lat, lng);
                }
            }
        });

        configureButtonListeners();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isLocationPermissionGranted()) {
            requestLocationPermission();
        } else {
            startUpdateLocation();
        }

        placeViewModel.getAllPlaces().observe(this, places -> {
            System.out.println(places);
            loadMarkersFromDatabase(places);
            placeList = places;
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopUpdateLocation();
    }

    private void configureFragment() {
        fragmentManager = getSupportFragmentManager();
        mapsFragment = new MapsFragment();
        fragmentManager.beginTransaction()
                .add(R.id.container_maps, mapsFragment)
                .commit();
    }

    private void configureButtonListeners() {
        favoritesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            resultLauncher.launch(intent);
        });

        searchButton.setOnClickListener(v -> {
            String placeName = nearbyPlaceName.getText().toString().trim().toLowerCase();

            if (placeName.isEmpty()) {
                return;
            }

            String url = getPlaceURL(userLocation.latitude, userLocation.longitude, placeName);

            findNearbyPlaces(url);

        });

        refreshButton.setOnClickListener(v -> {
            mapsFragment.getMap().clear();
            loadMarkersFromDatabase(placeList);
            nearbyPlaceName.clearFocus();
            nearbyPlaceName.setText("");
            mapsFragment.setupUserLocation(userCurrentLocation, null);
        });

        changeMapButton.setOnClickListener(v -> {
            isTypeChanged = !isTypeChanged;

            if (isTypeChanged) {
                mapsFragment.getMap().setMapType(GoogleMap.MAP_TYPE_HYBRID);
            } else {
                mapsFragment.getMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }

        });
    }

    public void zoomToItemClicked(LatLng latLng) {
        mapsFragment.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    private void loadMarkersFromDatabase(List<Place> placeList) {

        if (placeList != null) {
            for (Place place : placeList) {
                System.out.println("Place name " + place.getName());
                LatLng latLng = new LatLng(place.getLatitude(), place.getLongitude());

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(place.getName())
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                mapsFragment.setMarkers(options);
            }
        }
    }

    private String getPlaceURL(double latitude, double longitude, String placeType) {
        StringBuilder googlePlaceURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceURL.append("location=" + latitude + "," + longitude);
        googlePlaceURL.append("&radius=" + RADIUS);
        googlePlaceURL.append("&type=" + placeType);
        googlePlaceURL.append("&key=" + API_KEY);

        return googlePlaceURL.toString();
    }

    private void findNearbyPlaces(String url) {
        nearbyPlacesList.clear();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {

                    try {
                        JSONArray jsonArray = response.getJSONArray("results");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            JSONObject geometry = jsonObject.getJSONObject("geometry");
                            JSONObject location = geometry.getJSONObject("location");

                            String name = jsonObject.getString("name");
                            double lat = location.getDouble("lat");
                            double lng = location.getDouble("lng");

                            NearbyPlace nearbyPlace = new NearbyPlace(name, lat, lng);
                            nearbyPlacesList.add(nearbyPlace);

                            mapsFragment.setNearbyPlacesOnMap(nearbyPlacesList);

                            System.out.println("Name: " + name + " " + "Latitude: " + lat + " " + "Longitude: " + lng);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                },
                error -> {
                    Toast.makeText(this, "Error occured. Please try again!", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(jsonObjectRequest);

    }

    private boolean isLocationPermissionGranted() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE);
    }

    private void startUpdateLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {

                Location location = locationResult.getLastLocation();
                userCurrentLocation = location;
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mapsFragment.setupUserLocation(location, intentLatLng);

            }
        };
        if (checkPermission()) {
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void stopUpdateLocation() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("The permission is mandatory to use this application.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, REQUEST_CODE);
                        }).create().show();
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startUpdateLocation();
            }
        }

    }

    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

}