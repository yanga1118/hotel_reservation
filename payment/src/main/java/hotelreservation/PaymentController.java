package hotelreservation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.Date;
import java.util.List;
import java.util.Map;

 @RestController
 public class PaymentController {
   
    @Autowired
    PaymentRepository paymentRepository;

    @PostMapping(value = "/payRequest")
     public boolean createpaymentInfo(@RequestBody Map<String, String> param) {

        boolean result = false;
        Payment payment = new Payment();

        payment.setUserId(param.get("userId"));
        payment.setUserName(param.get("userName"));
        payment.setRoomNo(param.get("roomNo"));
        payment.setAmount(Long.parseLong(param.get("amount"))); 
        payment.setPayStatus(param.get("payStatus"));
        
        System.out.println("-------------------------------");
        System.out.println(param.toString());
        System.out.println("-------------------------------");
        try {
            payment = paymentRepository.save(payment);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
     
 }