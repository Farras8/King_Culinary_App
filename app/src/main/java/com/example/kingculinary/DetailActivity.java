package com.example.kingculinary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    TextView recipeNameTextView, recipeDateTextView, recipeUsernameTextView, recipeIngredientsTextView, recipeInstructionsTextView, getRecipeDescription, getRecipeCategory;
    ImageView recipeImageView, btnBack, btnComment, Bookmark, userImageView;

    DatabaseReference databaseReference, userReference, bookmarkReference;
    FirebaseAuth mAuth;
    String activeUserEmail;
    String recipeId;
    String imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        recipeNameTextView = findViewById(R.id.getRecipeName);
        recipeDateTextView = findViewById(R.id.getRecipeDate);
        recipeUsernameTextView = findViewById(R.id.getRecipeUsername);
        recipeIngredientsTextView = findViewById(R.id.getRecipeIngredient);
        recipeInstructionsTextView = findViewById(R.id.getRecipeInstructions);
        recipeImageView = findViewById(R.id.getRecipeImage);
        getRecipeDescription = findViewById(R.id.getRecipeDescription);
        getRecipeCategory = findViewById(R.id.getRecipeCategory);
        userImageView = findViewById(R.id.getUserImage);
        btnBack = findViewById(R.id.btnBack);
        btnComment = findViewById(R.id.btnComment);
        Bookmark = findViewById(R.id.Bookmark);

        mAuth = FirebaseAuth.getInstance();
        bookmarkReference = FirebaseDatabase.getInstance().getReference().child("bookmark");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            activeUserEmail = currentUser.getEmail();
        }

        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this, commentActivity.class);
                intent.putExtra("recipeId", recipeId);
                intent.putExtra("recipeName", recipeNameTextView.getText().toString());
                intent.putExtra("recipeImage", imageFile);
                intent.putExtra("recipeDate", recipeDateTextView.getText().toString());
                intent.putExtra("username", recipeUsernameTextView.getText().toString());

                startActivity(intent);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (getIntent().hasExtra("fromActivity")) {
                    String fromActivity = getIntent().getStringExtra("fromActivity");
                    switch (fromActivity) {
                        case "MainActivity":
                            intent = new Intent(DetailActivity.this, MainActivity.class);
                            break;
                        case "SearchActivity":
                            intent = new Intent(DetailActivity.this, SearchActivity.class);
                            break;
                        case "BookmarkActivity":
                            intent = new Intent(DetailActivity.this, BookmarkActivity.class);
                            break;
                        default:
                            intent = new Intent(DetailActivity.this, MainActivity.class);
                            break;
                    }
                } else {
                    intent = new Intent(DetailActivity.this, MainActivity.class);
                }
                startActivity(intent);
                finish();
            }
        });

        String recipeName = getIntent().getStringExtra("recipeName");
        imageFile = getIntent().getStringExtra("imageFile");

        databaseReference = FirebaseDatabase.getInstance().getReference("recipes");
        userReference = FirebaseDatabase.getInstance().getReference("user");

        databaseReference.orderByChild("recipeName").equalTo(recipeName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        recipeId = dataSnapshot.getKey(); // Get the recipeId
                        recipeNameTextView.setText(recipe.getRecipeName());
                        recipeDateTextView.setText(recipe.getCreatedAt());
                        recipeIngredientsTextView.setText(recipe.getIngredients());
                        recipeInstructionsTextView.setText(recipe.getInstructions());
                        getRecipeDescription.setText(recipe.getDescription());
                        getRecipeCategory.setText(recipe.getCategory());

                        Picasso.get().load(imageFile).into(recipeImageView);

                        String userEmail = recipe.getEmail();
                        if (userEmail.equals(activeUserEmail)) {
                            Bookmark.setVisibility(View.INVISIBLE); // Set Bookmark invisible
                        } else {
                            Bookmark.setVisibility(View.VISIBLE); // Set Bookmark visible
                        }
                        userReference.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                for (DataSnapshot userDataSnapshot : userSnapshot.getChildren()) {
                                    User user = userDataSnapshot.getValue(User.class);
                                    if (user != null) {
                                        recipeUsernameTextView.setText(user.getUsername());
                                        Glide.with(DetailActivity.this)
                                                .load(user.getProfilePicture())
                                                .error(R.drawable.baseline_account_circle_24)
                                                .into(userImageView);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });

                        toggleBookmarkButton(recipeId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void toggleBookmarkButton(String recipeId) {
        String uid = mAuth.getCurrentUser().getUid();
        bookmarkReference.child(uid).child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Recipe is bookmarked
                    Bookmark.setBackgroundResource(R.drawable.rounded_bookmark);
                    Bookmark.setColorFilter(getResources().getColor(R.color.krem));
                } else {
                    // Recipe is not bookmarked
                    Bookmark.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    Bookmark.setColorFilter(getResources().getColor(R.color.Green));
                }

                Bookmark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (snapshot.exists()) {
                            // Already bookmarked, remove bookmark
                            bookmarkReference.child(uid).child(recipeId).removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(DetailActivity.this, "Bookmark removed", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(DetailActivity.this, "Failed to remove bookmark: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // Not bookmarked, add bookmark
                            bookmarkReference.child(uid).child(recipeId).setValue(true)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(DetailActivity.this, "Bookmarked", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(DetailActivity.this, "Failed to bookmark: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, "Failed to retrieve bookmark status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
