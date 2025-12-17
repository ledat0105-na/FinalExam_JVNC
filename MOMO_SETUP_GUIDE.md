# HƯỚNG DẪN LẤY MOMO PARTNER CODE, ACCESS KEY VÀ SECRET KEY

## Bước 1: Đăng ký tài khoản MoMo Developer

1. Truy cập website MoMo Developer: https://developers.momo.vn/
2. Click vào **"Đăng ký"** hoặc **"Đăng nhập"** nếu đã có tài khoản
3. Điền đầy đủ thông tin:
   - Tên công ty/doanh nghiệp
   - Email
   - Số điện thoại
   - Địa chỉ
   - Mã số thuế (nếu có)

## Bước 2: Tạo ứng dụng mới

1. Sau khi đăng nhập, vào mục **"Ứng dụng"** hoặc **"Applications"**
2. Click **"Tạo ứng dụng mới"** hoặc **"Create New App"**
3. Điền thông tin ứng dụng:
   - Tên ứng dụng
   - Mô tả
   - Website URL
   - Callback URL

## Bước 3: Lấy Partner Code

1. Sau khi tạo ứng dụng, vào trang chi tiết ứng dụng
2. Tìm mục **"Partner Code"** hoặc **"Mã đối tác"**
3. Copy mã này (ví dụ: `MOMOBKUN20180529`)
4. Đây chính là **momo.partner.code**

## Bước 4: Lấy Access Key

1. Trong cùng trang ứng dụng, tìm mục **"Access Key"**
2. Click vào **"Hiển thị"** hoặc **"Show"** để xem Access Key
3. Copy mã này (ví dụ: `klm05TvNBzhg7h7j`)
4. Đây chính là **momo.access.key**

## Bước 5: Lấy Secret Key

1. Trong cùng trang ứng dụng, tìm mục **"Secret Key"**
2. Click vào **"Hiển thị"** hoặc **"Show"** để xem Secret Key
3. Copy mã này (ví dụ: `at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa`)
4. Đây chính là **momo.secret.key**

## Bước 6: Cấu hình trong application.properties

Mở file: `src/main/resources/application.properties`

Thay thế:

```properties
momo.partner.code=YOUR_PARTNER_CODE
momo.access.key=YOUR_ACCESS_KEY
momo.secret.key=YOUR_SECRET_KEY
```

Bằng:

```properties
momo.partner.code=MOMOBKUN20180529
momo.access.key=klm05TvNBzhg7h7j
momo.secret.key=at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa
```

## Bước 7: Cấu hình Callback URL

1. Vào trang chi tiết ứng dụng trong MoMo Developer
2. Tìm mục **"Callback URL"** hoặc **"IpnUrl"**
3. Nhập URL callback của bạn:
   ```
   http://localhost:8080/customer/wallet/momo/callback
   ```
   (Thay `localhost:8080` bằng domain thực tế khi deploy)

## Lưu ý quan trọng:

### Sandbox (Môi trường test):
- URL: `https://test-payment.momo.vn/v2/gateway/api/create`
- Dùng để test, không tính phí
- Có thể test với tài khoản MoMo test

### Production (Môi trường thật):
- URL: `https://payment.momo.vn/v2/gateway/api/create`
- Cần đăng ký tài khoản thật và được MoMo duyệt
- Có phí giao dịch

## Test với tài khoản MoMo test:

MoMo cung cấp các tài khoản test để kiểm tra:

**Tài khoản test:**
- Số điện thoại: `0123456789`
- Mật khẩu: `123456`
- OTP: `123456`

## Các bước tiếp theo:

1. **Test kết nối:**
   - Chạy ứng dụng
   - Thử nạp tiền qua MoMo
   - Kiểm tra xem có redirect đến trang MoMo không

2. **Test callback:**
   - Sau khi thanh toán thành công trên MoMo
   - MoMo sẽ redirect về URL callback của bạn
   - Kiểm tra xem tiền đã được nạp vào ví chưa

3. **Xử lý lỗi:**
   - Nếu có lỗi, kiểm tra log để xem chi tiết
   - Đảm bảo Partner Code, Access Key và Secret Key đúng
   - Đảm bảo Callback URL đã được cấu hình trong MoMo

## Hỗ trợ:

- Email MoMo: support@momo.vn
- Hotline: 1900 545 426
- Website: https://developers.momo.vn/
- Tài liệu API: https://developers.momo.vn/docs/

## Bảo mật:

⚠️ **QUAN TRỌNG:**
- **KHÔNG** commit Secret Key vào Git
- Sử dụng **environment variables** hoặc **application.properties** để lưu trữ
- Trong production, nên sử dụng **Spring Cloud Config** hoặc **Vault**

## Lưu ý về API MoMo:

MoMo yêu cầu gửi **POST request** với JSON body, không phải GET request với query parameters như VNPay.

Trong code hiện tại, tôi đã tạo URL với query parameters để đơn giản hóa. 
Để tích hợp đầy đủ, bạn cần:

1. Tạo một endpoint trung gian để xử lý POST request
2. Sử dụng RestTemplate hoặc WebClient để gửi POST request đến MoMo
3. Nhận response từ MoMo và redirect user đến payment URL

Ví dụ code đầy đủ sẽ cần:

```java
RestTemplate restTemplate = new RestTemplate();
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_JSON);

HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
ResponseEntity<Map> response = restTemplate.postForEntity(momoUrl, request, Map.class);

String paymentUrl = response.getBody().get("payUrl").toString();
```

