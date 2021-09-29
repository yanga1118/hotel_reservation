package hotelreservation;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String roomId;
    private String roomNo;
    private String roomStatus;
    private String roomSize;
    private String amenityInfo;
    private String reservStatus;
   
    private Date createRoomDate;
    private boolean payCompletedYn;
    private String userId;
    private Date reservDate;
    private String userName;
    private String peopleQty;
    private Date payDate;
    private Long amount;
    private String payMethod;
    private Date reservStartDate;
    private Date reservEndDate;
    
    private static final String RESERVATION_APPROVED = "Approved";
    private static final String RESERVATION_COMPLETED = "Completed";
    private static final String RESERVATION_CACELREQUEST = "CancelRequest";
    private static final String RESERVATION_CANCLED = "CancelCompleted";
    
    
    @PostPersist
    public void onPostPersist(){
      
        Logger logger = LoggerFactory.getLogger(this.getClass());
        
        //  Thread.sleep(5000);

         if(this.reservStatus.equals(RESERVATION_APPROVED) ){
          hotelreservation.external.Payment payment = new hotelreservation.external.Payment();

          payment.setUserId(this.userId);
          payment.setUserName(this.userName);
          payment.setRoomNo(this.roomNo);
          payment.setAmount(this.amount);
          payment.setPayMethod(this.payMethod);
          payment.setPayCompltedYn(this.payCompletedYn);
          payment.setPayStatus("PayReqeust");
          payment.setReservEndDate(this.reservStartDate);
          payment.setReservEndDate(this.reservEndDate);

          //RES/REQ 
          boolean result = ReservationApplication.applicationContext.getBean(hotelreservation.external.PaymentService.class).pay(payment);
          if(result){
             try {
                    Date nowDate = new Date();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd a HH:mm:ss"); 
                    String date = simpleDateFormat.format(nowDate);
                    
                    ReservationCompleted reservationCompleted = new ReservationCompleted();
                    this.setReservDate(simpleDateFormat.parse(date));
                    this.setPayDate(simpleDateFormat.parse(date));
                    this.setPayCompletedYn(true);
                    this.setRoomStatus("Charged");
       
                    BeanUtils.copyProperties(this, reservationCompleted);
                    reservationCompleted.publishAfterCommit();

                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }else if(this.reservStatus.equals(RESERVATION_CACELREQUEST) ){
            ReservationCancelRequested reservationCancelRequested = new ReservationCancelRequested();
            BeanUtils.copyProperties(this, reservationCancelRequested);
            reservationCancelRequested.publishAfterCommit();

        }else if(this.reservStatus.equals(RESERVATION_CANCLED)){
            ReservationCanceled reservationCanceled = new ReservationCanceled();
            BeanUtils.copyProperties(this, reservationCanceled);
            System.out.println("YYYYYYYYYYYYYYYYYY");
            reservationCanceled.publishAfterCommit();
        }
    }



    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getRoomId() {
        return roomId;
    }
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    public String getRoomNo() {
        return roomNo;
    }
    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }
    public String getRoomStatus() {
        return roomStatus;
    }
    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }
    public String getRoomSize() {
        return roomSize;
    }
    public void setRoomSize(String roomSize) {
        this.roomSize = roomSize;
    }
    public String getAmenityInfo() {
        return amenityInfo;
    }
    public void setAmenityInfo(String amenityInfo) {
        this.amenityInfo = amenityInfo;
    }
    public String getReservStaus() {
        return reservStatus;
    }
    public void setReservStaus(String reservStaus) {
        this.reservStatus = reservStaus;
    }
    public String getReservStatus() {
    return reservStatus;
}



public void setReservStatus(String reservStatus) {
    this.reservStatus = reservStatus;
}

    public Date getCreateRoomDate() {
        return createRoomDate;
    }
    public void setCreateRoomDate(Date createRoomDate) {
        this.createRoomDate = createRoomDate;
    }
    public boolean isPayCompletedYn() {
        return payCompletedYn;
    }
    public void setPayCompletedYn(boolean payCompletedYn) {
        this.payCompletedYn = payCompletedYn;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public Date getReservDate() {
        return reservDate;
    }
    public void setReservDate(Date reservDate) {
        this.reservDate = reservDate;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getPeopleQty() {
        return peopleQty;
    }
    public void setPeopleQty(String peopleQty) {
        this.peopleQty = peopleQty;
    }
  
    public Date getPayDate() {
        return payDate;
    }
    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }
    public Date getReservStartDate() {
        return reservStartDate;
    }
    public void setReservStartDate(Date reservStartDate) {
        this.reservStartDate = reservStartDate;
    }
    public Date getReservEndDate() {
        return reservEndDate;
    }
    public void setReservEndDate(Date reservEndDate) {
        this.reservEndDate = reservEndDate;
    }
    public Long getAmount() {
        return amount;
    }
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    public String getPayMethod() {
        return payMethod;
    }
    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }
   
    
}