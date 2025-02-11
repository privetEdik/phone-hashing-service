package kettlebell.phonehashingservice.controller;

import kettlebell.phonehashingservice.exception.AppException;
import kettlebell.phonehashingservice.service.PhoneHashService;
import kettlebell.phonehashingservice.validator.PhoneNumberValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PhoneHashController {

    private final PhoneHashService phoneHashService;
    private final PhoneNumberValidator phoneNumberValidator;

    @PostMapping("/hash")
    public ResponseEntity<String> getHash(@RequestParam String phoneNumber) {
        if (!phoneNumberValidator.isValid(phoneNumber)) {
            throw new AppException("Phone number must be in the format 380XXXXXXXXX");
        }
        return ResponseEntity.ok(phoneHashService.getOrCreateHash(phoneNumber));
    }

    @GetMapping("/dehash")
    public ResponseEntity<String> getPhoneNumber(@RequestParam String hash) {
        Optional<String> phoneNumberOpt = phoneHashService.findPhoneNumberByHash(hash);
        return phoneNumberOpt.map(ResponseEntity::ok).orElseGet(() ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Phone number not found"));

    }
}
