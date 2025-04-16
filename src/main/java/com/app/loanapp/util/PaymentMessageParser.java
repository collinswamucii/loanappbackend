package com.app.loanapp.util;

import com.app.loanapp.dto.PaymentMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentMessageParser {
    private static final Pattern MESSAGE_PATTERN = Pattern.compile(
            "\\(([^)]+)\\), Confirmed you have sent (\\d+\\.?\\d*) to Loan management app for account (\\d+) on (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\+\\d{2}:\\d{2})"
    );
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public static PaymentMessage parse(String message) throws IllegalArgumentException {
        Matcher matcher = MESSAGE_PATTERN.matcher(message);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid payment message format");
        }

        PaymentMessage paymentMessage = new PaymentMessage();
        paymentMessage.setPaymentRef(matcher.group(1));
        paymentMessage.setAmount(Double.parseDouble(matcher.group(2)));
        paymentMessage.setPhoneNumber(matcher.group(3));

        try {
            Date datePaid = DATE_FORMAT.parse(matcher.group(4));
            paymentMessage.setDatePaid(datePaid);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format in payment message", e);
        }

        return paymentMessage;
    }
}