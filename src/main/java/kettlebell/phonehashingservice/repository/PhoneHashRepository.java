package kettlebell.phonehashingservice.repository;

import kettlebell.phonehashingservice.repository.entity.PhoneNumberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhoneHashRepository extends JpaRepository<PhoneNumberEntity, Long> {

    @Query("SELECT p.number FROM PhoneNumberEntity p WHERE p.hashValue = :hash")
    Optional<String> findPhoneNumberByHash(@Param("hash") byte[] hash);

    @Query("SELECT p.hashValue FROM PhoneNumberEntity p WHERE p.number = :phoneNumber")
    Optional<byte[]> findHashInBytesByPhoneNumber(@Param("phoneNumber") String phoneNumber);

}
