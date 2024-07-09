package com.example.kingculinary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kingculinary.adapter.myRecipeAdapter;
import com.example.kingculinary.model.modelRecipe;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseAuth auth;
    private ListView listViewSearch;
    private ImageView navbarBtnSearch, navbarBtnPlusR, navbarBtnHome, navbarBtnProfile, btnSidebar;
    private TextView getEmail, getName, getBio;
    private Button buttonEditProfile;
    private CircleImageView circleImageView;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private NavigationView navView;
    private Uri imageUri;
    private CircleImageView dialogEditProfilePicture;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("profile_pictures");

        getEmail = findViewById(R.id.getEmail);
        getName = findViewById(R.id.getName);
        getBio = findViewById(R.id.getBio);
        circleImageView = findViewById(R.id.circleImageView);
        user = auth.getCurrentUser();
        navbarBtnHome = findViewById(R.id.navbarBtnHome);
        navbarBtnSearch = findViewById(R.id.navbarBtnSearch);
        navbarBtnPlusR = findViewById(R.id.navbarBtnPlusR);
        navbarBtnProfile = findViewById(R.id.navbarBtnProfile);
        btnSidebar = findViewById(R.id.btnSidebar);
        buttonEditProfile = findViewById(R.id.buttonEditProfile);
        navView = findViewById(R.id.nav_view);
        navView.setVisibility(View.GONE);
        listViewSearch = findViewById(R.id.listViewSearch);

        listViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                modelRecipe selectedRecipe = (modelRecipe) parent.getItemAtPosition(position);

                Intent intent = new Intent(ProfileActivity.this, EditDetailRecipe.class);
                intent.putExtra("recipeName", selectedRecipe.getRecipeName());
                intent.putExtra("imageFile", selectedRecipe.getImageFile());
                startActivity(intent);
            }
        });

        btnSidebar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleNavView();
            }
        });

        findViewById(android.R.id.content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (navView.getVisibility() == View.VISIBLE) {
                    toggleNavView();
                }
            }
        });

        navView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Consume the event to prevent closing navView when clicked inside
            }
        });

        // Set NavigationView listener
        navView.setNavigationItemSelectedListener(this);

        buttonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        navbarBtnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        navbarBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SearchActivity.class);
                startActivity(intent);
                finish();
            }
        });

        navbarBtnPlusR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, PlusRecipeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        navbarBtnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            getEmail.setText(user.getEmail());
            fetchUserProfile();
            fetchUserRecipes();
        }
    }

    private void toggleNavView() {
        if (navView.getVisibility() == View.GONE) {
            // Slide in from the left
            navView.setTranslationX(-navView.getWidth()); // Start off-screen
            navView.setVisibility(View.VISIBLE);
            navView.animate()
                    .translationX(0) // Slide in to its original position
                    .setDuration(500) // Animation duration in milliseconds
                    .start();
        } else {
            // Slide out to the right
            navView.animate()
                    .translationX(navView.getWidth()) // Slide out off-screen to the right
                    .setDuration(500)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            navView.setVisibility(View.GONE);
                            navView.setTranslationX(0); // Reset position
                        }
                    })
                    .start();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        // Handle click based on item id
        if (itemId == R.id.nav_bookmark) {
            Intent searchIntent = new Intent(this, BookmarkActivity.class);
            startActivity(searchIntent);
        } else if (itemId == R.id.btnLogout) {
            new AlertDialog.Builder(ProfileActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        // Close the navigation drawer after handling the click
        navView.setVisibility(View.GONE);
        return true;
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        dialogEditProfilePicture = dialogView.findViewById(R.id.editProfilePicture);
        TextInputEditText usernameBox = dialogView.findViewById(R.id.usernameBox);
        TextInputEditText bioBox = dialogView.findViewById(R.id.bioBox);

        // Fetch user data
        String userId = user.getUid();
        reference = FirebaseDatabase.getInstance().getReference("user").child(userId);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String bio = snapshot.child("bio").getValue(String.class);
                    String profilePictureUrl = snapshot.child("profilePicture").getValue(String.class);

                    usernameBox.setText(username);
                    bioBox.setText(bio);
                    if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                        // Check if activity is not destroyed before loading image
                        if (!isFinishing() && !isDestroyed()) {
                            // Use ImagePicker library to load image
                            Glide.with(ProfileActivity.this).load(profilePictureUrl).into(dialogEditProfilePicture);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
            }
        });

        dialogEditProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUsername = usernameBox.getText().toString().trim();
                String newBio = bioBox.getText().toString().trim();

                // Validate inputs
                if (TextUtils.isEmpty(newUsername)) {
                    usernameBox.setError("Username is required");
                    return;
                }
                if (TextUtils.isEmpty(newBio)) {
                    bioBox.setError("Bio is required");
                    return;
                }

                // Update user data
                reference.child("username").setValue(newUsername);
                reference.child("bio").setValue(newBio);

                // Upload new profile picture if selected
                if (imageUri != null) {
                    StorageReference fileReference = storageReference.child(userId + ".jpg");
                    fileReference.putFile(imageUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String profilePictureUrl = uri.toString();
                                            reference.child("profilePicture").setValue(profilePictureUrl);

                                            // Show toast for successful edit
                                            Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ProfileActivity.this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // Show toast for successful edit
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }


    private void openImagePicker() {
        // Use ImagePicker library to pick an image
        ImagePicker.Companion.with(ProfileActivity.this)
                .galleryOnly()
                .crop()
                .compress(1024) // Image size compression in KB
                .maxResultSize(1080, 1080) // Max image resolution
                .start(PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (dialogEditProfilePicture != null && !isFinishing() && !isDestroyed()) {
                dialogEditProfilePicture.setImageURI(imageUri);
            }
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
                    String bio = snapshot.child("bio").getValue(String.class);
                    String profilePictureUrl = snapshot.child("profilePicture").getValue(String.class);

                    getName.setText(username);
                    getBio.setText(bio);
                    if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                            Picasso.get().load(profilePictureUrl).into(circleImageView);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to fetch user profile", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchUserRecipes() {
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference("recipes");
        String userEmail = user.getEmail();

        recipesRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<modelRecipe> userRecipes = new ArrayList<>();

                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    modelRecipe recipe = recipeSnapshot.getValue(modelRecipe.class);
                    userRecipes.add(recipe);
                }

                myRecipeAdapter adapter = new myRecipeAdapter(ProfileActivity.this, userRecipes);
                listViewSearch.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to fetch recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
