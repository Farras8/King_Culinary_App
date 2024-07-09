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

import com.example.kingculinary.model.modelCategory;
import com.example.kingculinary.model.modelRecipe;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.util.ArrayList;

public class PlusRecipeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    ImageView navbarBtnSearch, navbarBtnPlusR, navbarBtnProfile, navbarBtnHome, up_photo;
    private Uri imageUri;
    TextInputEditText add_title, addIngredient, addInstruction, add_description;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    Button btnSave, btnCancel;
    FirebaseAuth mAuth;
    AutoCompleteTextView addCategory;
    ArrayAdapter<String> adapterItem;
    ArrayList<String> categoriesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plus_recipe);

        mAuth = FirebaseAuth.getInstance();
        navbarBtnHome = findViewById(R.id.navbarBtnHome);
        navbarBtnSearch = findViewById(R.id.navbarBtnSearch);
        navbarBtnPlusR = findViewById(R.id.navbarBtnPlusR);
        navbarBtnProfile = findViewById(R.id.navbarBtnProfile);
        up_photo = findViewById(R.id.up_photo);
        add_title = findViewById(R.id.add_title);
        addIngredient = findViewById(R.id.addIngredient);
        addInstruction = findViewById(R.id.addInstruction);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        addCategory = findViewById(R.id.addCategory);
        add_description = findViewById(R.id.add_description);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        databaseReference = FirebaseDatabase.getInstance().getReference("recipes");

        loadCategories();

        addCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(PlusRecipeActivity.this, "Selected item: " + selectedItem, Toast.LENGTH_SHORT).show();
            }
        });

        up_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        navbarBtnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlusRecipeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        navbarBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlusRecipeActivity.this, SearchActivity.class);
                startActivity(intent);
                finish();
            }
        });

        navbarBtnPlusR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlusRecipeActivity.this, PlusRecipeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        navbarBtnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlusRecipeActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecipe();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear all inputs and reset the activity
                add_title.setText("");
                add_description.setText("");
                addIngredient.setText("");
                addInstruction.setText("");
                up_photo.setImageResource(R.drawable.up_photo);
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
                adapterItem = new ArrayAdapter<>(PlusRecipeActivity.this, R.layout.list_item, categoriesList);
                addCategory.setAdapter(adapterItem);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PlusRecipeActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        ImagePicker.Companion.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImagePicker.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                imageUri = uri;
                up_photo.setImageURI(uri); // Set the selected image to ImageView
            } else {
                Toast.makeText(this, "Failed to retrieve image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Image selection canceled", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveRecipe() {
        String title = add_title.getText().toString().trim();
        String ingredients = addIngredient.getText().toString().trim();
        String instructions = addInstruction.getText().toString().trim();
        String category = addCategory.getText().toString().trim();
        String description = add_description.getText().toString().trim();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userEmail = currentUser != null ? currentUser.getEmail() : "";

        if (title.isEmpty() || ingredients.isEmpty() || instructions.isEmpty() || category.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            try {
                String fileName = System.currentTimeMillis() + "." + getFileName(imageUri);
                StorageReference fileReference = storageReference.child(fileName);
                fileReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    try {
                                        String imageUrl = uri.toString();
                                        String recipeId = databaseReference.push().getKey();
                                        modelRecipe recipe = new modelRecipe(recipeId, imageUrl, title, category, description, ingredients, instructions, userEmail);
                                        if (recipeId != null) {
                                            databaseReference.child(recipeId).setValue(recipe).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(PlusRecipeActivity.this, "Recipe added successfully", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(PlusRecipeActivity.this, ProfileActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Toast.makeText(PlusRecipeActivity.this, "Failed to add recipe: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    } catch (Exception e) {
                                        Toast.makeText(PlusRecipeActivity.this, "Error saving recipe: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(PlusRecipeActivity.this, "Upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (Exception e) {
                Toast.makeText(this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        String fileName = "";
        try {
            String lastPathSegment = uri.getLastPathSegment();
            if (lastPathSegment != null) {
                fileName = lastPathSegment.substring(lastPathSegment.lastIndexOf('/') + 1);
            } else {
                throw new Exception("Invalid URI");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error getting file name: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return fileName;
    }
}
