package com.example.kingculinary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kingculinary.R;
import com.example.kingculinary.model.modelRecipe;
import com.squareup.picasso.Picasso;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class BookmarkAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<modelRecipe> mRecipes;

    public BookmarkAdapter(Context context, ArrayList<modelRecipe> recipes) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.recycle_recipe, parent, false);
        }

        ImageView imageRecipe = convertView.findViewById(R.id.ImageRecipe);
        TextView recipeName = convertView.findViewById(R.id.recipeName);
        TextView category = convertView.findViewById(R.id.categoryName);
        ImageView bookmarkIcon = convertView.findViewById(R.id.Bookmark); // Ensure there is an ImageView for the bookmark

        final modelRecipe currentRecipe = mRecipes.get(position);

        Picasso.get()
                .load(currentRecipe.getImageFile())
                .placeholder(R.drawable.loading_icon) // Optional
                .error(R.drawable.loading_icon) // Optional
                .into(imageRecipe);
        recipeName.setText(currentRecipe.getRecipeName());
        category.setText(currentRecipe.getCategory());

        // Set the initial state of the bookmark icon
        bookmarkIcon.setBackgroundResource(R.drawable.rounded_bookmark);
        bookmarkIcon.setColorFilter(mContext.getResources().getColor(R.color.krem));

        // Add functionality to remove bookmark on clicking the bookmark icon
        bookmarkIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookmarkIcon.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
                bookmarkIcon.setColorFilter(mContext.getResources().getColor(R.color.Green));
                removeBookmark(currentRecipe);
            }
        });

        return convertView;
    }

    private void removeBookmark(modelRecipe recipe) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            DatabaseReference bookmarksRef = FirebaseDatabase.getInstance().getReference("bookmark").child(userId);
            bookmarksRef.child(recipe.getRecipeId().toString()).removeValue(); // Remove bookmark from Firebase

            // Optional: Remove from the local list if necessary
            mRecipes.remove(recipe);
            notifyDataSetChanged();

            Toast.makeText(mContext, "Bookmark removed", Toast.LENGTH_SHORT).show();
        }
    }
}
