package kettlebell.phonehashingservice;

import kettlebell.phonehashingservice.repository.PhoneHashRepository;
import kettlebell.phonehashingservice.repository.entity.PhoneNumberEntity;
import kettlebell.phonehashingservice.service.PhoneHashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
//@RequiredArgsConstructor
@Slf4j
class PhoneHashingServiceApplicationTests {


    @Autowired
    private PhoneHashService  phoneHashService;

    @Test
    void contextLoads() {

        String number = phoneHashService.getOrCreateHash("380000000036");
        if (number != null) {
            log.error("===========================================");
            log.error("!!!!!!!number: {}; hash: ;!!!!!!!!", number);
            log.error("===========================================");
        } else {
            log.error("!!!!!!!!!!!!!!NULL!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

}
