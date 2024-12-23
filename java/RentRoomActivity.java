package com.quanlyphongtro;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;

public class RentRoomActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private ActivityResultLauncher<Intent> cameraLauncher;
    EditText TenantNameInput, Phone, StartDateInput,
            RentMonthsInput, RoomPriceInput, ElectricityPriceInput,
            WaterPriceInput, ServicePriceInput;
    Button RentButton, CancelButton, CaptureContractButton;
    ImageView ContractImageView;
    String RoomID;
    DatabaseHelper db;
    Bitmap contractImageBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_rent_room);

        RoomID = getIntent().getStringExtra("RoomID");
        db = new DatabaseHelper(this);
        setTitle("Cho thuê phòng "+ db.getRoomName(RoomID));

        getWidget();
        handleEvents();
    }

    private void getWidget() {
        TenantNameInput = findViewById(R.id.TenantNameInput);
        Phone = findViewById(R.id.Phone);
        StartDateInput = findViewById(R.id.StartDateInput);
        RentMonthsInput = findViewById(R.id.RentMonthsInput);
        RoomPriceInput = findViewById(R.id.RoomPriceInput);
        ElectricityPriceInput = findViewById(R.id.ElectricityPriceInput);
        WaterPriceInput = findViewById(R.id.WaterPriceInput);
        ServicePriceInput = findViewById(R.id.ServicePriceInput);
        RentButton = findViewById(R.id.RentButton);
        CancelButton = findViewById(R.id.CancelButton);
        CaptureContractButton = findViewById(R.id.CaptureContractButton);
        ContractImageView = findViewById(R.id.ContractImageView);
    }

    private void handleEvents() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getExtras() != null) {
                            contractImageBitmap = (Bitmap) data.getExtras().get("data");
                            ContractImageView.setImageBitmap(contractImageBitmap);
                        }
                    } else {
                        Toast.makeText(this, "Chụp ảnh bị hủy hoặc thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        CaptureContractButton.setOnClickListener(v -> checkCameraPermission());

        StartDateInput.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        StartDateInput.setText(date);
                    }, year, month, day);
            datePickerDialog.show();

        });

        RentButton.setOnClickListener(v -> {
            String tenantName = TenantNameInput.getText().toString().trim();
            String phone = Phone.getText().toString().trim();
            String startDate = StartDateInput.getText().toString().trim();
            String rentMonthsStr = RentMonthsInput.getText().toString().trim();
            String roomPriceStr = RoomPriceInput.getText().toString().trim();
            String electricityPriceStr = ElectricityPriceInput.getText().toString().trim();
            String waterPriceStr = WaterPriceInput.getText().toString().trim();
            String servicePriceStr = ServicePriceInput.getText().toString().trim();

            if (tenantName.isEmpty() || phone.isEmpty() || startDate.isEmpty() || rentMonthsStr.isEmpty() ||
                    roomPriceStr.isEmpty() || electricityPriceStr.isEmpty() || waterPriceStr.isEmpty() ||
                    servicePriceStr.isEmpty() || contractImageBitmap == null) {
                Toast.makeText(RentRoomActivity.this, "Vui lòng nhập đầy đủ thông tin và chụp ảnh hợp đồng", Toast.LENGTH_SHORT).show();
                return;
            }

            int tenantPhone = Integer.parseInt(phone);
            int rentMonths = Integer.parseInt(rentMonthsStr);
            double roomPrice = Double.parseDouble(roomPriceStr);
            double electricityPrice = Double.parseDouble(electricityPriceStr);
            double waterPrice = Double.parseDouble(waterPriceStr);
            double servicePrice = Double.parseDouble(servicePriceStr);
            byte[] contractImageBytes = convertBitmapToByteArray(contractImageBitmap);

            boolean result = db.rentRoom(
                    RoomID,
                    tenantName,
                    tenantPhone,
                    startDate,
                    rentMonths,
                    roomPrice,
                    electricityPrice,
                    waterPrice,
                    servicePrice,
                    contractImageBytes
            );

            if (result) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi cho thuê phòng", Toast.LENGTH_SHORT).show();
            }
        });

        CancelButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            Toast.makeText(this, "Không tìm thấy ứng dụng chụp ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }
}
