package com.example.anganwadi.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anganwadi.Pojo.Model;
import com.example.anganwadi.Pojo.Utils;
import com.example.anganwadi.R;
import com.example.anganwadi.interfaces.OnStudentItemClick;

import java.util.ArrayList;

public class DashBoardAdapter extends RecyclerView.Adapter<DashBoardAdapter.viewHolder> {


    ArrayList<Model> arrayList;
    Context context;
    OnStudentItemClick onStudentItemClick;

    public DashBoardAdapter(ArrayList<Model> arrayList, Context context, OnStudentItemClick onStudentItemClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onStudentItemClick = onStudentItemClick;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        Model model = arrayList.get(position);
        holder.imageView.setImageResource(model.getImgAdmin());
        holder.total.setText(model.getTotal());

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isConnectedToInternet(context)) {
                    // Call the interface method for all items to handle clicks in AdminDashboard
                    onStudentItemClick.onStudentClick(holder.getAdapterPosition());
                } else {
                    Toast.makeText(context, "No Internet connection.....", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class viewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView total;
        RelativeLayout relativeLayout;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageview);
            total = itemView.findViewById(R.id.tvTotal);
            relativeLayout = itemView.findViewById(R.id.list_item1);
        }
    }
}
