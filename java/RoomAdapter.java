package com.quanlyphongtro;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomViewHolder> {
    private List<Room> roomList;
    private ActivityResultLauncher<Intent> rentRoomLauncher;
    private ActivityResultLauncher<Intent> paymentLauncher;
    private ActivityResultLauncher<Intent> roomDetailsLauncher;
    private OnRoomLongClickListener onRoomLongClickListener;

    public RoomAdapter(List<Room> roomList,
                       ActivityResultLauncher<Intent> rentRoomLauncher,
                       ActivityResultLauncher<Intent> paymentLauncher,
                       ActivityResultLauncher<Intent> roomDetailsLauncher) {
        this.roomList = roomList;
        this.rentRoomLauncher = rentRoomLauncher;
        this.paymentLauncher = paymentLauncher;
        this.roomDetailsLauncher = roomDetailsLauncher;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.bind(room); // Gắn dữ liệu của phòng vào giao diện

        // Xử lý khi nhấn nút PaymentButton
        holder.PaymentButton.setOnClickListener(v -> {
            Intent intent;
            if (!room.isRented()) {
                // Nếu phòng chưa được thuê, mở RentRoomActivity
                intent = new Intent(v.getContext(), RentRoomActivity.class);
                intent.putExtra("RoomID", room.getIdRoom());
                rentRoomLauncher.launch(intent);
            } else {
                // Nếu phòng đã được thuê, mở PaymentActivity
                intent = new Intent(v.getContext(), PaymentActivity.class);
                intent.putExtra("RoomID", room.getIdRoom());
                paymentLauncher.launch(intent);
            }
        });

        // Xử lý khi nhấn nút DetailButton
        holder.DetailButton.setOnClickListener(v -> {
            // Mở màn hình chi tiết phòng
            Intent intent;
            intent = new Intent(v.getContext(), RoomDetailsActivity.class);
            intent.putExtra("RoomID", room.getIdRoom());
            roomDetailsLauncher.launch(intent);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (onRoomLongClickListener != null) {
                onRoomLongClickListener.onLongClick(room);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    // Cập nhật danh sách phòng
    public void updateData(List<Room> newRooms) {
        this.roomList.clear();
        this.roomList.addAll(newRooms);
        notifyDataSetChanged();
    }

    public interface OnRoomLongClickListener {
        void onLongClick(Room room);
    }

    public void setOnRoomLongClickListener(OnRoomLongClickListener listener) {
        this.onRoomLongClickListener = listener;
    }


}
