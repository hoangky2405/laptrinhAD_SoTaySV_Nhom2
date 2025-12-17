package com.example.sotaysv_nhom2.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sotaysv_nhom2.Models.Note;
import com.example.sotaysv_nhom2.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> mListNote;
    private NoteClickListener listener;

    // Các biến cho chế độ chọn nhiều (Selection Mode)
    private boolean isSelectionMode = false;
    private Set<Integer> selectedIds = new HashSet<>();

    public interface NoteClickListener {
        void onNoteClick(Note note);
        void onNoteLongClick(Note note);
        void onSelectionChanged(int count);
    }

    public NoteAdapter(List<Note> mListNote, NoteClickListener listener) {
        this.mListNote = mListNote;
        this.listener = listener;
    }

    // --- Xử lý Selection Mode ---
    public void setSelectionMode(boolean isSelectionMode) {
        this.isSelectionMode = isSelectionMode;
        if (!isSelectionMode) selectedIds.clear();
        notifyDataSetChanged();
    }

    public Set<Integer> getSelectedIds() {
        return selectedIds;
    }

    public void toggleSelection(int noteId) {
        if (selectedIds.contains(noteId)) selectedIds.remove(noteId);
        else selectedIds.add(noteId);

        notifyDataSetChanged(); // Hoặc dùng notifyItemChanged để mượt hơn
        if (listener != null) listener.onSelectionChanged(selectedIds.size());
    }
    // ---------------------------

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = mListNote.get(position);
        if (note == null) return;

        // 1. Set Title & Content
        holder.tvTitle.setText(note.getTitle());
        if (note.getContent() == null || note.getContent().trim().isEmpty()) {
            holder.tvContent.setVisibility(View.GONE);
        } else {
            holder.tvContent.setVisibility(View.VISIBLE);
            holder.tvContent.setText(note.getContent());
        }

        // 2. LOGIC HIỂN THỊ THỜI GIAN VÀ MÀU SẮC (SỬA LỖI TẠI ĐÂY)
        boolean hasAlarm = note.getAlarmTime() != null && !note.getAlarmTime().isEmpty();

        if (hasAlarm) {
            // === CÓ BÁO THỨC ===
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvTime.setText(note.getAlarmTime()); // Hiện giờ báo thức (VD: 08:00)

            holder.imgType.setVisibility(View.VISIBLE);
            holder.imgType.setImageResource(android.R.drawable.ic_lock_idle_alarm);

            // Kiểm tra loại lặp lại dựa trên repeatType (0: 1 lần, 1: Ngày, 2: Tuần)
            if (note.getRepeatType() == 1) { // Hàng ngày
                holder.viewAccent.setBackgroundColor(Color.parseColor("#4CAF50")); // Xanh lá
                holder.imgType.setColorFilter(Color.parseColor("#4CAF50"));
            } else if (note.getRepeatType() == 2) { // Hàng tuần
                holder.viewAccent.setBackgroundColor(Color.parseColor("#9C27B0")); // Tím
                holder.imgType.setColorFilter(Color.parseColor("#9C27B0"));
            } else { // Một lần (repeatType == 0)
                holder.viewAccent.setBackgroundColor(Color.parseColor("#2196F3")); // Xanh dương
                holder.imgType.setColorFilter(Color.parseColor("#2196F3"));
            }
        } else {
            // === NOTE THƯỜNG (KHÔNG CÓ BÁO THỨC) ===
            // Nếu không có báo thức, hiện ngày tạo note (dateTime) thay vì ẩn đi
            if (note.getDateTime() != null && !note.getDateTime().isEmpty()) {
                holder.tvTime.setVisibility(View.VISIBLE);
                holder.tvTime.setText(note.getDateTime());
            } else {
                holder.tvTime.setVisibility(View.GONE);
            }

            holder.imgType.setVisibility(View.VISIBLE);
            holder.imgType.setImageResource(android.R.drawable.ic_menu_edit); // Icon edit/note

            // Màu xám nhạt cho note thường
            int grayColor = Color.parseColor("#9E9E9E");
            holder.imgType.setColorFilter(grayColor);
            holder.viewAccent.setBackgroundColor(grayColor);
        }

        // 3. Xử lý giao diện khi đang chọn nhiều (Selection Mode)
        if (isSelectionMode) {
            // Có thể hiện Checkbox nếu muốn, hoặc chỉ đổi màu nền
            // holder.checkBox.setVisibility(View.VISIBLE);

            if (selectedIds.contains(note.getId())) {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#BBDEFB")); // Màu xanh nhạt khi được chọn
            } else {
                holder.cardView.setCardBackgroundColor(Color.WHITE);
            }
        } else {
            // holder.checkBox.setVisibility(View.GONE);
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }

        // 4. Sự kiện Click
        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(note.getId());
            } else {
                listener.onNoteClick(note);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                listener.onNoteLongClick(note);
                // Tự động chọn item vừa long click
                toggleSelection(note.getId());
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mListNote.size();
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvTime;
        ImageView imgType;
        View viewAccent;
        CheckBox checkBox;
        CardView cardView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_note_title);
            tvContent = itemView.findViewById(R.id.tv_note_content);
            tvTime = itemView.findViewById(R.id.tv_note_time);
            imgType = itemView.findViewById(R.id.img_note_type);
            viewAccent = itemView.findViewById(R.id.view_accent_bar);
            checkBox = itemView.findViewById(R.id.checkbox_select);

            // Tìm CardView cha để đổi màu nền
            // Cách an toàn: tìm theo ID nếu bạn đã đặt ID cho CardView trong XML
            // Nếu root view là CardView thì ép kiểu
            if (itemView instanceof CardView) {
                cardView = (CardView) itemView;
            } else {
                // Nếu CardView nằm bên trong root layout khác
                cardView = itemView.findViewById(R.id.item_card_root);
            }
        }
    }
}