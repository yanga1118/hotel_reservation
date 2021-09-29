package hotelreservation;

import java.util.Date;

public class RoomRegistered extends AbstractEvent {

    private Long id;
    private String roomId;
    private String roomNo;
    private String viewInfo;
    private String status;
    private String roomSize;
    private Date createDate;
    private String amenityInfo;

    public RoomRegistered(){
        super();
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

    public String getViewInfo() {
        return viewInfo;
    }

    public void setViewInfo(String viewInfo) {
        this.viewInfo = viewInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRoomSize() {
        return roomSize;
    }

    public void setRoomSize(String roomSize) {
        this.roomSize = roomSize;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getAmenityInfo() {
        return amenityInfo;
    }

    public void setAmenityInfo(String amenityInfo) {
        this.amenityInfo = amenityInfo;
    }

   
}