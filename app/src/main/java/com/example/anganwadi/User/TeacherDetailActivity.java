package com.example.anganwadi.User;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.anganwadi.BaseActivity;
import com.example.anganwadi.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class TeacherDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_teacher_detail);

            CircleImageView profileImageView = findViewById(R.id.profile_image);
            TextView nameTextView = findViewById(R.id.nameTextView);
            TextView ageTextView = findViewById(R.id.ageTextView);
            TextView qualificationTextView = findViewById(R.id.qualificationTextView);
            TextView genderTextView = findViewById(R.id.genderTextView);

            String na = getString(R.string.not_available);

            if (getIntent() != null) {
                String name = getIntent().getStringExtra("name");
                String age = getIntent().getStringExtra("age");
                String qualification = getIntent().getStringExtra("qualification");
                String gender = getIntent().getStringExtra("gender");
                if (gender != null) {
                    if (gender.equals("પુરુષ")) {
                        gender = "Male";
                    } else if (gender.equals("સ્ત્રી")) {
                        gender = "Female";
                    }
                }
                String imageUrl = getIntent().getStringExtra("imageUrl");

                nameTextView.setText(
                        (name != null && !name.isEmpty())
                                ? name
                                : getString(R.string.profile_name_default)
                );
                ageTextView.setText(
                        getString(R.string.profile_age,
                                (age != null && !age.isEmpty()) ? age : na)
                );
                qualificationTextView.setText(
                        getString(R.string.profile_qualification,
                                (qualification != null && !qualification.isEmpty()) ? qualification : na)
                );
                genderTextView.setText(
                        getString(R.string.profile_gender,
                                (gender != null && !gender.isEmpty()) ? gender : na)
                );

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Picasso.get()
                            .load(imageUrl)
                            .placeholder(R.drawable.profile_member)
                            .error(R.drawable.profile_member)
                            .into(profileImageView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}