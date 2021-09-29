package hotelreservation;

import java.util.Date;

public class PayCanceled extends AbstractEvent {

    private Long id;
    private String userId;
    private String userName;
    private String roomNo;
    private String roomId;
    private Long amount;
    private Date payDate;
    private String payMethod;
    private boolean payCompletedYn;
    private String payStatus;
    private Date reservDate;
    private Date reservStartDate;
    private Date reservEndDate;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getRoomNo() {
        return roomNo;
    }
    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }
    public String getRoomId() {
        return roomId;
    }
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    public Long getAmount() {
        return amount;
    }
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    public Date getPayDate() {
        return payDate;
    }
    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }
    public String getPayMethod() {
        return payMethod;
    }
    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }
    public Date getReservDate() {
        return reservDate;
    }
    public void setReservDate(Date reservDate) {
        this.reservDate = reservDate;
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
    public boolean getPayCompletedYn() {
        return payCompletedYn;
    }
    public void setPayCompletedYn(boolean payCompletedYn) {
        this.payCompletedYn = payCompletedYn;
    }
    public String getPayStatus() {
        return payStatus;
    }
    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }

    
}