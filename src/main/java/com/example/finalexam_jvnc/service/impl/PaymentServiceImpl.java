package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class PaymentServiceImpl implements PaymentService {
    
    @Value("${momo.url:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String momoUrl;
    
    @Value("${momo.partner.code:YOUR_PARTNER_CODE}")
    private String momoPartnerCode;
    
    @Value("${momo.access.key:YOUR_ACCESS_KEY}")
    private String momoAccessKey;
    
    @Value("${momo.secret.key:YOUR_SECRET_KEY}")
    private String momoSecretKey;
    
    // Thông tin tài khoản ngân hàng
    private static final String BANK_ACCOUNT_NAME = "Lê Nguyễn Tiến Đạt";
    private static final String BANK_ACCOUNT_NUMBER = "0833562999";
    private static final String BANK_NAME = "VietinBank";
    
    // Thông tin tài khoản MoMo
    private static final String MOMO_ACCOUNT_NAME = "Lê Nguyễn Tiến Đạt";
    private static final String MOMO_PHONE_NUMBER = "0833562999";
    
    private final RestTemplate restTemplate;
    
    public PaymentServiceImpl() {
        this.restTemplate = new RestTemplate();
    }
    
    @Override
    public String createMoMoPaymentUrl(Double amount, String orderInfo, String returnUrl) {
        try {
            String orderId = generateOrderId();
            String requestId = generateRequestId();
            long amountLong = amount.longValue();
            
            // Tạo raw signature theo đúng thứ tự của MoMo
            String rawHash = "accessKey=" + momoAccessKey +
                    "&amount=" + amountLong +
                    "&extraData=" +
                    "&ipnUrl=" + returnUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + momoPartnerCode +
                    "&redirectUrl=" + returnUrl +
                    "&requestId=" + requestId +
                    "&requestType=captureWallet";
            
            // Tạo signature
            String signature = hmacSHA256(momoSecretKey, rawHash);
            
            // Tạo JSON request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", momoPartnerCode);
            requestBody.put("partnerName", "UCOP");
            requestBody.put("storeId", "UCOP Store");
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amountLong);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", returnUrl);
            requestBody.put("ipnUrl", returnUrl);
            requestBody.put("lang", "vi");
            requestBody.put("extraData", "");
            requestBody.put("requestType", "captureWallet");
            requestBody.put("signature", signature);
            
            // Gửi POST request đến MoMo API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            try {
                @SuppressWarnings("unchecked")
                ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                    momoUrl, request, (Class<Map<String, Object>>)(Class<?>)Map.class);
                
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    // MoMo trả về payUrl trong response
                    if (responseBody.containsKey("payUrl")) {
                        return (String) responseBody.get("payUrl");
                    } else if (responseBody.containsKey("resultCode")) {
                        // Nếu có lỗi, xử lý và throw exception với message thân thiện
                        Object resultCodeObj = responseBody.get("resultCode");
                        String resultCode = resultCodeObj != null ? resultCodeObj.toString() : "Unknown";
                        Object messageObj = responseBody.getOrDefault("message", "Unknown error");
                        String message = messageObj != null ? messageObj.toString() : "Unknown error";
                        
                        // Xử lý các mã lỗi phổ biến của MoMo
                        String friendlyMessage = getMoMoErrorMessage(resultCode, message);
                        throw new RuntimeException(friendlyMessage);
                    }
                }
                
                throw new RuntimeException("Invalid response from MoMo API");
            } catch (org.springframework.web.client.RestClientException e) {
                // Nếu không thể kết nối đến MoMo API, log và throw
                throw new RuntimeException("Error connecting to MoMo API: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new RuntimeException("Error creating MoMo payment URL", e);
        }
    }
    
    @Override
    public String generateQRCodeData(Double amount, String accountName, String accountNumber, String bankName) {
        // Tạo dữ liệu QR code theo định dạng đơn giản cho chuyển khoản
        // Format: SốTK|SốTiền (format đơn giản nhất, nhiều app ngân hàng hỗ trợ)
        try {
            long amountLong = amount.longValue();
            
            // Format đơn giản nhất: STK|Số tiền (không có dấu phẩy, không có dấu cách)
            // Format này được nhiều app ngân hàng Việt Nam hỗ trợ
            String qrData = String.format("%s|%d", accountNumber, amountLong);
            
            return qrData;
        } catch (Exception e) {
            // Fallback: chỉ số tài khoản
            return accountNumber;
        }
    }
    
    @Override
    public boolean verifyMoMoCallback(String resultCode, String amount, String orderId) {
        // Kiểm tra result code: 0 = thành công
        return "0".equals(resultCode);
    }
    
    private String generateOrderId() {
        return "MOMO" + System.currentTimeMillis();
    }
    
    private String generateRequestId() {
        return UUID.randomUUID().toString();
    }
    
    private String hmacSHA256(String key, String data) {
        try {
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSHA256.init(secretKey);
            byte[] digest = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA256", e);
        }
    }
    
    public String getBankAccountName() {
        return BANK_ACCOUNT_NAME;
    }
    
    public String getBankAccountNumber() {
        return BANK_ACCOUNT_NUMBER;
    }
    
    public String getBankName() {
        return BANK_NAME;
    }
    
    public String getMoMoAccountName() {
        return MOMO_ACCOUNT_NAME;
    }
    
    public String getMoMoPhoneNumber() {
        return MOMO_PHONE_NUMBER;
    }
    
    /**
     * Chuyển đổi mã lỗi MoMo thành thông báo thân thiện với người dùng
     */
    private String getMoMoErrorMessage(String resultCode, String originalMessage) {
        // Xử lý các mã lỗi phổ biến của MoMo
        switch (resultCode) {
            case "0":
                return "Thanh toán thành công";
            case "13":
                return "Lỗi kết nối với MoMo. Vui lòng thử lại sau hoặc sử dụng phương thức thanh toán khác.";
            case "1001":
                return "Thông tin thanh toán không hợp lệ";
            case "1002":
                return "Số tiền không hợp lệ";
            case "1003":
                return "Đơn hàng đã tồn tại";
            case "1004":
                return "Thông tin đối tác không hợp lệ";
            case "1005":
                return "Chữ ký không hợp lệ";
            case "1006":
                return "Thời gian thanh toán đã hết hạn";
            default:
                // Nếu không phải mã lỗi đã biết, trả về message gốc nhưng rút gọn
                if (originalMessage != null && originalMessage.length() > 100) {
                    return "Lỗi thanh toán MoMo. Vui lòng thử lại sau hoặc sử dụng phương thức thanh toán khác.";
                }
                return "Lỗi thanh toán MoMo: " + originalMessage;
        }
    }
}
