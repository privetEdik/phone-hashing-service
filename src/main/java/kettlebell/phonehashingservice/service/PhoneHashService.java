package kettlebell.phonehashingservice.service;

import kettlebell.phonehashingservice.HashingConfig;
import kettlebell.phonehashingservice.exception.AppException;
import kettlebell.phonehashingservice.repository.PhoneHashRepository;
import kettlebell.phonehashingservice.repository.entity.PhoneNumberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PhoneHashService {

    private final PhoneHashRepository phoneHashRepository;
    private final HashingConfig hashingConfig;

    public String getOrCreateHash(String phoneNumber) {
        byte[] hashBytes = phoneHashRepository.findHashInBytesByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    byte[] saveBytes;
                    StringBuilder numberPhoneAndDynamicSalt = new StringBuilder(phoneNumber);
                    int countCollisions = 0;
                    do {
                        saveBytes = generateHashPhoneNumberInBytes(numberPhoneAndDynamicSalt.toString());
                        numberPhoneAndDynamicSalt.append(hashingConfig.getSalt());
                        countCollisions++;
                    } while (phoneHashRepository.existsByHashValue(saveBytes) && countCollisions <= hashingConfig.getAllowedNumberOfCollisions());

                    if (countCollisions > hashingConfig.getAllowedNumberOfCollisions()) {
                        throw new AppException("Failed to generate a unique hash after 10 attempts");
                    }

                    phoneHashRepository.save(new PhoneNumberEntity(phoneNumber, saveBytes));
                    return saveBytes;
                });

        return Base64.getEncoder().encodeToString(hashBytes);
    }

    private byte[] generateHashPhoneNumberInBytes(String phoneNumber) {
        try {

            Algorithm algorithm = Algorithm.fromName(hashingConfig.getGroupOfAlgorithms());

            MessageDigest digest = MessageDigest.getInstance(algorithm.getAlgorithm());
            String saltedPhone = phoneNumber + hashingConfig.getSalt();
            return digest.digest(saltedPhone.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new AppException("Invalid hashing algorithm: " + hashingConfig.getGroupOfAlgorithms(), e);
        }

    }


    public Optional<String> findPhoneNumberByHash(String hash) {
        try {
            byte[] hashInBytes = Base64.getDecoder().decode(hash);
            return phoneHashRepository.findPhoneNumberByHash(hashInBytes);
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid Base64 hash format", e);
        }
    }

}


