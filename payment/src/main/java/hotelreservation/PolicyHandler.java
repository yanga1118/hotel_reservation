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
    @Autowired PaymentRepository paymentRepository;
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCancelRequested_PayCancel(@Payload ReservationCancelRequested reservationCancelRequested){

        if(!reservationCancelRequested.validate()) return;

        Payment payment = new Payment();

        payment.setRoomNo(reservationCancelRequested.getRoomNo());
        payment.setUserId(reservationCancelRequested.getUserId().toString());
        payment.setUserName(reservationCancelRequested.getUserName());
        payment.setAmount(reservationCancelRequested.getAmount());
        payment.setPayDate(reservationCancelRequested.getPayDate());
        payment.setPayMethod(reservationCancelRequested.getPayMethod());
        payment.setPayMethod(reservationCancelRequested.getPayMethod());
        payment.setPayStatus("PayCanceled");
        
        System.out.println("\n\n##### listener PayCancel : " + reservationCancelRequested.toJson() + "\n\n");

        paymentRepository.save(payment);
    }
 

}