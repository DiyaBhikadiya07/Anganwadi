package com.example.anganwadi.Admin;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anganwadi.R;
import com.example.anganwadi.BaseActivity;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class StaffProfileActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_staff_profile);
            CircleImageView profileImageView = findViewById(R.id.profile_image);
            TextView nameTextView = findViewById(R.id.nameTextView);
            TextView ageTextView = findViewById(R.id.ageTextView);
            TextView qualificationTextView = findViewById(R.id.qualificationTextView);
            TextView genderTextView = findViewById(R.id.genderTextView);
            TextView cityTextView = findViewById(R.id.cityTextView);
            TextView phoneTextView = findViewById(R.id.phoneTextView);

            String na = getString(R.string.not_available);

            if (getIntent() != null) {
                String name = getIntent().getStringExtra("name");
                String age = getIntent().getStringExtra("age");
                String qualification = getIntent().getStringExtra("qualification");
                String gender = getIntent().getStringExtra("gender");
                String city = getIntent().getStringExtra("city");
                String phone = getIntent().getStringExtra("phone");
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


                if (gender != null) {
                    if (gender.equals("પુરુષ")) {
                        gender = "Male";
                    } else if (gender.equals("સ્ત્રી")) {
                        gender = "Female";
                    }
                }

                genderTextView.setText(
                        getString(R.string.profile_gender,
                                (gender != null && !gender.isEmpty()) ? gender : na)
                );

                cityTextView.setText(
                        getString(R.string.profile_city,
                                (city != null && !city.isEmpty()) ? city : na)
                );

                phoneTextView.setText(
                        getString(R.string.profile_phone,
                                (phone != null && !phone.isEmpty()) ? phone : na)
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
            Toast.makeText(this,
                    getString(R.string.error_loading_profile),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}