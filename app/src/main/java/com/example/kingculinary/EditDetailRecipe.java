package com.example.kingculinary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kingculinary.model.modelCategory;
import com.example.kingculinary.model.modelRecipe;
import com.example.kingculinary.model.modelUser;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class EditDetailRecipe extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    FirebaseAuth auth;
    TextView recipeNameTextView, recipeDateTextView, recipeUsernameTextView, recipeIngredientsTextView, recipeInstructionsTextView, getRecipeDescription, getRecipeCategory;
    ImageView recipeImageView, btnBack,Comment, up_photo;
    CircleImageView userImageView;
    DatabaseReference reference, userReference;
    String imageFile, recipeId;
    private StorageReference storageReference;
    private Uri imageUri;
    FirebaseUser user;
    FirebaseDatabase database;// Declare imageFile and recipeId as class-level variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_detail_recipe);
        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        user = auth.getCurrentUser();
        recipeNameTextView = findViewById(R.id.getRecipeName);
        recipeDateTextView = findViewById(R.id.getRecipeDate);
        recipeUsernameTextView = findViewById(R.id.getRecipeUsername);
        recipeIngredientsTextView = findViewById(R.id.getRecipeIngredient);
        recipeInstructionsTextView = findViewById(R.id.getRecipeInstructions);
        recipeImageView = findViewById(R.id.getRecipeImage);
        userImageView = findViewById(R.id.getUserImage);
        getRecipeDescription = findViewById(R.id.getRecipeDescription);
        getRecipeCategory = findViewById(R.id.getRecipeCategory);
        btnBack = findViewById(R.id.btnBack);
        Comment = findViewById(R.id.Comment);



        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnDel = findViewById(R.id.btnDel);

        Comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditDetailRecipe.this, commentActivity.class);
                intent.putExtra("recipeId", recipeId);
                intent.putExtra("recipeName", recipeNameTextView.getText().toString());
                intent.putExtra("recipeImage", imageFile);
                intent.putExtra("recipeDate", recipeDateTextView.getText().toString());
                intent.putExtra("username", recipeUsernameTextView.getText().toString());

                startActivity(intent);
            }
        });

        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditRecipeDialog();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditDetailRecipe.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        String recipeName = getIntent().getStringExtra("recipeName");

        reference = FirebaseDatabase.getInstance().getReference("recipes");
        userReference = FirebaseDatabase.getInstance().getReference("user");

        reference.orderByChild("recipeName").equalTo(recipeName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    modelRecipe recipe = dataSnapshot.getValue(modelRecipe.class);
                    if (recipe != null) {
                        recipeNameTextView.setText(recipe.getRecipeName());
                        recipeDateTextView.setText(recipe.getCreatedAt());
                        recipeIngredientsTextView.setText(recipe.getIngredients());
                        recipeInstructionsTextView.setText(recipe.getInstructions());
                        getRecipeDescription.setText(recipe.getDescriptions());
                        getRecipeCategory.setText(recipe.getCategory());

                        // Store the imageFile URL
                        imageFile = recipe.getImageFile();
                        recipeId = dataSnapshot.getKey(); // Store the recipeId

                        // Load recipe image using Picasso
                        if (imageFile != null && !imageFile.isEmpty()) {
                            Picasso.get()
                                    .load(imageFile)
                                    .into(recipeImageView);
                        } else {
                            recipeImageView.setImageResource(R.drawable.loading_icon); // Set default image if imageFile is empty
                        }

                        // Retrieve the username from the users table using the email
                        String userEmail = recipe.getEmail();
                        userReference.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                for (DataSnapshot userDataSnapshot : userSnapshot.getChildren()) {
                                    modelUser user = userDataSnapshot.getValue(modelUser.class);
                                    if (user != null) {
                                        recipeUsernameTextView.setText(user.getUsername());

                                        // Load user profile picture using Picasso or set default image
                                        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                                            Picasso.get()
                                                    .load(user.getProfilePicture())
                                                    .error(R.drawable.baseline_account_circle_24) // Set default image if profile picture is not found
                                                    .into(userImageView);
                                        } else {
                                            userImageView.setImageResource(R.drawable.baseline_account_circle_24);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle error if needed
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });


    }

    private void showEditRecipeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditDetailRecipe.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_recipe, null);
        up_photo = dialogView.findViewById(R.id.up_photo);
        AutoCompleteTextView category_box = dialogView.findViewById(R.id.category_box);
        TextInputEditText title_box = dialogView.findViewById(R.id.title_box);
        TextInputEditText description_box = dialogView.findViewById(R.id.description_box);
        TextInputEditText instruction_box = dialogView.findViewById(R.id.instruction_box);
        TextInputEditText ingredient_box = dialogView.findViewById(R.id.ingredient_box);

        // Fetch recipe data
        reference.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String title = snapshot.child("recipeName").getValue(String.class);
                    String category = snapshot.child("category").getValue(String.class);
                    String description = snapshot.child("descriptions").getValue(String.class);
                    String instruction = snapshot.child("instructions").getValue(String.class);
                    String ingredients = snapshot.child("ingredients").getValue(String.class);
                    String recipePictureUrl = snapshot.child("imageFile").getValue(String.class);

                    title_box.setText(title);
                    category_box.setText(category);
                    description_box.setText(description);
                    instruction_box.setText(instruction);
                    ingredient_box.setText(ingredients);

                    if (recipePictureUrl != null && !recipePictureUrl.isEmpty()) {
                        // Check if activity is not destroyed before loading image
                        if (!isFinishing() && !isDestroyed()) {
                            // Use ImagePicker library to load image
                            Glide.with(EditDetailRecipe.this).load(recipePictureUrl).into(up_photo);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });

        AutoCompleteTextView categoryBox = dialogView.findViewById(R.id.category_box);

        // Load categories from Firebase
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("category");
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> categoriesList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    modelCategory category = snapshot.getValue(modelCategory.class);
                    if (category != null) {
                        categoriesList.add(category.getCatName());
                    }
                }
                ArrayAdapter<String> adapterItem = new ArrayAdapter<>(EditDetailRecipe.this, android.R.layout.simple_dropdown_item_1line, categoriesList);
                categoryBox.setAdapter(adapterItem);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditDetailRecipe.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });

        up_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btnEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newTitle = title_box.getText().toString().trim();
                String newCategory = category_box.getText().toString().trim();
                String newDescription = description_box.getText().toString().trim();
                String newInstruction = instruction_box.getText().toString().trim();
                String newIngredients = ingredient_box.getText().toString().trim();

                // Validate inputs
                if (TextUtils.isEmpty(newTitle)) {
                    title_box.setError("Title is required");
                    return;
                }
                if (TextUtils.isEmpty(newCategory)) {
                    category_box.setError("Category is required");
                    return;
                }
                if (TextUtils.isEmpty(newDescription)) {
                    description_box.setError("Description is required");
                    return;
                }
                if (TextUtils.isEmpty(newInstruction)) {
                    instruction_box.setError("Instruction is required");
                    return;
                }
                if (TextUtils.isEmpty(newIngredients)) {
                    ingredient_box.setError("Ingredients are required");
                    return;
                }

                // Update recipe data
                reference.child(recipeId).child("recipeName").setValue(newTitle);
                reference.child(recipeId).child("category").setValue(newCategory);
                reference.child(recipeId).child("descriptions").setValue(newDescription);
                reference.child(recipeId).child("instructions").setValue(newInstruction);
                reference.child(recipeId).child("ingredients").setValue(newIngredients);

                // Upload new recipe photo if selected
                if (imageUri != null) {
                    StorageReference fileReference = storageReference.child(recipeId + ".jpg");
                    fileReference.putFile(imageUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String recipeImageUrl = uri.toString();
                                            reference.child(recipeId).child("imageFile").setValue(recipeImageUrl);

                                            // Refresh data after successful update
                                            refreshRecipeData();
                                            Toast.makeText(EditDetailRecipe.this, "Recipe updated successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(EditDetailRecipe.this, "Failed to upload recipe photo", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // Refresh data after successful update
                    refreshRecipeData();
                    Toast.makeText(EditDetailRecipe.this, "Recipe updated successfully", Toast.LENGTH_SHORT).show();
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

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }



    private void openImagePicker() {
        // Use ImagePicker library to pick an image
        ImagePicker.Companion.with(EditDetailRecipe.this)
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
            if (up_photo!= null && !isFinishing() && !isDestroyed()) {
                up_photo.setImageURI(imageUri);
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditDetailRecipe.this);
        builder.setTitle("Delete Recipe");
        builder.setMessage("Are you sure you want to delete this recipe?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete the recipe from Firebase
                reference.child(recipeId).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditDetailRecipe.this, "Recipe deleted successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(EditDetailRecipe.this, ProfileActivity.class);
                                startActivity(intent);
                                finish(); // Finish current activity to prevent going back to it on back press
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditDetailRecipe.this, "Failed to delete recipe", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void refreshRecipeData() {
        reference.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    modelRecipe updatedRecipe = snapshot.getValue(modelRecipe.class);
                    if (updatedRecipe != null) {
                        // Update TextViews with new data
                        recipeNameTextView.setText(updatedRecipe.getRecipeName());
                        recipeDateTextView.setText(updatedRecipe.getCreatedAt());
                        recipeIngredientsTextView.setText(updatedRecipe.getIngredients());
                        recipeInstructionsTextView.setText(updatedRecipe.getInstructions());
                        getRecipeDescription.setText(updatedRecipe.getDescriptions());
                        getRecipeCategory.setText(updatedRecipe.getCategory());

                        // Load updated recipe image using Picasso
                        Picasso.get()
                                .load(updatedRecipe.getImageFile())
                                .into(recipeImageView);

                        // Retrieve the username from the users table using the email
                        String userEmail = updatedRecipe.getEmail();
                        userReference.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                for (DataSnapshot userDataSnapshot : userSnapshot.getChildren()) {
                                    modelUser user = userDataSnapshot.getValue(modelUser.class);
                                    if (user != null) {
                                        recipeUsernameTextView.setText(user.getUsername());

                                        // Load user profile picture using Picasso or set default image
                                        Picasso.get()
                                                .load(user.getProfilePicture())
                                                .error(R.drawable.baseline_account_circle_24) // Set default image if profile picture is not found
                                                .into(userImageView);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle error if needed
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
    }


}
