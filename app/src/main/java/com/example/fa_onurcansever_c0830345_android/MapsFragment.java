package com.example.fa_onurcansever_c0830345_android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.fa_onurcansever_c0830345_android.room.Place;
import com.example.fa_onurcansever_c0830345_android.viewmodel.PlaceViewModel;
import com.example.fa_onurcansever_c0830345_android.volley.NearbyPlace;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MapsFragment extends Fragment {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap map;
    private PlaceViewModel placeViewModel;

    private AlertDialog.Builder addNewMarkerDialog;
    private EditText textField;
    private LatLng userLocation;

    public GoogleMap getMap() {
        return map;
    }

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            setMarkerOnMap();
            markerDragging();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        placeViewModel = new ViewModelProvider.AndroidViewModelFactory(this.getActivity().getApplication()).create(PlaceViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    public void setMarkers(MarkerOptions options) {
        map.addMarker(options);
    }

    private void markerDragging() {

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

                PolylineOptions options = new PolylineOptions()
                        .color(Color.RED)
                        .width(5f)
                        .add(userLocation, marker.getPosition());

                map.addPolyline(options);
                marker.showInfoWindow();

                System.out.println("Title " + marker.getTitle());

                String placeName = marker.getTitle();
                double latitude = marker.getPosition().latitude;
                double longitude = marker.getPosition().longitude;
                String fullAddress = getAddress(latitude, longitude);
                String date = getCurrentDate();

                Place place = new Place(placeName, latitude, longitude, date, fullAddress, false);
                placeViewModel.insert(place);

                return true;
            }
        });


    }

    public void setupUserLocation(Location location, LatLng latLng) {
        if (checkPermission()) { return; }

        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        userLocation = userLatLng;
        map.setMyLocationEnabled(true);

        if (latLng == null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }

    }


    private void setMarkerOnMap() {
        map.setOnMapLongClickListener(latLng -> {
            createAlertDialog(latLng);
        });
    }

    public void setNearbyPlacesOnMap(List<NearbyPlace> nearbyPlaces) {
        map.clear();
        for (NearbyPlace place: nearbyPlaces) {
            LatLng latLng = new LatLng(place.getLatitude(), place.getLongitude());
            MarkerOptions options = new MarkerOptions().position(latLng)
                    .title(place.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
            map.addMarker(options);
        }
    }

    private String getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        String fullAddress = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address address = addresses.get(0);
            fullAddress = address.getAddressLine(0);

            System.out.println("Name: " + fullAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fullAddress;
    }

    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
        String date = simpleDateFormat.format(calendar.getTime());

        return date;
    }

    private void createAlertDialog(LatLng latLng) {
        textField = new EditText(getActivity());
        addNewMarkerDialog = new AlertDialog.Builder(getActivity());
        addNewMarkerDialog.setTitle("Add a Favorite Place");
        addNewMarkerDialog.setMessage("Please enter the name of your favorite place.");
        addNewMarkerDialog.setView(textField);

        addNewMarkerDialog.setPositiveButton("Add", (dialog, which) -> {
            String placeName = textField.getText().toString();

            if (TextUtils.isEmpty(placeName)) {
                return;
            }

            MarkerOptions options = new MarkerOptions().position(latLng).title(placeName)
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            map.addMarker(options);

            String fullAddress = getAddress(latLng.latitude, latLng.longitude);
            String currentDate = getCurrentDate();

            Place place = new Place(
                    placeName,
                    latLng.latitude,
                    latLng.longitude,
                    currentDate,
                    fullAddress,
                    false);

            placeViewModel.insert(place);

            dialog.dismiss();
        });

        addNewMarkerDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        addNewMarkerDialog.show();

    }

    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }
}