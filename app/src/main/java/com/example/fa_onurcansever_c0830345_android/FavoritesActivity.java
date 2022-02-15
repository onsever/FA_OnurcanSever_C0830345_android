package com.example.fa_onurcansever_c0830345_android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;

import com.example.fa_onurcansever_c0830345_android.databinding.ActivityFavoritesBinding;
import com.example.fa_onurcansever_c0830345_android.room.Place;
import com.example.fa_onurcansever_c0830345_android.viewmodel.PlaceViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity implements PlaceAdapter.ItemClickListener {

    private ActivityFavoritesBinding binding;
    private PlaceViewModel placeViewModel;
    private PlaceAdapter placeAdapter;
    private MapsFragment mapsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoritesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        placeViewModel = new ViewModelProvider.AndroidViewModelFactory(this.getApplication()).create(PlaceViewModel.class);

        placeAdapter = new PlaceAdapter(this, this);
        binding.placesRecyclerView.setAdapter(placeAdapter);
        binding.placesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onResume() {
        super.onResume();

        placeViewModel.getAllPlaces().observe(this, places -> {
            placeAdapter.setPlacesData(places);
        });
    }

    @Override
    public void onDelete(Place place) {
        placeViewModel.delete(place);
        placeAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(Place place) {
        Intent intent = new Intent();
        intent.putExtra("lat", place.getLatitude());
        intent.putExtra("lng", place.getLongitude());
        setResult(RESULT_OK, intent);
        finish();
    }
}