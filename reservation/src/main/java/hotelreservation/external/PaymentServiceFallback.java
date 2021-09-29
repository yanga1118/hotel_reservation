package hotelreservation.external;

import org.springframework.stereotype.Component;

import hotelreservation.external.Payment;

@Component
public class PaymentServiceFallback implements PaymentService {
 
    @Override
    public boolean pay(Payment payment) {
        // TODO Auto-generated method stub
        
        System.out.println("Circuit breaker has been opened. Thank you for your patience ");
        return false;
    }
    
  
}
