# BÀI TẬP 3: ĐỌC HIỂU & DÒ LỖI - LẬP TRÌNH PHÒNG THỦ CHỐNG ẢO TƯỞNG THAM SỐ

## 1. PHÂN TÍCH LỖI VÀ RỦI RO TRONG MÃ NGUỒN CŨ

Trong mã nguồn ban đầu:
```java
// Code cũ bị lỗi:
public String checkRoomAvailability(String checkIn, String checkOut, String roomType) {
    LocalDate start = LocalDate.parse(checkIn);
    LocalDate end = LocalDate.parse(checkOut);
    // ...
}
```

### Các điểm yếu và nguy cơ crash hệ thống:
1. **Thiếu Validation Null / Empty:** Nếu LLM chưa trích xuất đủ tham số (ví dụ người dùng không nói loại phòng), giá trị `null` truyền vào `LocalDate.parse(null)` sẽ bắn ngay ra `NullPointerException`.
2. **Crash do sai định dạng ngày (`DateTimeParseException`):** Nếu LLM ảo tưởng hoặc nhận dạng sai chuỗi (vd `"ngày mai"`, `"20-08-2026"`), việc parse trực tiếp sẽ làm sập luồng Function Calling.
3. **Ném Exception ra ngoài Spring AI Stream:** Khi một Spring AI Function quăng ngoại lệ `RuntimeException`, Spring AI Engine sẽ thất bại trong việc hoàn tất Function Call cycle, dẫn đến HTTP 500 Server Error trả về cho người dùng thay vì câu trả lời lịch sự từ AI Agent.

---

## 2. NGUYÊN TẮC THIẾT KẾ LẬP TRÌNH PHÒNG THỦ (DEFENSIVE PROGRAMMING)

1. **Đóng gói tham số đầu vào bằng Java Record DTO:** Sử dụng `@JsonPropertyDescription` giúp LLM hiểu chính xác Schema và định dạng yêu cầu trước khi tạo Function Call Payload.
2. **Cơ chế Error-as-Response (Không ném Exception):** Đóng gói toàn bộ lỗi validation hoặc ngoại lệ hệ thống vào đối tượng trả về `RoomCheckResponse(isSuccess = false, message = "...")`.
3. **Giúp AI Agent tự sửa lỗi (Self-Correction Loop):** Khi nhận được phản hồi `isSuccess = false` kèm `message` giải thích rõ lý do lỗi (như *"Thiếu ngày trả phòng"* hay *"Ngày nhận phòng không thể ở quá khứ"*), LLM sẽ tự động đọc thông điệp này và tương tác với người dùng để hỏi lại đúng thông tin còn thiếu.

---

## 3. CÁC FILE MÃ NGUỒN ĐÃ TÁI CẤU TRÚC

- `RoomCheckRequest.java`: Java Record chứa thông tin yêu cầu với mô tả JSON Schema.
- `RoomCheckResponse.java`: Java Record chuẩn hóa kết quả trả về cho LLM.
- `BookingService.java`: Service triển khai `Function<RoomCheckRequest, RoomCheckResponse>` với đầy đủ các tầng phòng thủ:
  - Null/Empty Check
  - Regex Date Validation (`^\d{4}-\d{2}-\d{2}$`)
  - Date Order Logic (CheckIn >= Today, CheckOut > CheckIn)
  - Try-Catch đệm cho mọi thao tác CSDL / Logic nâng cao.
