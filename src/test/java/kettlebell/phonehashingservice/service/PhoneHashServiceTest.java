package kettlebell.phonehashingservice.service;

import kettlebell.phonehashingservice.HashingConfig;
import kettlebell.phonehashingservice.exception.AppException;
import kettlebell.phonehashingservice.repository.PhoneHashRepository;
import kettlebell.phonehashingservice.repository.entity.PhoneNumberEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PhoneHashServiceTest {

    @Autowired
    private PhoneHashRepository phoneHashRepository;
    private PhoneHashService phoneHashService;
    private Algorithm algorithm;
    private String salt;

    @BeforeEach
    void before() {

       String groupOfAlgorithms = "SHA1";
        salt = "salt_value";
        Integer attempt = 3;

        algorithm = Algorithm.fromName(groupOfAlgorithms);

        HashingConfig hashingConfig = new HashingConfig();
        hashingConfig.setSalt(salt);
        hashingConfig.setGroupOfAlgorithms(groupOfAlgorithms);
        hashingConfig.setAllowedNumberOfCollisions(attempt);

        phoneHashService = new PhoneHashService(phoneHashRepository, hashingConfig);
        phoneHashRepository.deleteAll();
    }

    @Test
    void getOrCreateHash_numberPhoneIsInBase() {

        MessageDigest digest;

        String phoneNumber = "382000000000";
        String phoneNumberPlusSalt = phoneNumber + salt;

        try {
            digest = MessageDigest.getInstance(algorithm.getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] saveHashInBytes = digest.digest(phoneNumberPlusSalt.getBytes(StandardCharsets.UTF_8));

        // объекта нет не по хешу не по номеру
        assertTrue(phoneHashRepository.findPhoneNumberByHash(saveHashInBytes).isEmpty());
        assertTrue(phoneHashRepository.findHashInBytesByPhoneNumber(phoneNumber).isEmpty());

        phoneHashRepository.save(new PhoneNumberEntity(phoneNumber, saveHashInBytes));

        String hash = Base64.getEncoder().encodeToString(saveHashInBytes);

        assertEquals(hash, phoneHashService.getOrCreateHash(phoneNumber));

    }

    @Test //аномальное число коллизий не дали сохранить объект в базу
    void getOrCreateHash_Exception() {

        String phoneNumber = "381100000000";
        String phoneNumber1 = phoneNumber + salt;
        String phoneNumber2 = phoneNumber + salt + salt;
        String phoneNumber3 = phoneNumber + salt + salt + salt;

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algorithm.getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] saveHashInBytes1 = digest.digest(phoneNumber1.getBytes(StandardCharsets.UTF_8));
        byte[] saveHashInBytes2 = digest.digest(phoneNumber2.getBytes(StandardCharsets.UTF_8));
        byte[] saveHashInBytes3 = digest.digest(phoneNumber3.getBytes(StandardCharsets.UTF_8));

        phoneHashRepository.save(new PhoneNumberEntity("381110000000", saveHashInBytes1));
        phoneHashRepository.save(new PhoneNumberEntity("381120000000", saveHashInBytes2));
        phoneHashRepository.save(new PhoneNumberEntity("381130000000", saveHashInBytes3));

        assertThrows(AppException.class, () -> phoneHashService.getOrCreateHash(phoneNumber));

    }

    @Test
    void findPhoneNumberByHash_AppException() {
        assertThrows(AppException.class,() -> phoneHashService.findPhoneNumberByHash("d_+::;;```"));
    }
        @Test
    void findPhoneNumberByHash_Hash_is_not_In_Base() {
        assertTrue(phoneHashService.findPhoneNumberByHash("Z3Gu3BK9rFKs/Tuo44ojOW245O8=").isEmpty());
    }
        @Test
    void findPhoneNumberByHash_Hash_Is_In_Base() {

            MessageDigest digest;

            String phoneNumber = "382000000000";
            String phoneNumberPlusSalt = phoneNumber + salt;

            try {
                digest = MessageDigest.getInstance(algorithm.getAlgorithm());
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            byte[] saveHashInBytes = digest.digest(phoneNumberPlusSalt.getBytes(StandardCharsets.UTF_8));

            phoneHashRepository.save(new PhoneNumberEntity(phoneNumber, saveHashInBytes));

            String hash = Base64.getEncoder().encodeToString(saveHashInBytes);

            assertEquals(phoneNumber,phoneHashService.findPhoneNumberByHash(hash).orElse(""));

        }

}