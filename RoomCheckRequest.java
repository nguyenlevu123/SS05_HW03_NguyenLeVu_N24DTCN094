package com.example.booking.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record RoomCheckRequest(
    @JsonPropertyDescription("Loại phòng khách sạn cần kiểm tra (ví dụ: Standard, Deluxe, VIP, Suite)")
    String roomType,

    @JsonPropertyDescription("Ngày nhận phòng theo định dạng ISO-8601 (YYYY-MM-DD), ví dụ: 2026-08-20")
    String checkIn,

    @JsonPropertyDescription("Ngày trả phòng theo định dạng ISO-8601 (YYYY-MM-DD), ví dụ: 2026-08-22")
    String checkOut
) {}
