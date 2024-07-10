package com.example.kingculinary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kingculinary.adapter.commentAdapter;
import com.example.kingculinary.model.modelComment;
import com.example.kingculinary.model.modelUser;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class commentActivity extends AppCompatActivity {
    CardView cardView5;
    TextInputEditText add_comment;
    CircleImageView getUserImage;
    ListView listViewComment;
    TextView getRecipeName, getRecipeDate, getRecipeUsername;
    ImageView btnSend, getRecipeImage, Bookmark, btnBack;
    String recipeId;
    DatabaseReference commentReference, userReference, bookmarkReference;
    FirebaseAuth firebaseAuth;
    ArrayList<modelComment> commentList;
    commentAdapter adapter;
    private String activeUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        // Initialize views
        add_comment = findViewById(R.id.add_comment);
        getUserImage = findViewById(R.id.getUserImage);
        getRecipeName = findViewById(R.id.getRecipeName);
        getRecipeDate = findViewById(R.id.getRecipeDate);
        getRecipeUsername = findViewById(R.id.getRecipeUsername);
        btnSend = findViewById(R.id.btnSend);
        getRecipeImage = findViewById(R.id.getRecipeImage);
        Bookmark = findViewById(R.id.Bookmark);
        listViewComment = findViewById(R.id.listViewComment);
        btnBack = findViewById(R.id.btnBack);
        cardView5 = findViewById(R.id.cardView5);
        firebaseAuth = FirebaseAuth.getInstance();
        commentReference = FirebaseDatabase.getInstance().getReference("comment");
        userReference = FirebaseDatabase.getInstance().getReference("user");
        bookmarkReference = FirebaseDatabase.getInstance().getReference().child("bookmark");
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            activeUserEmail = currentUser.getEmail();
        }

        // Get data passed from DetailActivity
        String recipeName = getIntent().getStringExtra("recipeName");
        String recipeImage = getIntent().getStringExtra("recipeImage");
        String recipeDate = getIntent().getStringExtra("recipeDate");
        String username = getIntent().getStringExtra("username");
        recipeId = getIntent().getStringExtra("recipeId");

        // Set values to respective views
        getRecipeName.setText(recipeName);
        getRecipeDate.setText(recipeDate);
        getRecipeUsername.setText(username);

        // Load recipe image using Glide
        Picasso.get().load(recipeImage).into(getRecipeImage);

        // Setup adapter for ListView
        commentList = new ArrayList<>();
        adapter = new commentAdapter(this, commentList, activeUserEmail);
        listViewComment.setAdapter(adapter);

        // Load comments from Firebase Realtime Database
        loadComments();
        loadUserProfilePicture();

        // Check if active user is recipe creator and hide cardView5 if true
        checkRecipeBookmark();

        // Send comment button click listener
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = add_comment.getText().toString().trim();
                if (!TextUtils.isEmpty(commentText)) {
                    sendComment(commentText);
                    loadComments();
                } else {
                    Toast.makeText(commentActivity.this, "Komentar tidak boleh kosong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void loadComments() {
        // Clear existing comments
        commentList.clear();

        // Query comments for the current recipeId
        commentReference.orderByChild("recipeId").equalTo(recipeId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    modelComment comment = snapshot.getValue(modelComment.class);
                    if (comment != null) {
                        commentList.add(comment);
                    }
                }
                adapter.notifyDataSetChanged(); // Update ListView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(commentActivity.this, "Gagal memuat komentar: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendComment(String commentText) {
        // Get current user's UID
        String userId = firebaseAuth.getCurrentUser().getUid();

        // Create a new reference for the comment
        String commentId = commentReference.push().getKey();

        // Get current timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Create comment object
        modelComment comment = new modelComment(commentId, recipeId, userId, commentText);

        // Save comment to Firebase Realtime Database
        commentReference.child(commentId).setValue(comment)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(commentActivity.this, "Success Add Comment", Toast.LENGTH_SHORT).show();
                        add_comment.setText("");
                        finish();
                        startActivity(getIntent());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(commentActivity.this, "Gagal menambahkan komentar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkRecipeBookmark() {
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);
        recipesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String creatorEmail = dataSnapshot.child("email").getValue(String.class);
                    if (creatorEmail != null && creatorEmail.equals(activeUserEmail)) {
                        cardView5.setVisibility(View.GONE);
                        Bookmark.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        // Toggle bookmark button based on user's bookmark status
        toggleBookmarkButton();
    }

    private void toggleBookmarkButton() {
        String uid = firebaseAuth.getCurrentUser().getUid();
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
                                            Toast.makeText(commentActivity.this, "Bookmark removed", Toast.LENGTH_SHORT).show();
                                            finish();
                                            startActivity(getIntent());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(commentActivity.this, "Failed to remove bookmark: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // Not bookmarked, add bookmark
                            bookmarkReference.child(uid).child(recipeId).setValue(true)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(commentActivity.this, "Bookmarked", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(commentActivity.this, "Failed to bookmark: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(commentActivity.this, "Failed to retrieve bookmark status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserProfilePicture() {
        DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);
        recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String creatorEmail = dataSnapshot.child("email").getValue(String.class);
                    if (creatorEmail != null) {
                        userReference.orderByChild("email").equalTo(creatorEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userDataSnapshot) {
                                for (DataSnapshot userData : userDataSnapshot.getChildren()) {
                                    modelUser user = userData.getValue(modelUser.class);
                                    if (user != null) {
                                        Glide.with(commentActivity.this)
                                                .load(user.getProfilePicture())
                                                .error(R.drawable.baseline_account_circle_24)
                                                .into(getUserImage);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle error
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}
