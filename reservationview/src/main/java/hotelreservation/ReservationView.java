package hotelreservation;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name="ReservationView_table")
public class ReservationView {

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
    private Long payAmount;
    private String payMethod;
    private Date reservStartDate;
    private Date reservEndDate;

    
    private static final String RESERVATION_APPROVED = "Approved";
    private static final String RESERVATION_COMPLETED = "Completed";
    
    
    @PostPersist
    public void onPostPersist(){
      
        Logger logger = LoggerFactory.getLogger(this.getClass());
        
        //  Thread.sleep(5000);

         if(this.reservStatus.equals(RESERVATION_APPROVED) ){

                 System.out.println("Reservation Completed");
                ReservationCompleted reservationCompleted = new ReservationCompleted();
                this.setReservSatus(RESERVATION_COMPLETED);
                BeanUtils.copyProperties(this, reservationCompleted);
                reservationCompleted.publishAfterCommit();

        }
        if(this.reservStatus.equals("CancelRequest")){
            ReservationCancelRequested reservationCancelRequested = new ReservationCancelRequested();
            BeanUtils.copyProperties(this, reservationCancelRequested);
            reservationCancelRequested.publishAfterCommit();

        }

       
    }
    @PostUpdate
    public void onPostUpdate(){
        
        ReservationCanceled reservationCanceled = new ReservationCanceled();
        BeanUtils.copyProperties(this, reservationCanceled);
        reservationCanceled.publishAfterCommit();

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
    public String getReservSatus() {
        return reservStatus;
    }
    public void setReservSatus(String reservStatus) {
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
    public Long getPayAmount() {
        return payAmount;
    }
    public void setPayAmount(Long payAmount) {
        this.payAmount = payAmount;
    }
    public String getPayMethod() {
        return payMethod;
    }
    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }
   
     
    
}