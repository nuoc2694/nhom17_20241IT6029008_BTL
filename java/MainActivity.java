package com.quanlyphongtro;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Khai báo launcher
    private ActivityResultLauncher<Intent> rentRoomLauncher;
    private ActivityResultLauncher<Intent> paymentLauncher;
    private ActivityResultLauncher<Intent> roomDetailsLauncher;

    // Khai báo biến cần thiết
    RecyclerView RoomList;
    EditText RoomNameInput;
    Button AddRoomButton;
    RoomAdapter adapter;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.layout_main);

        getWidget();
        handleEvents();
    }

    // Khởi tạo các thành phần giao diện
    private void getWidget() {
        RoomList = findViewById(R.id.RoomList);
        RoomNameInput = findViewById(R.id.RoomNameInput);
        AddRoomButton = findViewById(R.id.AddRoomButton);
    }

    // Xử lý logic chính
    private void handleEvents() {
        // Thiết lập layout cho danh sách các phòng
        RoomList.setLayoutManager(new LinearLayoutManager(this));

        // Truy cập database
        db = new DatabaseHelper(this);

        // Lấy danh sách phòng từ cơ sở dữ liệu
        List<Room> rooms = db.getAllRooms();

        // Khởi tạo các launcher
        rentRoomLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Xử lý kết quả từ RentRoomActivity
                        List<Room> updatedRooms = db.getAllRooms();
                        adapter.updateData(updatedRooms); // Cập nhật danh sách phòng
                        Toast.makeText(this, "Phòng đã được thuê!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        paymentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Xử lý kết quả từ PaymentActivity
                        List<Room> updatedRooms = db.getAllRooms();
                        adapter.updateData(updatedRooms); // Cập nhật danh sách phòng
                        Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        roomDetailsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Xử lý kết quả từ RoomDetailsActivity
                        List<Room> updatedRooms = db.getAllRooms();
                        adapter.updateData(updatedRooms); // Cập nhật danh sách phòng
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Khởi tạo adapter với các launcher
        adapter = new RoomAdapter(rooms, rentRoomLauncher, paymentLauncher,roomDetailsLauncher);
        RoomList.setAdapter(adapter);

        // Xử lý thêm phòng
        AddRoomButton.setOnClickListener(view -> {
            String TenPhong = RoomNameInput.getText().toString().trim();
            if (TenPhong.isEmpty() || !TenPhong.matches("P[1-9][0-9][0-9]?")) {
                Toast.makeText(MainActivity.this, "Tên phòng không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (db.insertRoom(TenPhong)) {
                Toast.makeText(MainActivity.this, "Thêm phòng thành công", Toast.LENGTH_SHORT).show();
                List<Room> updatedRooms = db.getAllRooms(); // Lấy lại danh sách phòng
                adapter.updateData(updatedRooms); // Cập nhật danh sách phòng
            } else {
                Toast.makeText(MainActivity.this, "Phòng đã tồn tại", Toast.LENGTH_SHORT).show();
            }
        });

        adapter.setOnRoomLongClickListener(room -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xóa phòng")
                    .setMessage("Bạn có chắc chắn muốn xóa phòng "+room.getRoomName()+"? Dữ liệu liên quan sẽ bị xóa.")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        boolean success = db.deleteRoomWithAllData(room.getIdRoom());
                        if (success) {
                            List<Room> updatedRooms = db.getAllRooms(); // Lấy lại danh sách phòng
                            adapter.updateData(updatedRooms);
                            Toast.makeText(this, "Đã xóa phòng", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Không thể xóa phòng", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }
}
