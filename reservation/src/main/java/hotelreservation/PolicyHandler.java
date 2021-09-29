package hotelreservation;

import hotelreservation.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired ReservationRepository reservationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRoomRegistered_InsertRoom(@Payload RoomRegistered roomRegistered){

        if(!roomRegistered.validate()) return;

        // roomInfo 객체 생성 //
        Reservation roomInfo = new Reservation();

        roomInfo.setRoomId(roomRegistered.getRoomId());
        roomInfo.setRoomNo(roomRegistered.getRoomNo());
        roomInfo.setCreateRoomDate(roomRegistered.getCreateDate());
        roomInfo.setRoomSize(roomRegistered.getRoomSize());
        roomInfo.setAmenityInfo(roomRegistered.getAmenityInfo());
        roomInfo.setRoomStatus("NEW");
        roomInfo.setReservStaus("");
        roomInfo.setPayCompletedYn(false);
        roomInfo.setUserId("");
        roomInfo.setReservDate(null);
        roomInfo.setUserName("");
        roomInfo.setPeopleQty("");
        roomInfo.setReservStartDate(null);
        roomInfo.setReservEndDate(null);
        roomInfo.setPayMethod("");
        roomInfo.setAmount(null);
        roomInfo.setPayDate(null);
    
        reservationRepository.save(roomInfo);
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCanceled_updateReservation(@Payload PayCanceled payCanceled){

        if(!payCanceled.validate()) return;

         Reservation reservation = new Reservation();
         reservation.setRoomId(payCanceled.getRoomId());
         reservation.setRoomNo(payCanceled.getRoomNo());
         reservation.setCreateRoomDate(null);
         reservation.setRoomSize("");
         reservation.setAmenityInfo("");
         reservation.setRoomStatus("");
         reservation.setReservStaus("CancelCompleted");
         reservation.setPayCompletedYn(payCanceled.getPayCompletedYn());
         reservation.setUserId(payCanceled.getUserId());
         reservation.setReservDate(payCanceled.getReservDate());
         reservation.setUserName(payCanceled.getUserName());
         reservation.setPeopleQty("");
         reservation.setReservStartDate(payCanceled.getReservStartDate());
         reservation.setReservEndDate(payCanceled.getReservEndDate());
         reservation.setPayMethod(payCanceled.getPayMethod());
         reservation.setAmount(payCanceled.getAmount());
         reservation.setPayDate(payCanceled.getPayDate());

         reservationRepository.save(reservation);
 
    }

}