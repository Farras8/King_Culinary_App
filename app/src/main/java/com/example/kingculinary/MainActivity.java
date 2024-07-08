package com.example.kingculinary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private Uri imageUri;
    CircleImageView userPP;
    FirebaseAuth auth;
    ImageView navbarBtnSearch, navbarBtnPlusR, navbarBtnProfile, navbarBtnHome;
    TextView getName, viewAll;
    Button btnGoToPlus;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference reference;
    ViewFlipper viewFlipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        userPP = findViewById(R.id.userPP);
        navbarBtnHome = findViewById(R.id.navbarBtnHome);
        navbarBtnSearch = findViewById(R.id.navbarBtnSearch);
        navbarBtnPlusR = findViewById(R.id.navbarBtnPlusR);
        navbarBtnProfile = findViewById(R.id.navbarBtnProfile);
        getName = findViewById(R.id.getName);
        viewAll = findViewById(R.id.viewAll);
        btnGoToPlus = findViewById(R.id.btnGoToPlus);
        viewFlipper = findViewById(R.id.viewFlipper);
        user = auth.getCurrentUser();

        viewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnGoToPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlusRecipeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        navbarBtnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        navbarBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                finish();
            }
        });

        navbarBtnPlusR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlusRecipeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        navbarBtnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            fetchUserProfile();
            fetchRecipes();
        }
    }

    private void fetchUserProfile() {
        String userId = user.getUid();
        reference = FirebaseDatabase.getInstance().getReference("user").child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String profileImageUrl = snapshot.child("profilePicture").getValue(String.class);

                    getName.setText(username);
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        // Load profile picture into CircleImageView using Picasso
                        Picasso.get().load(profileImageUrl).into(userPP);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void fetchRecipes() {
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference("recipes");
        recipesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                viewFlipper.removeAllViews(); // Clear existing views
                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    String category = recipeSnapshot.child("category").getValue(String.class);
                    if ("Dessert".equals(category)) {
                        String recipeName = recipeSnapshot.child("recipeName").getValue(String.class);
                        String imageUrl = recipeSnapshot.child("imageFile").getValue(String.class);
                        addRecipeToFlipper(recipeName, imageUrl);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to fetch recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addRecipeToFlipper(final String recipeName, final String imageUrl) {
        View view = LayoutInflater.from(this).inflate(R.layout.slider_card, viewFlipper, false);
        TextView recipeNameTextView = view.findViewById(R.id.getRecipeNameMain);
        ImageView recipeImageView = view.findViewById(R.id.getRecipeImageMain);

        recipeNameTextView.setText(recipeName);

        // Load image using Picasso
        Picasso.get().load(imageUrl).into(recipeImageView);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("recipeName", recipeName);
                intent.putExtra("imageFile", imageUrl);
                intent.putExtra("fromActivity", "MainActivity");
                startActivity(intent);
            }
        });

        viewFlipper.addView(view);
    }

}
