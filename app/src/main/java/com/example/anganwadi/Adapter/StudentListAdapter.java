package com.example.anganwadi.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anganwadi.Pojo.StudentData;
import com.example.anganwadi.R;
import com.example.anganwadi.interfaces.OnNutritionClick;
import com.example.anganwadi.interfaces.OnStudent;

import java.util.ArrayList;
import java.util.List;

public class StudentListAdapter extends RecyclerView.Adapter<StudentListAdapter.ViewHolder> {

    private List<StudentData> studentList;
    private LayoutInflater mInflater;
    private OnItemLongClickListener longClickListener;

    private OnNutritionClick nutritionClick;
    private OnStudent onStudent;

    private Boolean isAdmin;
    private OnSelectionChangeListener selectionChangeListener;

    public StudentListAdapter(Context context, List<StudentData> studentList, OnItemLongClickListener longClickListener, OnNutritionClick nutritionClick, OnStudent onStudent, Boolean isAdmin) {
        this.mInflater = LayoutInflater.from(context);
        this.studentList = studentList;
        this.longClickListener = longClickListener;
        this.nutritionClick = nutritionClick;
        this.onStudent = onStudent;
        this.isAdmin = isAdmin;
    }

    public void setSelectionChangeListener(OnSelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.student_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentData student = studentList.get(position);
        holder.myTextView.setText(student.getName());

        if (isAdmin != null && isAdmin) {
            boolean inSelectionMode = isSelectionMode();
            
            // Checkbox visibility: Show for ALL items if AT LEAST ONE is selected
            holder.cbSelect.setVisibility(inSelectionMode ? View.VISIBLE : View.GONE);
            
            holder.cbSelect.setOnCheckedChangeListener(null);
            holder.cbSelect.setChecked(student.isSelected());

            holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                student.setSelected(isChecked);
                if (selectionChangeListener != null) {
                    selectionChangeListener.onSelectionChanged();
                }
                notifyDataSetChanged(); // Refresh to update visibility for others
            });

            holder.addNutrition.setVisibility(inSelectionMode ? View.GONE : View.VISIBLE);
            holder.addNutrition.setOnClickListener(v -> {
                nutritionClick.onNutritionClick(holder.getAdapterPosition(), student.getKey(), "");
            });

            holder.myTextView.setOnLongClickListener(view -> {
                if (!isSelectionMode()) {
                    student.setSelected(true);
                    notifyDataSetChanged();
                    if (selectionChangeListener != null) {
                        selectionChangeListener.onSelectionChanged();
                    }
                }
                return true;
            });

            holder.myTextView.setOnClickListener(v -> {
                if (isSelectionMode()) {
                    student.setSelected(!student.isSelected());
                    notifyDataSetChanged();
                    if (selectionChangeListener != null) {
                        selectionChangeListener.onSelectionChanged();
                    }
                } else {
                    onStudent.onStudentClick(student.getKey());
                }
            });

        } else {
            holder.addNutrition.setVisibility(View.GONE);
            holder.cbSelect.setVisibility(View.GONE);
            holder.myTextView.setOnClickListener(v -> onStudent.onStudentClick(student.getKey()));
        }
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(ArrayList<StudentData> filteredList) {
        studentList = filteredList;
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        if (studentList == null) return false;
        for (StudentData student : studentList) {
            if (student.isSelected()) return true;
        }
        return false;
    }

    public List<String> getSelectedStudentKeys() {
        List<String> selectedKeys = new ArrayList<>();
        if (studentList != null) {
            for (StudentData student : studentList) {
                if (student.isSelected()) {
                    selectedKeys.add(student.getKey());
                }
            }
        }
        return selectedKeys;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView myTextView;
        TextView addNutrition;
        CheckBox cbSelect;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.tvStudentName);
            addNutrition = itemView.findViewById(R.id.txtAddNt);
            cbSelect = itemView.findViewById(R.id.cbSelect);
        }
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(String key);
    }

    public interface OnSelectionChangeListener {
        void onSelectionChanged();
    }
}
