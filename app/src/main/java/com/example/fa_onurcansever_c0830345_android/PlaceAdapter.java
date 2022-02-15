package com.example.fa_onurcansever_c0830345_android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa_onurcansever_c0830345_android.databinding.PlaceRowBinding;
import com.example.fa_onurcansever_c0830345_android.room.Place;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private PlaceRowBinding binding;
    private Context context;
    private List<Place> placeList;
    private ItemClickListener itemClickListener;

    public interface ItemClickListener {
        void onDelete(Place place);
        void onItemClick(Place place);
    }

    public PlaceAdapter(Context context, ItemClickListener itemClickListener) {
        this.context = context;
        this.itemClickListener = itemClickListener;
    }

    public void setPlacesData(List<Place> placeList) {
        this.placeList = placeList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = PlaceRowBinding.inflate(LayoutInflater.from(context), parent, false);
        return new PlaceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        binding.placeNameText.setText(placeList.get(position).getName());
        binding.placeAddressText.setText(placeList.get(position).getAddress());
        binding.placeDateText.setText(placeList.get(position).getDate());
        binding.deleteButton.setOnClickListener(v -> {
            itemClickListener.onDelete(placeList.get(position));
        });
        binding.linearLayout.setOnClickListener(v -> {
            itemClickListener.onItemClick(placeList.get(position));
        });
    }

    @Override
    public int getItemCount() {
        if (placeList == null || placeList.size() == 0) {
            return 0;
        }

        return placeList.size();
    }

    public class PlaceViewHolder extends RecyclerView.ViewHolder {

        public PlaceViewHolder(@NonNull PlaceRowBinding binding) {
            super(binding.getRoot());
        }
    }
}
