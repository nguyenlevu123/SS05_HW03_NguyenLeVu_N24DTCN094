package com.example.booking.dto;

public record RoomCheckResponse(
    boolean isSuccess,
    boolean isAvailable,
    double pricePerNight,
    String message
) {
    // Helper method tạo response lỗi validation
    public static RoomCheckResponse error(String errorMessage) {
        return new RoomCheckResponse(false, false, 0.0, errorMessage);
    }

    // Helper method tạo response thành công
    public static RoomCheckResponse success(boolean isAvailable, double pricePerNight, String message) {
        return new RoomCheckResponse(true, isAvailable, pricePerNight, message);
    }
}
