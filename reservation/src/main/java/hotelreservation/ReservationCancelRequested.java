package hotelreservation;

import java.util.Date;

public class ReservationCancelRequested extends AbstractEvent {

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
    private String payStatus;
    private Date payDate;
    private String payMethod; 
    private Date reservStartDate;
    private Date reservEndDate;
    private Long amount;
    
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
    public String getPayStatus() {
        return payStatus;
    }
    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }
    public Date getPayDate() {
        return payDate;
    }
    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }

    
    public Long getAmount() {
        return amount;
    }
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    public String getReservStatus() {
        return reservStatus;
    }
    public void setReservStatus(String reservStatus) {
        this.reservStatus = reservStatus;
    }
    public String getPayMethod() {
        return payMethod;
    }
    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
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
 
}