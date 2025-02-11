package kettlebell.phonehashingservice;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
//@ConfigurationProperties(prefix = "hashing")
@Data  // Lombok-аннотация для геттеров, сеттеров и других методов
public class HashingConfig {
    @Value("${hashing.algorithm}")
    private String groupOfAlgorithms;
    @Value("${hashing.salt}")
    private String salt;
    @Value("${hashing.allowed-number-of-collisions}")
    private Integer allowedNumberOfCollisions;
}
