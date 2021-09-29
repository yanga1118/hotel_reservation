package hotelreservation;
import java.util.Date;

public class ReservationCanceled extends AbstractEvent {

  
    private Long id;
    private String roomId;
    private String roomNo;
    private String roomStatus;
    private String roomSize;
    private String amenityInfo;
    private Date createDate;
    private Date reservStatus;
    private String payCompltedYn;
    private Long userId;
    private String userName;
    private String peopleQty;
    private Date reservDate;
    private String payStatus;
    private Date payDate;
    private Date reservStartDate;
    private Date reservEndDate;
    
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
    public Date getCreateDate() {
        return createDate;
    }
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
    public Date getReservStatus() {
        return reservStatus;
    }
    public void setReservStatus(Date reservStatus) {
        this.reservStatus = reservStatus;
    }
    public String getPayCompltedYn() {
        return payCompltedYn;
    }
    public void setPayCompltedYn(String payCompltedYn) {
        this.payCompltedYn = payCompltedYn;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
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
    public Date getReservDate() {
        return reservDate;
    }
    public void setReservDate(Date reservDate) {
        this.reservDate = reservDate;
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