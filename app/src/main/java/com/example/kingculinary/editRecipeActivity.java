package com.example.kingculinary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kingculinary.model.modelCategory;
import com.example.kingculinary.model.modelRecipe;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class editRecipeActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    ImageView btnBack, up_photo;
    private Uri imageUri;
    TextInputEditText add_title, addIngredient, addInstruction, add_description;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    Button btnSave, btnCancel;
    FirebaseAuth mAuth;
    AutoCompleteTextView addCategory;
    ArrayAdapter<String> adapterItem;
    ArrayList<String> categoriesList = new ArrayList<>();

    private String recipeId;
    private String imageFile; // To store the existing image URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        up_photo = findViewById(R.id.up_photo);
        add_title = findViewById(R.id.add_title);
        addIngredient = findViewById(R.id.addIngredient);
        addInstruction = findViewById(R.id.addInstruction);
        btnSave = findViewById(R.id.btnEdit);
        btnCancel = findViewById(R.id.btnCancel);
        addCategory = findViewById(R.id.addCategory);
        add_description = findViewById(R.id.add_description);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        databaseReference = FirebaseDatabase.getInstance().getReference("recipes");

        loadCategories();

        Intent intent = getIntent();
        recipeId = intent.getStringExtra("recipeId"); // Ensure recipeId is properly initialized
        String recipeName = intent.getStringExtra("recipeName");
        String ingredients = intent.getStringExtra("ingredients");
        String instructions = intent.getStringExtra("instructions");
        imageFile = intent.getStringExtra("imageFile"); // Get the imageFile URL
        String descriptions = intent.getStringExtra("descriptions");
        String category = intent.getStringExtra("category");

        add_title.setText(recipeName);
        addIngredient.setText(ingredients);
        addInstruction.setText(instructions);
        add_description.setText(descriptions);
        addCategory.setText(category, false);

        if (imageFile != null) {
            Glide.with(this).load(imageFile).into(up_photo); // Load the image using Glide
        }

        addCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(editRecipeActivity.this, "Selected item: " + selectedItem, Toast.LENGTH_SHORT).show();
            }
        });

        up_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEditedRecipe();
            }
        });
    }

    private void loadCategories() {
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("category");
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoriesList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    modelCategory category = snapshot.getValue(modelCategory.class);
                    if (category != null) {
                        categoriesList.add(category.getCatName());
                    }
                }
                adapterItem = new ArrayAdapter<>(editRecipeActivity.this, R.layout.list_item, categoriesList);
                addCategory.setAdapter(adapterItem);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(editRecipeActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            up_photo.setImageURI(imageUri); // Set the selected image to ImageView
        }
    }

    private String getFileExtension(Uri uri) {
        String extension = "";
        try {
            extension = getContentResolver().getType(uri).split("/")[1];
        } catch (Exception e) {
            Toast.makeText(this, "Error getting file extension: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return extension;
    }

    private void saveEditedRecipe() {
        String recipeName = add_title.getText().toString();
        String ingredients = addIngredient.getText().toString();
        String instructions = addInstruction.getText().toString();
        String descriptions = add_description.getText().toString();
        String category = addCategory.getText().toString();

        // Check if recipeId is null
        if (recipeId == null) {
            Toast.makeText(editRecipeActivity.this, "Recipe ID is null", Toast.LENGTH_SHORT).show();
            return; // Exit method if recipeId is null
        }

        if (imageUri != null) {
            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    updateRecipeInDatabase(recipeId, recipeName, ingredients, instructions, descriptions, category, imageUrl);
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(editRecipeActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            // Use imageFile only if it's not null
            if (imageFile != null) {
                updateRecipeInDatabase(recipeId, recipeName, ingredients, instructions, descriptions, category, imageFile);
            } else {
                // Handle the case where there's no new image and no original image retrieved
                Toast.makeText(editRecipeActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateRecipeInDatabase(String recipeId, String recipeName, String ingredients, String instructions, String descriptions, String category, String imageUrl) {
        modelRecipe updatedRecipe = new modelRecipe(recipeId, imageUrl, recipeName, category, descriptions, ingredients, instructions);
        databaseReference.child(recipeId).setValue(updatedRecipe).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(editRecipeActivity.this, "Recipe updated successfully", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity
            } else {
                Toast.makeText(editRecipeActivity.this, "Failed to update recipe: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

