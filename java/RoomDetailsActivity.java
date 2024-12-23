package com.quanlyphongtro;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RoomDetailsActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private TextView contractDetailsTextView;
    private TextView remainingDaysTextView;
    private ListView paymentListView;
    private Button renewContractButton;
    private Button cancelContractButton;
    private Button returnButton;
    private EditText renewRenDay;
    String RoomID;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_room_details);

        getWidget();

        RoomID = getIntent().getStringExtra("RoomID");
        db = new DatabaseHelper(this);
        setTitle("Chi tiết phòng "+ db.getRoomName(RoomID));
        showContractDetails(RoomID);

        showPaymentDetails(RoomID);

        handleEvents();

    }

    private void getWidget() {
        contractDetailsTextView = findViewById(R.id.contractDetailsTextView);
        remainingDaysTextView = findViewById(R.id.remainingDaysTextView);
        paymentListView = findViewById(R.id.paymentListView);
        renewContractButton = findViewById(R.id.renewContractButton);
        cancelContractButton = findViewById(R.id.cancelContractButton);
        renewRenDay = findViewById(R.id.renewRentDay);
        returnButton = findViewById(R.id.returnButton);
    }
    private void handleEvents(){
        renewContractButton.setOnClickListener(v -> {
            try {
                int additionalMonths = Integer.parseInt(renewRenDay.getText().toString().trim());

                boolean success = db.renewContract(RoomID, additionalMonths);

                if (success) {
                    showContractDetails(RoomID);
                    Toast.makeText(this, "Gia hạn hợp đồng thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Gia hạn hợp đồng thất bại!", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Vui lòng nhập số tháng hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });

        cancelContractButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận hủy hợp đồng")
                    .setMessage("Bạn có chắc chắn muốn hủy hợp đồng này không?")
                    .setPositiveButton("Đồng ý", (dialog, which) -> {
                        boolean result = db.deleteContract(RoomID);
                        if (result) {
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            Toast.makeText(RoomDetailsActivity.this, "Hợp đồng đã được hủy", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(RoomDetailsActivity.this, "Lỗi khi hủy hợp đồng!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
        returnButton.setOnClickListener(v->{
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        });
    }
    private void showContractDetails(String maPhong) {
        Cursor cursor = db.getReadableDatabase().rawQuery(
                "SELECT TenNguoiThue, SoDienThoai, NgayThue, SoThangThue, GiaPhong, GiaDien, GiaNuoc, GiaDichVu " +
                        "FROM HOPDONG WHERE MaPhong = ?", new String[]{maPhong});

        if (cursor.moveToFirst()) {
            @SuppressLint("Range") String tenNguoiThue = cursor.getString(cursor.getColumnIndex("TenNguoiThue"));
            @SuppressLint("Range") String soDienThoai = cursor.getString(cursor.getColumnIndex("SoDienThoai"));
            @SuppressLint("Range") String ngayThue = cursor.getString(cursor.getColumnIndex("NgayThue"));
            @SuppressLint("Range") int soThangThue = cursor.getInt(cursor.getColumnIndex("SoThangThue"));
            @SuppressLint("Range") double giaPhong = cursor.getDouble(cursor.getColumnIndex("GiaPhong"));
            @SuppressLint("Range") double giaDien = cursor.getDouble(cursor.getColumnIndex("GiaDien"));
            @SuppressLint("Range") double giaNuoc = cursor.getDouble(cursor.getColumnIndex("GiaNuoc"));
            @SuppressLint("Range") double giaDichVu = cursor.getDouble(cursor.getColumnIndex("GiaDichVu"));

            // Gắn dữ liệu vào TextView
            contractDetailsTextView.setText(
                    "Tên người thuê: " + tenNguoiThue + "\n" +
                            "Số điện thoại: " + soDienThoai + "\n" +
                            "Ngày thuê: " + ngayThue + "\n" +
                            "Số tháng thuê: " + soThangThue + "\n" +
                            "Giá phòng: " + giaPhong + "\n" +
                            "Giá điện: " + giaDien + "\n" +
                            "Giá nước: " + giaNuoc + "\n" +
                            "Giá dịch vụ: " + giaDichVu
            );

            // Tính toán số ngày còn lại
            calculateRemainingDays(ngayThue, soThangThue);
        } else {
            contractDetailsTextView.setText("Không có thông tin hợp đồng!");
            remainingDaysTextView.setText("Số ngày còn lại: Không áp dụng");
            // Ẩn nút gia hạn và hủy hợp đồng nếu không có hợp đồng
            renewContractButton.setVisibility(View.GONE);
            cancelContractButton.setVisibility(View.GONE);
        }
        cursor.close();
    }


    private void showPaymentDetails(String maPhong) {
        Cursor cursor = db.getReadableDatabase().rawQuery(
                "SELECT NgayThu, TienNo FROM THUTIEN WHERE MaPhong = ? ORDER BY NgayThu DESC", new String[]{maPhong});

        List<HashMap<String, String>> paymentList = new ArrayList<>();
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String ngayThu = cursor.getString(cursor.getColumnIndex("NgayThu"));
            @SuppressLint("Range") String tienNo = String.valueOf(cursor.getDouble(cursor.getColumnIndex("TienNo")));

            HashMap<String, String> map = new HashMap<>();
            map.put("NgayThu", "Ngày thu: " + ngayThu);
            map.put("TienNo", "Tiền nợ: " + tienNo);
            paymentList.add(map);
        }
        cursor.close();

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                paymentList,
                android.R.layout.simple_list_item_2,
                new String[]{"NgayThu", "TienNo"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        paymentListView.setAdapter(adapter);
    }



    private void calculateRemainingDays(String startDate, int months) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(sdf.parse(startDate));
            if (startCal == null) {
                remainingDaysTextView.setText("Số ngày còn lại: Không xác định");
                return;
            }

            // Tính ngày hết hạn hợp đồng
            Calendar endCal = (Calendar) startCal.clone();
            endCal.add(Calendar.MONTH, months);
            long endTime = endCal.getTimeInMillis();
            long currentTime = System.currentTimeMillis();

            long diff = endTime - currentTime;
            long daysRemaining = TimeUnit.MILLISECONDS.toDays(diff);

            if (daysRemaining > 0) {
                remainingDaysTextView.setText("Số ngày còn lại: " + daysRemaining + " ngày");
            } else {
                remainingDaysTextView.setText("Số ngày còn lại: Hết hạn");
            }
        } catch (ParseException e) {
            e.printStackTrace();
            remainingDaysTextView.setText("Số ngày còn lại: Lỗi tính toán");
        }
    }
}
