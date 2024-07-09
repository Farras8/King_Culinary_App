package com.example.kingculinary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kingculinary.R;
import com.example.kingculinary.model.modelRecipe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class myRecipeAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<modelRecipe> mRecipes;

    public myRecipeAdapter (Context context, ArrayList<modelRecipe> recipes) {
        mContext = context;
        mRecipes = recipes;
    }

    @Override
    public int getCount() {
        return mRecipes.size();
    }

    @Override
    public Object getItem(int position) {
        return mRecipes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.recycle_my_recipe, parent, false);
        }

        ImageView imageRecipe = convertView.findViewById(R.id.ImageRecipe);
        TextView recipeName = convertView.findViewById(R.id.recipeName);
        TextView category = convertView.findViewById(R.id.categoryName);

        Picasso.get()
                .load(mRecipes.get(position).getImageFile())
                .placeholder(R.drawable.loading_icon) // Optional
                .error(R.drawable.loading_icon) // Optional
                .into(imageRecipe);
        recipeName.setText(mRecipes.get(position).getRecipeName());
        category.setText(mRecipes.get(position).getCategory());

        return convertView;
    }
}
