package hotelreservation;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.Date;

@Entity
@Table(name="Payment_table")
public class Payment {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String userId;
    private String userName;
    private String roomNo;
    private String payStatus;
    private boolean payCompletedYn;
    private Date payDate;
    private Long amount;
    private String payMethod;

    @PostPersist
    public void onPostPersist(){

      //결제 요청
      if(this.payStatus.equals("PayReqeust")){
        PayCompleted payCompleted = new PayCompleted();
        this.setPayCompletedYn(true);
        this.setPayStatus("PayCompleted");
        BeanUtils.copyProperties(this, payCompleted);
        payCompleted.publishAfterCommit();

      }else if(this.payStatus.equals("PayCanceled")){  //취소 요청
        PayCanceled payCanceled = new PayCanceled();
        this.setPayCompletedYn(false);
        this.setPayStatus("PayCanceled");
        System.out.println("AAAA");
        BeanUtils.copyProperties(this, payCanceled);
        payCanceled.publishAfterCommit();

      }else{
          System.out.println("Not Alloved Status");
      }
    }

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
   
    public String getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }

    
    public boolean getPayCompletedYn() {
        return payCompletedYn;
    }

    public void setPayCompletedYn(boolean payCompletedYn) {
        this.payCompletedYn = payCompletedYn;
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

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

 



}