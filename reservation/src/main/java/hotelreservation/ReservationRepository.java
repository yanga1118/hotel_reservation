package hotelreservation;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;

@RepositoryRestResource(collectionResourceRel="reservations", path="reservations")
public interface ReservationRepository extends CrudRepository<Reservation, Long>{

    List<Reservation> findByRoomId(String RoomId);
}
