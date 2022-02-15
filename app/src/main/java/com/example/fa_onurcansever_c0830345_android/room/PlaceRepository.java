package com.example.fa_onurcansever_c0830345_android.room;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class PlaceRepository {

    private PlaceDao placeDao;
    private LiveData<List<Place>> allPlaces;

    public PlaceRepository(Application application) {
        PlaceDatabase placeDatabase = PlaceDatabase.getInstance(application);
        placeDao = placeDatabase.placeDao();
        allPlaces = placeDao.getAllPlaces();
    }

    public LiveData<List<Place>> getAllPlaces() {
        return allPlaces;
    }

    public void insert(Place place) {
        PlaceDatabase.databaseWriteExecutor.execute(() -> {
            placeDao.insert(place);
        });
    }

    public void delete(Place place) {
        PlaceDatabase.databaseWriteExecutor.execute(() -> {
            placeDao.delete(place);
        });
    }

    public void update(Place place) {
        PlaceDatabase.databaseWriteExecutor.execute(() -> {
            placeDao.update(place);
        });
    }
}
