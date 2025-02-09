package kettlebell.phonehashingservice.service;

import kettlebell.phonehashingservice.repository.PhoneHashRepository;
import kettlebell.phonehashingservice.repository.entity.PhoneNumberEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

@Service
public class PhoneHashService {

    private final PhoneHashRepository phoneHashRepository;

    @Value("${hashing.algorithm}")
    private String groupOfAlgorithms;

    @Value("${hashing.salt}")
    private String salt;

    @Value("${hashing.allowed-number-of-collisions}")
    private Integer attempt;

    public PhoneHashService(PhoneHashRepository phoneHashRepository) {
        this.phoneHashRepository = phoneHashRepository;
    }


    public String getOrCreateHash(String phoneNumber) {
        byte[] hashBytes = phoneHashRepository.findHashInBytesByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    byte[] saveBytes;
                    StringBuilder numberPhoneAndDynamicSalt = new StringBuilder(phoneNumber);
                    int countCollisions = 0;
                    do {
                        saveBytes = generateHashPhoneNumberInBytes(numberPhoneAndDynamicSalt.toString());
                        numberPhoneAndDynamicSalt.append(System.currentTimeMillis());
                        countCollisions++;
                    } while (!phoneHashRepository.existsByHashValue(saveBytes) && countCollisions <= attempt);

                    if (countCollisions >= attempt) {
                        throw new RuntimeException("Failed to generate a unique hash after 10 attempts");
                    }

                    phoneHashRepository.save(new PhoneNumberEntity(phoneNumber, saveBytes));
                    return saveBytes;
                });

        return Base64.getEncoder().encodeToString(hashBytes);
    }

    private byte[] generateHashPhoneNumberInBytes(String phoneNumber) {
        try {

            Algorithm algorithm = Algorithm.fromName(groupOfAlgorithms);

            MessageDigest digest = MessageDigest.getInstance(algorithm.getAlgorithm());
            String saltedPhone = phoneNumber + salt;
            return digest.digest(saltedPhone.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Invalid hashing algorithm: " + groupOfAlgorithms, e);
        }

    }


    public Optional<String> findPhoneNumberByHash(String hash) {
        try {
            byte[] hashInBytes = Base64.getDecoder().decode(hash);
            return phoneHashRepository.findPhoneNumberByHash(hashInBytes);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid Base64 hash format", e);
        }
    }

}


