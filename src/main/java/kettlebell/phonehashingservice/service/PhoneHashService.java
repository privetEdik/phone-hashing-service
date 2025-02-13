package kettlebell.phonehashingservice.service;

import kettlebell.phonehashingservice.config.HashingConfig;
import kettlebell.phonehashingservice.exception.AppException;
import kettlebell.phonehashingservice.repository.PhoneHashRepository;
import kettlebell.phonehashingservice.repository.entity.PhoneNumberEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhoneHashService {

    private final PhoneHashRepository phoneHashRepository;
    private final HashingConfig hashingConfig;

    @Transactional(isolation = Isolation.REPEATABLE_READ, timeout = 10)
    public String getOrCreateHash(String phoneNumber) {
        byte[] hashBytes = phoneHashRepository.findHashInBytesByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    byte[] saveBytes;
                    StringBuilder numberPhoneAndDynamicSalt = new StringBuilder(phoneNumber);

                    for (int countCollisions = 0; countCollisions < hashingConfig.getAllowedNumberOfCollisions(); countCollisions++) {
                        try {
                            saveBytes = generateHashPhoneNumberInBytes(numberPhoneAndDynamicSalt.toString());
                            phoneHashRepository.save(new PhoneNumberEntity(phoneNumber, saveBytes));
                            return saveBytes;  // Успешное сохранение, выходим
                        } catch (DataIntegrityViolationException e) {
                            log.warn("Hash collision detected for phone number: {}. Attempt {}/{}",
                                    phoneNumber, countCollisions + 1, hashingConfig.getAllowedNumberOfCollisions());
                            numberPhoneAndDynamicSalt.append(hashingConfig.getSalt());  // Добавляем соль для новой попытки
                        }
                    }

                    throw new AppException("Failed to generate a unique hash after " + hashingConfig.getAllowedNumberOfCollisions() + " attempts");
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
        if (hash == null || hash.isBlank()) {
            log.warn("Received null or empty hash");
            return Optional.empty();
        }

        try {
            byte[] hashInBytes = Base64.getDecoder().decode(hash);
            return phoneHashRepository.findPhoneNumberByHash(hashInBytes);
        } catch (IllegalArgumentException e) {
            log.error("Invalid Base64 hash format: {}", hash, e);
            throw new AppException("Invalid Base64 hash format", e);
        }
    }

}


