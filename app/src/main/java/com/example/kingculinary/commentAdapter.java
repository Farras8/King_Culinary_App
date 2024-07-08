package com.example.kingculinary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class commentAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<modalComment> mCommentList;
    private DatabaseReference mUserReference;
    private DatabaseReference mCommentReference;
    private String activeUserEmail;
    private FirebaseAuth mAuth;

    public commentAdapter(Context context, ArrayList<modalComment> commentList, String activeUserEmail) {
        mContext = context;
        mCommentList = commentList;
        mUserReference = FirebaseDatabase.getInstance().getReference("user");
        mCommentReference = FirebaseDatabase.getInstance().getReference("comment");
        this.activeUserEmail = activeUserEmail;
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public int getCount() {
        return mCommentList.size();
    }

    @Override
    public Object getItem(int position) {
        return mCommentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.recycle_comment, parent, false);
        }

        CircleImageView getUserImage = convertView.findViewById(R.id.getUserImage);
        TextView getRecipeUsername = convertView.findViewById(R.id.getRecipeUsername);
        TextView getRecipeComment = convertView.findViewById(R.id.getRecipeComment);
        TextView getRecipeDate = convertView.findViewById(R.id.getRecipeDate);
        ImageView btnDel = convertView.findViewById(R.id.BtnDel);

        modalComment comment = mCommentList.get(position);

        // Set data komentar ke views
        getRecipeComment.setText(comment.getCommentText());
        getRecipeDate.setText(comment.getTimestamp());

        // Mengambil data pengguna (username dan userImage) dari Firebase Realtime Database berdasarkan userId
        mUserReference.child(comment.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        getRecipeUsername.setText(user.getUsername());

                        // Load gambar pengguna menggunakan Glide
                        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                            Glide.with(mContext)
                                    .load(user.getProfilePicture())
                                    .placeholder(R.drawable.baseline_account_circle_24)
                                    .error(R.drawable.baseline_account_circle_24)
                                    .into(getUserImage);
                        } else {
                            // Jika user tidak memiliki gambar profil, tampilkan ikon default
                            getUserImage.setImageResource(R.drawable.baseline_account_circle_24);
                        }

                        // Hide delete button if activeUserEmail is not the creator's email
                        if (!activeUserEmail.equals(user.getEmail())) {
                            btnDel.setVisibility(View.INVISIBLE);
                        } else {
                            btnDel.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, "Gagal memuat pengguna: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Handle delete button click
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get comment ID and delete the comment from Firebase
                String commentId = comment.getCommentId();
                mCommentReference.child(commentId).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(mContext, "Delete Comment Success", Toast.LENGTH_SHORT).show();
                        mCommentList.remove(position);
                        notifyDataSetChanged();
                        // Reload comments in the activity
                        if (mContext instanceof commentActivity) {
                            ((commentActivity) mContext).loadComments();
                        }
                    } else {
                        Toast.makeText(mContext, "Gagal menghapus komentar", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return convertView;
    }

}
