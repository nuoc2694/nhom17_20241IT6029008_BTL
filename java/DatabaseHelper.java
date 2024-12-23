package com.quanlyphongtro;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "QuanLyPhongTro.db";
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE PHONG (" +
                "MaPhong INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TenPhong TEXT NOT NULL);");

        db.execSQL("CREATE TABLE HOPDONG (" +
                "MaHopDong INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MaPhong INTEGER NOT NULL," +
                "TenNguoiThue TEXT NOT NULL," +
                "SoDienThoai INTEGER NOT NULL, " +
                "NgayThue DATE NOT NULL," +
                "SoThangThue INTEGER NOT NULL," +
                "GiaPhong REAL NOT NULL," +
                "GiaDien REAL NOT NULL," +
                "GiaNuoc REAL NOT NULL," +
                "GiaDichVu REAL NOT NULL," +
                "AnhHopDong BLOB," +
                "FOREIGN KEY (MaPhong) REFERENCES PHONG(MaPhong));");

        db.execSQL("CREATE TABLE THUTIEN (" +
                "MaThuTien INTEGER PRIMARY KEY AUTOINCREMENT," +
                "MaPhong INTEGER NOT NULL," +
                "NgayThu DATE NOT NULL," +
                "SoDien REAL NOT NULL," +
                "SoNuoc REAL NOT NULL," +
                "TienNo REAL, " +
                "DaThu BOOLEAN DEFAULT 0," +
                "FOREIGN KEY (MaPhong) REFERENCES PHONG(MaPhong)," +
                "UNIQUE(MaPhong, NgayThu));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS THUTIEN");
        db.execSQL("DROP TABLE IF EXISTS HOPDONG");
        db.execSQL("DROP TABLE IF EXISTS PHONG");
        onCreate(db);
    }

    public boolean insertRoom(String TenPhong) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * " +
                                        "FROM PHONG " +
                                        "WHERE TenPhong = ?",
                new String[]{TenPhong});
        if (cursor.moveToFirst()) {
            cursor.close();
            db.close();
            return false;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("TenPhong", TenPhong);
        long result = db.insert("PHONG", null, values);
        db.close();
        return result != -1;
    }

    public boolean rentRoom(String MaPhong, String TenNguoiThue, int SoDienThoai, String NgayThue, int SoThangThue, double GiaPhong, double GiaDien, double GiaNuoc, double GiaDichVu, byte[] AnhHopDong) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("MaPhong ", MaPhong);
        values.put("TenNguoiThue ", TenNguoiThue);
        values.put("SoDienThoai", SoDienThoai);
        values.put("NgayThue ", NgayThue);
        values.put("SoThangThue ", SoThangThue);
        values.put("GiaPhong ", GiaPhong);
        values.put("GiaDien ", GiaDien);
        values.put("GiaNuoc ", GiaNuoc);
        values.put("GiaDichVu ", GiaDichVu);
        values.put("AnhHopDong ", AnhHopDong);
        long result = db.insert("HOPDONG", null, values);
        db.close();
        return result != -1;
    }

    public List<Room> getAllRooms() {
        List<Room> roomList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT p.MaPhong, p.TenPhong, h.TenNguoiThue, COALESCE(t.DaThu, 0) AS DaThu, t.NgayThu " +
                        "FROM PHONG p " +
                        "LEFT JOIN HOPDONG h ON p.MaPhong = h.MaPhong " +
                        "LEFT JOIN THUTIEN t ON p.MaPhong = t.MaPhong AND t.NgayThu = (SELECT MAX(NgayThu) FROM THUTIEN WHERE MaPhong = p.MaPhong)";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String MaPhong = cursor.getString(cursor.getColumnIndex("MaPhong"));
                @SuppressLint("Range") String TenPhong = cursor.getString(cursor.getColumnIndex("TenPhong"));
                @SuppressLint("Range") String TenNguoiThue = cursor.getString(cursor.getColumnIndex("TenNguoiThue"));
                @SuppressLint("Range") boolean DaThu = cursor.getInt(cursor.getColumnIndex("DaThu")) == 1;
                @SuppressLint("Range") String NgayThu = cursor.getString(cursor.getColumnIndex("NgayThu"));
                Room room = new Room(MaPhong, TenPhong, TenNguoiThue, DaThu, NgayThu);
                roomList.add(room);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return roomList;
    }

    public RoomPrices getRoomPrices(String MaPhong) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT GiaPhong, GiaDien, GiaNuoc, GiaDichVu FROM HOPDONG WHERE MaPhong = ?",
                new String[]{MaPhong}
        );

        RoomPrices prices = null;
        if (cursor.moveToFirst()) {
            @SuppressLint("Range") double roomPrice = cursor.getDouble(cursor.getColumnIndex("GiaPhong"));
            @SuppressLint("Range") double electricityPrice = cursor.getDouble(cursor.getColumnIndex("GiaDien"));
            @SuppressLint("Range") double waterPrice = cursor.getDouble(cursor.getColumnIndex("GiaNuoc"));
            @SuppressLint("Range") double servicePrice = cursor.getDouble(cursor.getColumnIndex("GiaDichVu"));

            prices = new RoomPrices(roomPrice, electricityPrice, waterPrice, servicePrice);
        }
        cursor.close();
        return prices;
    }

    public boolean checkPaymentRecordExists(String MaPhong, String NgayThu) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM THUTIEN WHERE MaPhong = ? AND NgayThu = ?",
                new String[]{MaPhong, NgayThu}
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public Cursor getPaymentRecord(String MaPhong, String NgayThu) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM THUTIEN WHERE MaPhong = ? AND NgayThu = ?", new String[]{MaPhong, NgayThu});
    }

    public boolean updatePaymentRecord(String MaPhong, String NgayThu, int SoDien, int SoNuoc, double TienNo, boolean DaThu) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("SoDien", SoDien);
        values.put("SoNuoc", SoNuoc);
        values.put("TienNo", TienNo);
        values.put("DaThu", DaThu ? 1 : 0);

        int result = db.update("THUTIEN", values, "MaPhong = ? AND NgayThu = ?", new String[]{MaPhong, NgayThu});
        return result > 0;
    }

    public boolean insertPaymentRecord(String MaPhong, String NgayThu, int SoDien, int SoNuoc, double TienNo, boolean DaThu) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("MaPhong", MaPhong);
        values.put("NgayThu", NgayThu);
        values.put("SoDien", SoDien);
        values.put("SoNuoc", SoNuoc);
        values.put("TienNo", TienNo);
        values.put("DaThu", DaThu ? 1 : 0);

        long result = db.insert("THUTIEN", null, values);
        return result != -1;
    }

    @SuppressLint("Range")
    public String getTenantPhone(String MaPhong) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SoDienThoai FROM HOPDONG WHERE MaPhong = ?", new String[]{MaPhong});
        String phone = null;

        if (cursor.moveToFirst()) {
            phone = cursor.getString(cursor.getColumnIndex("SoDienThoai"));
        }
        cursor.close();
        return phone;
    }

    @SuppressLint("Range")
    public String getRoomName(String MaPhong) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT TenPhong FROM PHONG WHERE MaPhong = ?", new String[]{MaPhong});
        String roomName = null;

        if (cursor.moveToFirst()) {
            roomName = cursor.getString(cursor.getColumnIndex("TenPhong"));
        }
        cursor.close();
        return roomName;
    }

    public boolean deleteContract(String maPhong) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete("HOPDONG", "MaPhong = ?", new String[]{maPhong});
        db.close();
        return rowsAffected > 0;
    }

    public boolean renewContract(String maPhong, int additionalMonths) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE HOPDONG " +
                "SET SoThangThue = SoThangThue + ? " +
                "WHERE MaPhong = ?";
        db.execSQL(query, new Object[]{additionalMonths, maPhong});
        db.close();
        return true;
    }

    public boolean deleteRoomWithAllData(String maPhong) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            db.delete("THUTIEN", "MaPhong = ?", new String[]{maPhong});
            db.delete("HOPDONG", "MaPhong = ?", new String[]{maPhong});
            int rowsDeleted = db.delete("PHONG", "MaPhong = ?", new String[]{maPhong});
            db.setTransactionSuccessful();
            return rowsDeleted > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }



}
