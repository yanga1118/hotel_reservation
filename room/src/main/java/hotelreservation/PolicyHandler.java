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
    @Autowired RoomInfoRepository roomInfoRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCompleted_UpdateRoomStatus(@Payload ReservationCompleted reservationCompleted){

        if(!reservationCompleted.validate()) return;

        System.out.println("\n\n##### listener UpdateRoomStatus : " + reservationCompleted.toJson() + "\n\n");
        RoomInfo roominfo = new RoomInfo();

        roominfo.setRoomId(reservationCompleted.getRoomId());
        roominfo.setRoomNo(reservationCompleted.getRoomNo());
        roominfo.setStatus("CHARGED");
        roominfo.setReservStartDate(reservationCompleted.getReservStartDate());
        roominfo.setReservEndDate(reservationCompleted.getReservEndDate());
        
        roomInfoRepository.save(roominfo);
    }

     @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCanceled_UpdateRoomStatus(@Payload ReservationCanceled reservationCanceled){

        if(!reservationCanceled.validate()) return;
         System.out.println("\n\n##### listener UpdateRoomStatus : " + reservationCanceled.toJson() + "\n\n");
     
         RoomInfo roominfo = new RoomInfo();
 
         roominfo.setRoomId(reservationCanceled.getRoomId());
         roominfo.setRoomNo(reservationCanceled.getRoomNo());
         roominfo.setStatus("EMPTY");
         roominfo.setReservStartDate(reservationCanceled.getReservStartDate());
         roominfo.setReservEndDate(reservationCanceled.getReservEndDate());
         
         roomInfoRepository.save(roominfo);
    }


}