package hotelreservation;

import java.util.Date;

public class PayCompleted extends AbstractEvent {

    private Long id;
    private Integer userId;
    private String userName;
    private String RoomNo;
    private String orderstatus;
    private String payStatus;
    private Date payDate;
    private Long amount;
    private String payMethod;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Integer getPhoneno() {
        return userId;
    }

    public void setPhoneno(Integer userId) {
        this.userId = userId;
    }
    public String getUsername() {
        return userName;
    }

    public void setUsername(String userName) {
        this.userName = userName;
    }
    public String getOrderid() {
        return RoomNo;
    }

    public void setOrderid(String RoomNo) {
        this.RoomNo = RoomNo;
    }
    public String getOrderstatus() {
        return orderstatus;
    }

    public void setOrderstatus(String orderstatus) {
        this.orderstatus = orderstatus;
    }
    public String getPaystatus() {
        return payStatus;
    }

    public void setPaystatus(String payStatus) {
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
    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }
}