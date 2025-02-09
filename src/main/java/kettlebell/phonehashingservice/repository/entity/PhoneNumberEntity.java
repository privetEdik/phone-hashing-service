package kettlebell.phonehashingservice.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false, unique = true, length = 12)
    private String number;

    @Column(name = "hash_value", nullable = false, unique = true)
    private byte[] hashValue;

    public PhoneNumberEntity(String number, byte[] hashValue) {
        this.number = number;
        this.hashValue = hashValue;
    }
}
