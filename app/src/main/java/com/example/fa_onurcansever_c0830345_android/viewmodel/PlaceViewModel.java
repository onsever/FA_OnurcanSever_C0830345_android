package com.example.fa_onurcansever_c0830345_android.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.fa_onurcansever_c0830345_android.room.Place;
import com.example.fa_onurcansever_c0830345_android.room.PlaceDatabase;
import com.example.fa_onurcansever_c0830345_android.room.PlaceRepository;

import java.util.List;

public class PlaceViewModel extends AndroidViewModel {

    private PlaceRepository repository;
    private final LiveData<List<Place>> allPlaces;

    public PlaceViewModel(@NonNull Application application) {
        super(application);

        repository = new PlaceRepository(application);
        allPlaces = repository.getAllPlaces();
    }

    public LiveData<List<Place>> getAllPlaces() {
        return allPlaces;
    }

    public void insert(Place place) {
        repository.insert(place);
    }

    public void delete(Place place) {
        repository.delete(place);
    }

    public void update(Place place) {
        repository.update(place);
    }
}
