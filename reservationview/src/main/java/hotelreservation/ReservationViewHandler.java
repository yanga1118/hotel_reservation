package hotelreservation;

import hotelreservation.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationViewHandler {


    @Autowired
    private ReservationViewRepository reservationViewRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationCompleted_then_CREATE_1 (@Payload ReservationCompleted reservationCompleted) {
        try {

            if (!reservationCompleted.validate()) return;

            // view 객체 생성
            ReservationView reservationView = new ReservationView();
            // view 객체에 이벤트의 Value 를 set 함
            reservationView.setRoomId(reservationCompleted.getRoomId());
            reservationView.setRoomNo(reservationCompleted.getRoomNo());
            reservationView.setPayAmount(reservationCompleted.getAmount());
            reservationView.setReservDate(reservationCompleted.getReservDate());
            reservationView.setPayDate(reservationCompleted.getPayDate());
            reservationView.setReservStartDate(reservationCompleted.getReservStartDate());
            reservationView.setReservEndDate(reservationCompleted.getReservEndDate());
            reservationView.setPeopleQty(reservationCompleted.getPeopleQty());
            reservationView.setRoomSize(reservationCompleted.getRoomSize());
            reservationView.setPayCompletedYn(reservationCompleted.getPayCompletedYn());
            reservationView.setUserName(reservationCompleted.getUserName());
            reservationView.setAmenityInfo(reservationCompleted.getAmenityInfo());
            reservationView.setRoomStatus("charged");
            reservationView.setReservSatus("Completed");
            reservationCompleted.setPayStatus("payCompleted");
            reservationView.setPayMethod(reservationCompleted.getPayMethod());
            reservationView.setPeopleQty(reservationCompleted.getPeopleQty());
            reservationView.setPayMethod(reservationCompleted.getPayMethod());
            reservationView.setPayCompletedYn(reservationCompleted.getPayCompletedYn());
            reservationView.setUserId(reservationCompleted.getUserId());

            reservationViewRepository.save(reservationView);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationCancelRequested_then_CREATED_1 (@Payload PayCanceled payCanceled) {
        try {

            if (!payCanceled.validate()) return;

            // view 객체 생성
            ReservationView reservationView = new ReservationView();
            // view 객체에 이벤트의 Value 를 set 함
            reservationView.setRoomId(payCanceled.getRoomId());
            reservationView.setRoomNo(payCanceled.getRoomNo());
            reservationView.setPayAmount(payCanceled.getAmount());
            reservationView.setReservDate(payCanceled.getReservDate());
            reservationView.setPayDate(payCanceled.getPayDate());
            reservationView.setReservStartDate(payCanceled.getReservStartDate());
            reservationView.setReservEndDate(payCanceled.getReservEndDate());
            reservationView.setUserName(payCanceled.getUserName());
            reservationView.setPayCompletedYn(payCanceled.getPayCompletedYn());
            reservationView.setPayMethod(payCanceled.getPayMethod());

            reservationView.setPeopleQty("");
            reservationView.setRoomSize("");
            reservationView.setAmenityInfo("");
            reservationView.setRoomStatus("");
           
             // view 레파지 토리에 save
            reservationViewRepository.save(reservationView);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationCanceled_then_UPDATE_1 (@Payload ReservationCanceled reservationCanceled) {
        try {

            if (!reservationCanceled.validate()) return;

            // view 객체 생성
            ReservationView reservationView = new ReservationView();
            // view 객체에 이벤트의 Value 를 set 함
            // view 레파지 토리에 save
              reservationView.setRoomId(reservationCanceled.getRoomId());
              reservationView.setRoomNo(reservationCanceled.getRoomNo());
              reservationView.setUserId(reservationCanceled.getUserId());
              reservationView.setUserName(reservationCanceled.getUserName());
              reservationView.setPayAmount(reservationCanceled.getAmount());
              reservationView.setReservDate(reservationCanceled.getReservDate());
              reservationView.setPayDate(reservationCanceled.getPayDate());
              reservationView.setReservStartDate(reservationCanceled.getReservStartDate());
              reservationView.setReservEndDate(reservationCanceled.getReservEndDate());
              reservationView.setPeopleQty(reservationCanceled.getPeopleQty());
              reservationView.setPayCompletedYn(reservationCanceled.getPayCompletedYn());
              reservationView.setReservSatus(reservationCanceled.getReservSatus());
              reservationView.setPayMethod(reservationCanceled.getPayMethod());
              reservationView.setPeopleQty(reservationCanceled.getPeopleQty());
              reservationView.setPayMethod(reservationCanceled.getPayMethod());
              reservationView.setPayCompletedYn(reservationCanceled.getPayCompletedYn());
              
              reservationView.setAmenityInfo("");
              reservationView.setRoomStatus("");
              reservationView.setRoomSize("");
              
            reservationViewRepository.save(reservationView);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPayCanceled_then_CREATED_2 (@Payload PayCanceled reservationCompleted) {
        try {

            if (!reservationCompleted.validate()) return;

            // view 객체 생성
            ReservationView reservationView = new ReservationView();
            // view 객체에 이벤트의 Value 를 set 함
            // view 레파지 토리에 save
            reservationViewRepository.save(reservationView);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

