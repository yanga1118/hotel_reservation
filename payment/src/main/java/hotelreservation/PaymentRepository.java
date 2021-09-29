package hotelreservation;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.repository.CrudRepository;

@RepositoryRestResource(collectionResourceRel="payments", path="payments")
public interface PaymentRepository extends CrudRepository<Payment, Long>{


}
