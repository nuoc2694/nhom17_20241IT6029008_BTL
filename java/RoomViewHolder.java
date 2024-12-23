package com.quanlyphongtro;

import android.icu.text.SimpleDateFormat;
import android.net.ParseException;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Date;
import java.util.Locale;

public class RoomViewHolder extends RecyclerView.ViewHolder {
    TextView RoomNameTV, TenantNameTV, StatusTV;
    Button PaymentButton, DetailButton;

    public RoomViewHolder(@NonNull View itemView) {
        super(itemView);
        RoomNameTV = itemView.findViewById(R.id.RoomName);
        TenantNameTV = itemView.findViewById(R.id.TenantName);
        StatusTV = itemView.findViewById(R.id.Status);
        PaymentButton = itemView.findViewById(R.id.PaymentButton);
        DetailButton = itemView.findViewById(R.id.DetailButton);
    }

    public void bind(Room room) {
        RoomNameTV.setText(room.getRoomName());

        if (!room.isRented()) {
            TenantNameTV.setVisibility(View.GONE);
            StatusTV.setVisibility(View.GONE);
            PaymentButton.setText("Cho thuê");
            DetailButton.setVisibility(View.GONE);
        } else {
            TenantNameTV.setVisibility(View.VISIBLE);
            TenantNameTV.setText(room.getTenantName());

            if (room.isPaid() && isWithinLast30Days(room.getCollectDay())) {
                StatusTV.setVisibility(View.VISIBLE);
                StatusTV.setText("Đã thu");
                PaymentButton.setVisibility(View.GONE);
            } else {
                StatusTV.setVisibility(View.GONE);
                PaymentButton.setVisibility(View.VISIBLE);
                PaymentButton.setText("Thu tiền");
            }

            DetailButton.setVisibility(View.VISIBLE);
            DetailButton.setText("Chi tiết");
        }
    }

    private boolean isWithinLast30Days(String collectDay) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date paymentDate = sdf.parse(collectDay);
            Date currentDate = new Date();

            long diffInMillis = currentDate.getTime() - paymentDate.getTime();
            long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

            return diffInDays <= 30 && diffInDays >= 0;
        } catch (ParseException | java.text.ParseException e) {
            e.printStackTrace();
            return false;
        }
    }



}
