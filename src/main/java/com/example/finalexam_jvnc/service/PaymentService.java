package com.example.finalexam_jvnc.service;

public interface PaymentService {

    String createMoMoPaymentUrl(Double amount, String orderInfo, String returnUrl);

    String generateQRCodeData(Double amount, String accountName, String accountNumber, String bankName);

    boolean verifyMoMoCallback(String resultCode, String amount, String orderId);
    
    String getBankAccountName();
    String getBankAccountNumber();
    String getBankName();
    
    String getMoMoAccountName();
    String getMoMoPhoneNumber();
}

