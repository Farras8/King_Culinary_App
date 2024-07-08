package com.example.kingculinary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {
    EditText searchInput;
    ImageView navbarBtnSearch, navbarBtnPlusR, navbarBtnProfile, navbarBtnHome;
    TextView getBreakfast, getLunch, getBrunch, getDinner, getAppetizer, getDessert, getMainCourse;
    CardView btnBreakfast, btnLunch, btnBrunch, btnDinner, btnAppetizer, btnDessert, btnMainCourse, btnAll;
    ListView listViewSearch;
    ArrayList<Recipe> mRecipe;
    SearchAdapter searchAdapter;
    final private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("recipes");
    final private DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("category");
    private String activeUserEmail;
    private CardView activeButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Mendapatkan instance FirebaseAuth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            activeUserEmail = currentUser.getEmail(); // Mendapatkan email pengguna yang sedang login
        }

        navbarBtnHome = findViewById(R.id.navbarBtnHome);
        navbarBtnSearch = findViewById(R.id.navbarBtnSearch);
        navbarBtnPlusR = findViewById(R.id.navbarBtnPlusR);
        navbarBtnProfile = findViewById(R.id.navbarBtnProfile);
        listViewSearch = findViewById(R.id.listViewSearch);
        getBreakfast = findViewById(R.id.getBreakfast);
        getLunch = findViewById(R.id.getLunch);
        getBrunch = findViewById(R.id.getBrunch);
        getDinner = findViewById(R.id.getDinner);
        getAppetizer = findViewById(R.id.getAppetizer);
        getDessert = findViewById(R.id.getDessert);
        getMainCourse = findViewById(R.id.getMainCourse);
        btnBreakfast = findViewById(R.id.btnBreakfast);
        btnLunch = findViewById(R.id.btnLunch);
        btnBrunch = findViewById(R.id.btnBrunch);
        btnDinner = findViewById(R.id.btnDinner);
        btnAppetizer = findViewById(R.id.btnAppetizer);
        btnDessert = findViewById(R.id.btnDessert);
        btnMainCourse = findViewById(R.id.btnMainCourse);
        btnAll = findViewById(R.id.btnAll);
        searchInput = findViewById(R.id.searchInput);

        setActiveButton(btnAll);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used in this case
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Called when the text in searchInput changes
                String searchText = s.toString().toLowerCase(Locale.getDefault());
                filterRecipesByName(searchText);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used in this case
            }
        });

        btnBreakfast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setActiveButton(btnBreakfast);
                filterRecipesByCategory("Breakfast");
            }
        });

        btnLunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setActiveButton(btnLunch);
                filterRecipesByCategory("Lunch");
            }
        });

        btnBrunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setActiveButton(btnBrunch);
                filterRecipesByCategory("Brunch");
            }
        });

        btnDinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setActiveButton(btnDinner);
                filterRecipesByCategory("Dinner");
            }
        });

        btnAppetizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setActiveButton(btnAppetizer);
                filterRecipesByCategory("Appetizer");
            }
        });

        btnDessert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setActiveButton(btnDessert);
                filterRecipesByCategory("Dessert");
            }
        });

        btnMainCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setActiveButton(btnMainCourse);
                filterRecipesByCategory("Main Course");
            }
        });

        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setActiveButton(btnAll);
                displayAllRecipes();
            }
        });

        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    modalCategory category = dataSnapshot.getValue(modalCategory.class);
                    if (category != null) {
                        // Misalnya, sesuaikan TextView berdasarkan catName dari database
                        switch (category.getCatName()) {
                            case "Breakfast":
                                getBreakfast.setText(category.getCatName());
                                break;
                            case "Lunch":
                                getLunch.setText(category.getCatName());
                                break;
                            case "Brunch":
                                getBrunch.setText(category.getCatName());
                                break;
                            case "Dinner":
                                getDinner.setText(category.getCatName());
                                break;
                            case "Appetizer":
                                getAppetizer.setText(category.getCatName());
                                break;
                            case "Dessert":
                                getDessert.setText(category.getCatName());
                                break;
                            case "Main Course":
                                getMainCourse.setText(category.getCatName());
                                break;
                            default:
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });

        listViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Recipe selectedRecipe = (Recipe) parent.getItemAtPosition(position);

                Intent intent = new Intent(SearchActivity.this, DetailActivity.class);
                intent.putExtra("recipeName", selectedRecipe.getRecipeName());
                intent.putExtra("imageFile", selectedRecipe.getImageFile());
                intent.putExtra("fromActivity", "SearchActivity");
                startActivity(intent);
            }
        });

        mRecipe = new ArrayList<>();
        searchAdapter = new SearchAdapter(this, mRecipe, activeUserEmail); // Menyediakan email pengguna yang sedang login
        listViewSearch.setAdapter(searchAdapter);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mRecipe.clear(); // Clear existing data before adding new data
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        mRecipe.add(recipe);
                    }
                }
                searchAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });

        navbarBtnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        navbarBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // No action needed since already in SearchActivity
            }
        });

        navbarBtnPlusR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, PlusRecipeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        navbarBtnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void filterRecipesByCategory(String category) {
        ArrayList<Recipe> filteredRecipes = new ArrayList<>();
        for (Recipe recipe : mRecipe) {
            if (recipe.getCategory() != null && recipe.getCategory().equals(category)) {
                filteredRecipes.add(recipe);
            }
        }
        // Update adapter with filtered recipes
        searchAdapter = new SearchAdapter(SearchActivity.this, filteredRecipes, activeUserEmail);
        listViewSearch.setAdapter(searchAdapter);
    }

    private void filterRecipesByName(String searchText) {
        ArrayList<Recipe> filteredRecipes = new ArrayList<>();
        for (Recipe recipe : mRecipe) {
            // Filter berdasarkan nama resep (case insensitive)
            if (recipe.getRecipeName().toLowerCase(Locale.getDefault()).contains(searchText)) {
                filteredRecipes.add(recipe);
            }
        }
        // Update adapter with filtered recipes
        searchAdapter = new SearchAdapter(SearchActivity.this, filteredRecipes, activeUserEmail);
        listViewSearch.setAdapter(searchAdapter);
    }

    private void displayAllRecipes() {
        // Tampilkan semua resep tanpa filter
        searchAdapter = new SearchAdapter(SearchActivity.this, mRecipe, activeUserEmail);
        listViewSearch.setAdapter(searchAdapter);
    }
    private TextView getTextViewFromCardView(CardView cardView) {
        LinearLayout linearLayout = (LinearLayout) cardView.getChildAt(0);
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View view = linearLayout.getChildAt(i);
            if (view instanceof TextView) {
                return (TextView) view;
            }
        }
        return null;
    }

    private void setActiveButton(CardView selectedButton) {
        // Reset the background color of the previous active button and text color
        if (activeButton != null && activeButton != selectedButton) {
            activeButton.setCardBackgroundColor(getResources().getColor(R.color.kremTua));
            TextView previousTextView = getTextViewFromCardView(activeButton);
            if (previousTextView != null) {
                previousTextView.setTextColor(getResources().getColor(R.color.Green));
            }
        }

        // Set the background color of the selected button and text color
        selectedButton.setCardBackgroundColor(getResources().getColor(R.color.Green));
        TextView selectedTextView = getTextViewFromCardView(selectedButton);
        if (selectedTextView != null) {
            selectedTextView.setTextColor(getResources().getColor(R.color.krem));
        }

        // Update the active button
        activeButton = selectedButton;
    }


}
