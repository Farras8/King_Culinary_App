package com.example.kingculinary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BookmarkActivity extends AppCompatActivity {
    ImageView btnBack;
    ListView listViewBookmark;
    BookmarkAdapter bookmarkAdapter;
    ArrayList<Recipe> bookmarkedRecipes;

    DatabaseReference bookmarksRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        btnBack = findViewById(R.id.btnBack);
        listViewBookmark = findViewById(R.id.listViewBookmark);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            bookmarksRef = FirebaseDatabase.getInstance().getReference("bookmark").child(userId);
            loadBookmarkedRecipes();
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookmarkActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        listViewBookmark.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Recipe selectedRecipe = (Recipe) parent.getItemAtPosition(position);

                Intent intent = new Intent(BookmarkActivity.this, DetailActivity.class);
                intent.putExtra("recipeName", selectedRecipe.getRecipeName());
                intent.putExtra("imageFile", selectedRecipe.getImageFile());
                intent.putExtra("fromActivity", "BookmarkActivity");
                startActivity(intent);
            }
        });
    }

    private void loadBookmarkedRecipes() {
        bookmarkedRecipes = new ArrayList<>();
        bookmarkAdapter = new BookmarkAdapter(this, bookmarkedRecipes);
        listViewBookmark.setAdapter(bookmarkAdapter);

        bookmarksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                bookmarkedRecipes.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String recipeId = snapshot.getKey();
                    // Load each recipe based on recipeId from another node in Firebase
                    DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);
                    recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot recipeSnapshot) {
                            Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                            if (recipe != null) {
                                bookmarkedRecipes.add(recipe);
                                bookmarkAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle error
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}
