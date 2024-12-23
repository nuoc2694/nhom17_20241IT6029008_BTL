package com.quanlyphongtro;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {
    // Khai báo biến
    private EditText CollectDateInput, ElectricityInput, WaterInput, PaidInput;
    private TextView DebtTextView, TotalPaymentTextView;
    private Button SearchButton, CalculateButton, SendInvoiceButton, ConfirmButton, BackButton;
    private DatabaseHelper db;
    private String RoomID;
    private double roomPrice, electricityPrice, waterPrice, servicePrice;
    private double currentDebt = 0.0;
    private double totalPayment = 0.0;
    private double oldDebt = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_payment);

        RoomID = getIntent().getStringExtra("RoomID");

        db = new DatabaseHelper(this);

        setTitle("Thu tiền phòng "+ db.getRoomName(RoomID));

        getWidget();

        loadRoomPrices();

        setupButtonActions();
    }

    private void getWidget() {
        CollectDateInput = findViewById(R.id.CollectDateInput);
        ElectricityInput = findViewById(R.id.ElectricityInput);
        WaterInput = findViewById(R.id.WaterInput);
        PaidInput = findViewById(R.id.PaidInput);
        DebtTextView = findViewById(R.id.DebtTextView);
        TotalPaymentTextView = findViewById(R.id.TotalPaymentTextView);
        SearchButton = findViewById(R.id.SearchButton);
        CalculateButton = findViewById(R.id.CalculateButton);
        SendInvoiceButton = findViewById(R.id.SendInvoiceButton);
        ConfirmButton = findViewById(R.id.ConfirmButton);
        BackButton = findViewById(R.id.BackButton);
    }

    private void loadRoomPrices() {
        RoomPrices prices = db.getRoomPrices(RoomID);
        if (prices != null) {
            roomPrice = prices.roomPrice;
            electricityPrice = prices.electricityPrice;
            waterPrice = prices.waterPrice;
            servicePrice = prices.servicePrice;
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin giá phòng", Toast.LENGTH_SHORT).show();
            roomPrice = 0.0;
            electricityPrice = 0.0;
            waterPrice = 0.0;
            servicePrice = 0.0;
        }
    }


    @SuppressLint("Range")
    private void setupButtonActions() {
        CollectDateInput.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        CollectDateInput.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });

        SearchButton.setOnClickListener(v -> {
            String collectDate = CollectDateInput.getText().toString().trim();
            if (collectDate.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập ngày thu", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cursor = db.getPaymentRecord(RoomID, collectDate);
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range")
                double electricity = cursor.getDouble(cursor.getColumnIndex("SoDien"));
                double water = cursor.getDouble(cursor.getColumnIndex("SoNuoc"));
                currentDebt = cursor.getDouble(cursor.getColumnIndex("TienNo"));

                ElectricityInput.setText(String.valueOf((int) electricity));
                WaterInput.setText(String.valueOf((int) water));
                DebtTextView.setText(String.format("%.2f", currentDebt));
                TotalPaymentTextView.setText("");

                cursor.close();
                Toast.makeText(this, "Thông tin ngày thu đã được tải", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không tìm thấy thông tin ngày thu", Toast.LENGTH_SHORT).show();
            }
        });

        CalculateButton.setOnClickListener(v -> {
            try {
                int electricity = Integer.parseInt(ElectricityInput.getText().toString());
                int water = Integer.parseInt(WaterInput.getText().toString());
                totalPayment = electricity * electricityPrice
                        + water * waterPrice
                        + roomPrice
                        + servicePrice
                        + currentDebt;
                oldDebt = currentDebt;
                currentDebt = totalPayment;
                TotalPaymentTextView.setText(String.format("%.2f", totalPayment));
                DebtTextView.setText(String.format("%.2f", currentDebt));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Vui lòng nhập số điện và số nước hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        SendInvoiceButton.setOnClickListener(v -> {
            String collectDate = CollectDateInput.getText().toString().trim();
            String electricityStr = ElectricityInput.getText().toString().trim();
            String waterStr = WaterInput.getText().toString().trim();

            if (collectDate.isEmpty() || electricityStr.isEmpty() || waterStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin để gửi hóa đơn", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int electricity = Integer.parseInt(electricityStr);
                int water = Integer.parseInt(waterStr);

                double electricityCost = electricity * electricityPrice;
                double waterCost = water * waterPrice;

                String tenantPhone = db.getTenantPhone(RoomID);
                if (tenantPhone == null || tenantPhone.isEmpty()) {
                    Toast.makeText(this, "Không tìm thấy số điện thoại của người thuê", Toast.LENGTH_SHORT).show();
                    return;
                }

                String message = String.format(
                        "Hóa đơn phòng %s\nNgày thu: %s\n" +
                                "Số điện: %d - Tiền điện: %.2f\n" +
                                "Số nước: %d - Tiền nước: %.2f\n" +
                                "Giá phòng: %.2f\nGiá dịch vụ: %.2f\n" +
                                "Số tiền còn nợ: %.2f\nSố tiền phải trả: %.2f",
                        db.getRoomName(RoomID), collectDate,
                        electricity, electricityCost,
                        water, waterCost,
                        roomPrice, servicePrice,
                        oldDebt, currentDebt
                );

                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setData(android.net.Uri.parse("sms:" + tenantPhone));
                smsIntent.putExtra("sms_body", message);
                startActivity(smsIntent);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Vui lòng nhập đúng số điện và số nước", Toast.LENGTH_SHORT).show();
            }
        });


        ConfirmButton.setOnClickListener(v -> {
            String collectDate = CollectDateInput.getText().toString().trim();
            String electricityStr = ElectricityInput.getText().toString().trim();
            String waterStr = WaterInput.getText().toString().trim();
            String paidStr = PaidInput.getText().toString().trim();

            if (collectDate.isEmpty() || electricityStr.isEmpty() || waterStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ngày thu, nhập số điện và số nước", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int electricity = Integer.parseInt(electricityStr);
                int water = Integer.parseInt(waterStr);
                double paid = paidStr.isEmpty() ? 0.0 : Double.parseDouble(paidStr);

                currentDebt -= paid;
                boolean isPaidOff = currentDebt <= 0.0;

                boolean isExistingRecord = db.checkPaymentRecordExists(RoomID, collectDate);

                boolean result;
                if (isExistingRecord) {
                    result = db.updatePaymentRecord(RoomID, collectDate, electricity, water, currentDebt, isPaidOff);
                } else {
                    result = db.insertPaymentRecord(RoomID, collectDate, electricity, water, currentDebt, isPaidOff);
                }

                if (result) {
                    DebtTextView.setText(String.format("%.2f", currentDebt));
                    Toast.makeText(this, isExistingRecord ? "Cập nhật thành công" : "Thêm mới thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Lỗi khi lưu dữ liệu", Toast.LENGTH_SHORT).show();
                }

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
            }
        });
        BackButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        });
    }
}