package com.blogspot.yashas003.chitter.Adapters;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.blogspot.yashas003.chitter.Activities.PostDetailActivity;
import com.blogspot.yashas003.chitter.Model.Posts;
import com.blogspot.yashas003.chitter.R;
import com.blogspot.yashas003.chitter.Utils.PicassoCache;

import java.util.List;

public class GridViewAdapter extends RecyclerView.Adapter<GridViewAdapter.ViewHolder> {

    private List<Posts> post_list;

    public GridViewAdapter(List<Posts> post_list) {
        this.post_list = post_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.posts_grid_items, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Posts posts = post_list.get(i);

        PicassoCache
                .getPicassoInstance(viewHolder.image.getContext())
                .load(posts.getImage_url())
                .placeholder(R.mipmap.postback)
                .into(viewHolder.image);

        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent postIntent = new Intent(v.getContext(), PostDetailActivity.class);
                postIntent.putExtra("post_id", posts.getPost_id());
                v.getContext().startActivity(postIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return post_list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        CardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.cardView = itemView.findViewById(R.id.parent);
            this.image = itemView.findViewById(R.id.grid_post_image);
        }
    }
}
