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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SearchAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<modelRecipe> mRecipes;
    private String activeUserEmail;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public SearchAdapter(Context context, ArrayList<modelRecipe> recipes, String activeUserEmail) {
        mContext = context;
        mRecipes = recipes;
        this.activeUserEmail = activeUserEmail;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.recycle_recipe, parent, false);
        }

        ImageView imageRecipe = convertView.findViewById(R.id.ImageRecipe);
        TextView recipeName = convertView.findViewById(R.id.recipeName);
        TextView category = convertView.findViewById(R.id.categoryName);
        ImageView bookmarkIcon = convertView.findViewById(R.id.Bookmark);

        Picasso.get()
                .load(mRecipes.get(position).getImageFile())
                .placeholder(R.drawable.loading_icon) // Optional
                .error(R.drawable.loading_icon) // Optional
                .into(imageRecipe);
        recipeName.setText(mRecipes.get(position).getRecipeName());
        category.setText(mRecipes.get(position).getCategory());

        // Retrieve bookmark state
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            String recipeId = String.valueOf(mRecipes.get(position).getRecipeId());
            mDatabase.child("bookmark").child(uid).child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        bookmarkIcon.setBackgroundResource(R.drawable.rounded_bookmark);
                        bookmarkIcon.setColorFilter(mContext.getResources().getColor(R.color.krem));
                        mRecipes.get(position).setBookmarked(true); // update local list state
                    } else {
                        bookmarkIcon.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
                        bookmarkIcon.setColorFilter(mContext.getResources().getColor(R.color.Green));
                        mRecipes.get(position).setBookmarked(false); // update local list state
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(mContext, "Failed to retrieve bookmark status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Implement bookmark toggle
        bookmarkIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBookmark(position, bookmarkIcon);
            }
        });

        // Hide bookmark if recipe belongs to the logged-in user
        if (mRecipes.get(position).getEmail().equals(activeUserEmail)) {
            bookmarkIcon.setVisibility(View.INVISIBLE);
        } else {
            bookmarkIcon.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    private void toggleBookmark(int position, ImageView bookmarkIcon) {
        // Toggle status bookmark
        boolean isBookmarked = mRecipes.get(position).isBookmarked();
        isBookmarked = !isBookmarked;
        mRecipes.get(position).setBookmarked(isBookmarked);

        // Change the icon's background and tint
        if (isBookmarked) {
            bookmarkIcon.setBackgroundResource(R.drawable.rounded_bookmark);
            bookmarkIcon.setColorFilter(mContext.getResources().getColor(R.color.krem));
            addBookmarkToDatabase(position);
        } else {
            bookmarkIcon.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
            bookmarkIcon.setColorFilter(mContext.getResources().getColor(R.color.Green));
            removeBookmarkFromDatabase(position);
        }
    }

    private void addBookmarkToDatabase(int position) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            String recipeId = String.valueOf(mRecipes.get(position).getRecipeId());
            mDatabase.child("bookmark").child(uid).child(recipeId).setValue(true);
            Toast.makeText(mContext, "Bookmarked", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeBookmarkFromDatabase(int position) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            String recipeId = String.valueOf(mRecipes.get(position).getRecipeId());
            mDatabase.child("bookmark").child(uid).child(recipeId).removeValue();
            Toast.makeText(mContext, "Bookmark removed", Toast.LENGTH_SHORT).show();
        }
    }
}
