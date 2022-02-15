package com.example.fa_onurcansever_c0830345_android.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public abstract class PlaceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insert(Place place);

    @Delete
    public abstract void delete(Place place);

    @Update
    public abstract void update(Place place);

    @Query("SELECT * FROM place_table")
    public abstract LiveData<List<Place>> getAllPlaces();

    @Query("DELETE FROM place_table")
    public abstract void deleteAll();
}
