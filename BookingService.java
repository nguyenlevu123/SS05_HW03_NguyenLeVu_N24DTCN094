package com.example.booking.service;

import com.example.booking.dto.RoomCheckRequest;
import com.example.booking.dto.RoomCheckResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.function.Function;
import java.util.regex.Pattern;

@Service
public class BookingService implements Function<RoomCheckRequest, RoomCheckResponse> {

    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    @Override
    public RoomCheckResponse apply(RoomCheckRequest request) {
        // 1. Kiểm tra Null Pointer & Chuỗi rỗng đối với Request DTO
        if (request == null) {
            return RoomCheckResponse.error("Yêu cầu không hợp lệ. Vui lòng cung cấp đầy đủ thông tin phòng và thời gian đặt phòng.");
        }

        if (request.roomType() == null || request.roomType().isBlank()) {
            return RoomCheckResponse.error("Thiếu thông tin loại phòng (roomType). Vui lòng hỏi lại người dùng muốn chọn loại phòng nào (ví dụ: Standard, Deluxe, VIP).");
        }

        if (request.checkIn() == null || request.checkIn().isBlank()) {
            return RoomCheckResponse.error("Thiếu ngày nhận phòng (checkIn). Vui lòng yêu cầu người dùng cung cấp ngày nhận phòng theo định dạng YYYY-MM-DD.");
        }

        if (request.checkOut() == null || request.checkOut().isBlank()) {
            return RoomCheckResponse.error("Thiếu ngày trả phòng (checkOut). Vui lòng yêu cầu người dùng cung cấp ngày trả phòng theo định dạng YYYY-MM-DD.");
        }

        // 2. Validate định dạng Regex YYYY-MM-DD
        if (!DATE_PATTERN.matcher(request.checkIn().trim()).matches()) {
            return RoomCheckResponse.error("Ngày nhận phòng '" + request.checkIn() + "' không đúng định dạng YYYY-MM-DD. Vui lòng kiểm tra lại.");
        }

        if (!DATE_PATTERN.matcher(request.checkOut().trim()).matches()) {
            return RoomCheckResponse.error("Ngày trả phòng '" + request.checkOut() + "' không đúng định dạng YYYY-MM-DD. Vui lòng kiểm tra lại.");
        }

        // 3. Parse ngày tháng an toàn & Validate logic nghiệp vụ thời gian
        LocalDate checkInDate;
        LocalDate checkOutDate;

        try {
            checkInDate = LocalDate.parse(request.checkIn().trim());
            checkOutDate = LocalDate.parse(request.checkOut().trim());
        } catch (DateTimeParseException e) {
            return RoomCheckResponse.error("Ngày tháng không hợp lệ (ví dụ ngày 30/02 hoặc không đúng chuẩn ISO). Chi tiết: " + e.getMessage());
        }

        LocalDate today = LocalDate.now();

        if (checkInDate.isBefore(today)) {
            return RoomCheckResponse.error("Ngày nhận phòng (" + checkInDate + ") không được ở trong quá khứ. Ngày hiện tại là: " + today);
        }

        if (!checkOutDate.isAfter(checkInDate)) {
            return RoomCheckResponse.error("Ngày trả phòng (" + checkOutDate + ") phải sau ngày nhận phòng (" + checkInDate + ") ít nhất 1 ngày.");
        }

        // 4. Xử lý logic tra cứu dữ liệu thực tế (Ví dụ giả định kiểm tra CSDL)
        try {
            boolean available = checkAvailabilityFromDatabase(request.roomType(), checkInDate, checkOutDate);
            double price = getRoomPricePerNight(request.roomType());

            if (available) {
                return RoomCheckResponse.success(
                    true, 
                    price, 
                    String.format("Phòng %s còn trống từ ngày %s đến ngày %s. Giá phòng: %,.0f VNĐ/đêm.", 
                        request.roomType(), checkInDate, checkOutDate, price)
                );
            } else {
                return RoomCheckResponse.success(
                    false, 
                    price, 
                    String.format("Rất tiếc, loại phòng %s đã hết chỗ trong khoảng thời gian từ %s đến %s.", 
                        request.roomType(), checkInDate, checkOutDate)
                );
            }
        } catch (Exception e) {
            // Bắt mọi ngoại lệ hệ thống và đóng gói thành Response lỗi thay vì quăng Exception sập Stream Spring AI
            return RoomCheckResponse.error("Hệ thống tra cứu phòng đang gặp sự cố tạm thời. Vui lòng thử lại sau. Chi tiết: " + e.getMessage());
        }
    }

    private boolean checkAvailabilityFromDatabase(String roomType, LocalDate checkIn, LocalDate checkOut) {
        // Giả lập logic query DB
        return !"FULL".equalsIgnoreCase(roomType);
    }

    private double getRoomPricePerNight(String roomType) {
        return switch (roomType.toUpperCase()) {
            case "DELUXE" -> 1500000.0;
            case "VIP" -> 2500000.0;
            case "SUITE" -> 3500000.0;
            default -> 800000.0;
        };
    }
}
