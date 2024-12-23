package com.quanlyphongtro;

public class Room {
    private String MaPhong;
    private String TenPhong;
    private String TenNguoiThue;
    private boolean DaThu;
    private String NgayThu;
    public Room(String MaPhong, String TenPhong, String TenNguoiThue, boolean DaThu, String NgayThu) {
        this.MaPhong = MaPhong;
        this.TenPhong = TenPhong;
        this.TenNguoiThue = TenNguoiThue;
        this.DaThu = DaThu;
        this.NgayThu = NgayThu;
    }

    public String getCollectDay() {
        return NgayThu;
    }

    public String getIdRoom(){
        return MaPhong;
    }

    public String getRoomName() {
        return TenPhong;
    }

    public String getTenantName() {
        return TenNguoiThue;
    }

    public boolean isRented() {
        return TenNguoiThue != null && !TenNguoiThue.isEmpty();
    }

    public boolean isPaid() {
        return DaThu;
    }
}
