package hotelreservation;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ReservationViewRepository extends CrudRepository<ReservationView, Long> {

//	List<ReservationView> findByRoomId(Long RoomId);

}