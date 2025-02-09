package kettlebell.phonehashingservice.validator;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PhoneNumberValidator {

    private final Pattern PHONE_PATTERN = Pattern.compile("^380\\d{9}$");

    public boolean isValid(String phoneNumber) {
        return PHONE_PATTERN.matcher(phoneNumber).matches();
    }
}
