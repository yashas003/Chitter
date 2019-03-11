package com.blogspot.yashas003.chitter.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.blogspot.yashas003.chitter.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class StaggeredRecyclerViewAdapter extends RecyclerView.Adapter<StaggeredRecyclerViewAdapter.ViewHolder> {
    private ArrayList<String> mImageUrls;
    private Context context;

    public StaggeredRecyclerViewAdapter(Context context, ArrayList<String> mImages) {
        this.mImageUrls = mImages;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.posts_grid_items, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {

        Picasso
                .get()
                .load(mImageUrls.get(i))
                .into(viewHolder.image);

    }

    @Override
    public int getItemCount() {
        return mImageUrls.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ConstraintLayout parent;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.image = itemView.findViewById(R.id.imageView2);
            this.parent = itemView.findViewById(R.id.parent_layout);
        }
    }
}