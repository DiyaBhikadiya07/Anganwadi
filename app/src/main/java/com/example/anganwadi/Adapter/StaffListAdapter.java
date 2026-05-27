package com.example.anganwadi.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anganwadi.Pojo.StaffData;
import com.example.anganwadi.R;
import com.example.anganwadi.Admin.StaffProfileActivity;

import java.util.List;

public class StaffListAdapter extends RecyclerView.Adapter<StaffListAdapter.ViewHolder> {

    private List<StaffData> staffList;
    private LayoutInflater mInflater;
    private OnItemLongClickListener longClickListener;
    private Context mContext;


    // Constructor
    public StaffListAdapter(Context context, List<StaffData> staffList, OnItemLongClickListener longClickListener) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.staffList = staffList;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.student_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StaffData staff = staffList.get(position);
        holder.myTextView.setText(staff.getName());
        holder.ss.setVisibility(View.GONE);
        holder.myTextView.setOnLongClickListener(view -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(staff.getKey());
            }
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, StaffProfileActivity.class);
            intent.putExtra("name", staff.getName());
            intent.putExtra("age", staff.getAge());
            intent.putExtra("qualification", staff.getQualification());
            intent.putExtra("gender", staff.getGender());
            intent.putExtra("city", staff.getCity());
            intent.putExtra("phone", String.valueOf(staff.getPhoneNo()));
            intent.putExtra("imageUrl", staff.getImageUrl());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView myTextView;
        TextView ss;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.tvStudentName);
            ss = itemView.findViewById(R.id.txtAddNt);
        }

    }

    public interface OnItemLongClickListener {
        void onItemLongClick(String key);
    }

}
