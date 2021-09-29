package hotelreservation.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import java.util.Date;



@EnableCircuitBreaker
@FeignClient(name="payment", url = "${api.Payment.url}", fallback = PaymentServiceFallback.class)
public interface PaymentService {

    @RequestMapping(method=RequestMethod.POST, path="/payRequest")
    public boolean pay(@RequestBody Payment payment);

}

