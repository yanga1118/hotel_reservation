# 개인과제 - 호텔예약시스템

![12번가](https://user-images.githubusercontent.com/88864433/133467597-709524b1-4613-4dab-bc57-948f433ad565.png)
---------------------------------

# Table of contents

- [개인과제 - 호텔예약시스템](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현)
    - [DDD 의 적용](#DDD의-적용)
    - [동기식 호출과 Fallback 처리](#동기식-호출과-Fallback-처리) 
    - [비동기식 호출과 Eventual Consistency](#비동기식-호출과-Eventual-Consistency) 
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [API 게이트웨이](#API-게이트웨이)
  - [운영](#운영)
    - [Deploy/Pipeline](#deploypipeline)
    - [동기식 호출 / Circuit Breaker / 장애격리](#동기식-호출-circuit-breaker-장애격리)
    - [Autoscale (HPA)](#autoscale-(hpa))
    - [Zero-downtime deploy (Readiness Probe)](#zerodowntime-deploy-(readiness-Probe))
    - [Self-healing (Liveness Probe)](#self-healing-(liveness-probe))
    - [운영유연성](#운영유연성)



# 서비스 시나리오
	
[서비스 아키텍쳐]
객실 관리, 결제, 마케팅팀

[서비스 시나리오]

가. 기능적 요구사항

1. 호텔의 객실 관리팀은 객실 정보를 등록한다.( pub/sub )

2. 고객은 결제가 완료되어야 예약이 완료된다. ( Req/Res )

3. 예약이 완료되면 호텔에 방이 charge 상태로 변경된다. ( pub/sub )

4. 고객의 객실 예약 상태를 언제든지 조회할 수 있다. (CQRS)

5. 고객은 객실 예약을 취소 할 수 있다.( pub/sub )

6. 고객이 주문을 취소한다.( pub/sub )

7. 주문 취소를 하면 결제가 취소 된다. ( pub/sub )

8. 결제가 취소되어야 최종적으로 예약 취소가 완료된다. ( pub/sub )



나. 비기능적 요구사항

1. [설계/구현]Req/Resp : 객실 예약시 결제가 완료되어야 예약이 완료된다.

2. [설계/구현]CQRS : 고객의 객실 예약 상태를 언제든지 조회할 수 있다.

3. [설계/구현]Correlation : 객실 예약 취소 요청 -> 결제 취소 -> 객실 예약 취소 완료로 예약 상태가 변경된다.

4. [설계/구현]saga : 서비스(객실 관리, 결제, 예약)는 단일 서비스 내의 데이터를 처리하고, 각자의 이벤트를 발행하면 연관된 서비스에서 이벤트에 반응하여 각자의 데이터를 변경시킨다.

5. [설계/구현/운영]circuit breaker : 결제 요청 건수가 임계치 이상 발생할 경우 Circuit Breaker 가 발동된다. 

다. 기타 

1. [설계/구현/운영]polyglot : yo--상품팀과 주문팀은 서로 다른 DB를 사용하여 polyglot을 충족시킨다.


# 체크포인트

+ 분석 설계

	- 이벤트스토밍:
		- 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
		- 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
		- 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
		- 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?
	- 서브 도메인, 바운디드 컨텍스트 분리
		- 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
			- 적어도 3개 이상 서비스 분리
		- 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
		- 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
	- 컨텍스트 매핑 / 이벤트 드리븐 아키텍처
		- 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
		- Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
		- 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
		- 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
		- 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?
	- 헥사고날 아키텍처
		- 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?

- 구현

	- [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
		-Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
		- [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
		- 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
	- Request-Response 방식의 서비스 중심 아키텍처 구현
		- 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
		- 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
	- 이벤트 드리븐 아키텍처의 구현
		- 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
		- Correlation-key: 각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
		- Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
		- Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
		- CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

	- 폴리글랏 플로그래밍

		- 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
		- 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?

	- API 게이트웨이

		- API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
		- 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?

- 운영
	- SLA 준수
		- 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
		- 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
		- 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
		- 모니터링, 앨럿팅:

	- 무정지 운영 CI/CD (10)
		- Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명
		- Contract Test : 자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?
---

# 분석/설계

## Event Stoming 결과

- MSAEz로 모델링한 이벤트스토밍 결과
https://www.msaez.io/#/storming/7znb05057kPWQo1TAWCkGM0O2LJ3/5843d1078a788a01aa837bc508a68029


### 이벤트 도출

![DDD_1](https://user-images.githubusercontent.com/43808557/135551164-7ea3caee-7d59-4388-93eb-5761c1acae25.PNG)

```
1차적으로 필요하다고 생각되는 이벤트를 도출
``` 

### 부적격 이벤트 탈락

![DDD2](https://user-images.githubusercontent.com/43808557/135551208-6fb37744-ddd2-4034-93c6-8dc9a3d02eba.PNG)

```
- 과정 중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
- 객실 가능 예약 조회는 예약 서비스 내에서 수행하는 서비스로 이벤트에서 제외함

```

![DDD_3](https://user-images.githubusercontent.com/43808557/135551295-b81c8043-4157-41ca-b1ee-68be73282533.PNG)

### 액터, 커맨드를 부착하여 읽기 좋게 

![DDD4](https://user-images.githubusercontent.com/43808557/135551425-466d4289-e796-4dd8-bc2c-35831a771af8.PNG)
 
### 어그리게잇으로 묶기

![5-3](https://user-images.githubusercontent.com/88864433/133556981-a8bfb142-2690-442d-bc92-8d89a3307472.PNG)
 
``` 
- 고객의 예약 관리 결제 관리, 호텔의 객실관리는 command와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 묶어줌
```

### 바운디드 컨텍스트로 묶기

![6-3](https://user-images.githubusercontent.com/88864433/133557010-ac6b1c40-82b3-4445-8182-0feb50e4dbfb.PNG)
 
```
- 도메인 서열 분리 
    - Core Domain:  order, delivery : 없어서는 안될 핵심 서비스이며, 연간 Up-time SLA 수준을 99.999% 목표, 배포주기는 order의 경우 1주일 1회 미만, delivery의 경우 1개월 1회 미만
    - Supporting Domain:  marketing : 경쟁력을 내기위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 각 팀의 자율이나 표준 스프린트 주기가 1주일 이므로 1주일 1회 이상을 기준으로 함
```
### 폴리시 부착


![7-3](https://user-images.githubusercontent.com/88864433/133557035-7d121b68-59ee-4816-98bf-35f7fc2bb160.PNG)
 

### 폴리시의 이동과 컨텍스트 맵핑 (점선은 Pub/Sub, 실선은 Req/Resp) 

![8-3](https://user-images.githubusercontent.com/88864433/133557055-ab304be0-37a2-4675-bce0-425281df7301.PNG)
 

### 완성된 모형

![모델](https://user-images.githubusercontent.com/88864433/133361343-d99b4182-22ac-4881-aeee-19ae121723b5.PNG)
 
### 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

![주문완료검증](https://user-images.githubusercontent.com/88864433/133361542-bc0225f1-d540-42d8-ab1b-f9de9967e84a.PNG)

```
- 호텔의 객실 관리팀은 객실 정보를 등록한다. (ok)
- 고객은 결제가 완료하여야, 예약이 완료된다. (ok)
- 마케팅팀에서 쿠폰을 발행한다 (ok) 
- 쿠폰이 발행된 것을 확인하고 배송을 시작한다 (ok)
```
![주문취소검증](https://user-images.githubusercontent.com/88864433/133361562-11bef187-a52e-4948-a429-995d76d4424d.PNG)

``` 
- 고객이 주문을 취소할 수 있다 (ok)
- 주문을 취소하면 결제도 함께 취소된다 (ok)
- 주문이 취소되면 배송팀에 전달된다 (ok)
- 마케팅팀에서 쿠폰발행을 취소한다 (ok)
- 쿠폰발행이 취소되면 배송팀에서 배송을 취소한다 (ok)
```

### 비기능 요구사항에 대한 검증 (5개가 맞는지 검토 필요)

![비기능적 요구사항2](https://user-images.githubusercontent.com/88864433/133557381-ccd4b060-9193-4c38-a8a2-6cd8f846545a.PNG)

```
1. [설계/구현]Req/Resp : 쿠폰이 발행된 건에 한하여 배송을 시작한다. 
2. [설계/구현]CQRS : 고객이 주문상태를 확인 가능해야한다.
3. [설계/구현]Correlation : 주문을 취소하면 -> 쿠폰을 취소하고 -> 배달을 취소 후 주문 상태 변경
4. [설계/구현]saga : 서비스(상품팀, 상품배송팀, 마케팅팀)는 단일 서비스 내의 데이터를 처리하고, 각자의 이벤트를 발행하면 연관된 서비스에서 이벤트에 반응하여 각자의 데이터를 변경시킨다.
5. [설계/구현/운영]circuit breaker : 배송 요청 건수가 임계치 이상 발생할 경우 Circuit Breaker 가 발동된다. 
``` 

### 헥사고날 아키텍처 다이어그램 도출 (그림 수정필요없는지 확인 필요)

![분산스트림2](https://user-images.githubusercontent.com/88864433/133557657-451e67e9-400a-477c-af09-2bfd56f9a659.PNG)
 

```
- Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
- 호출관계에서 PubSub 과 Req/Resp 를 구분함
- 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐
```

# 구현

- 분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 바운더리 컨텍스트 별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

```
cd room
mvn spring-boot:run

cd reservation 
mvn spring-boot:run

cd payment
mvn spring-boot:run 
```

# DDD의 적용
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가? 

각 서비스 내에 도출된 핵심 Aggregate Root 객체를 Entity로 선언하였다. (객실관리(room), 예약(reservation), 결제(paymnet)) 

예약 Entity (Reservation.java) 
```

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

			....생략 
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



....생략 

```

Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 하였고 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다

ReservationRepository.java

```

@RepositoryRestResource(collectionResourceRel="reservations", path="reservations")
public interface ReservationRepository extends CrudRepository<Reservation, Long>{

    List<Reservation> findByRoomId(String RoomId);
}

```

결제 Payment.java

```
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
        ...생략

      }else{
          System.out.println("Not Alloved Status");
      }
    }
```
```
@Entity
@Table(name="StockDelivery_table")
public class StockDelivery {

     //Distance 삭제 및 Id auto로 변경
    
    private Long orderId;
    private String orderStatus;
    private String userName;
    private String address;
    private String productId;
    private Integer qty;
    private String storeName;
    private Date orderDate;
    private Date confirmDate;
    private String productName;
    private String phoneNo;
    private Long productPrice;
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String customerId;
    private String deliveryStatus;
    private Date deliveryDate;
    private String userId;
    
    private static final String DELIVERY_STARTED = "delivery Started";
    private static final String DELIVERY_CANCELED = "delivery Canceled";
... 생략 
```

마케팅의 promote.java 

``` 
@Entity
@Table(name="Promote_table")
public class Promote {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String phoneNo;
    private String username;
    private Long orderId;
    private String orderStatus;
    private String productId;
    private String payStatus;
    private String couponId;
    private String couponKind;
    private String couponUseYn;
    private String userId;

    @PostPersist
    public void onPostPersist(){
        CouponPublished couponPublished = new CouponPublished();
        BeanUtils.copyProperties(this, couponPublished);
        couponPublished.publishAfterCommit();

    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPhoneNo() {
		return phoneNo;
	}
.... 생략 

```

PromoteRepository.java

```
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
public interface PromoteRepository extends PagingAndSortingRepository<Promote, Long>{

	List<Promote> findByOrderId(Long orderId);

}
```

- 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
가능한 현업에서 사용하는 언어를 모델링 및 구현시 그대로 사용하려고 노력하였다. 

- 적용 후 Rest API의 테스트
주문 결제 후 productdelivery 주문 접수하기 POST

```
[시나리오 1]
http POST http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/orders address=“Seoul” productId=“1001" payStatus=“Y” phoneNo=“01011110000" productName=“Mac” productPrice=3000000 qty=1 userId=“goodman” username=“John”
http POST http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/orders address=“England” productId=“2001” payStatus=“Y” phoneNo=“0102220000” productName=“gram” productPrice=9000000 qty=1 userId=“gentleman” username=“John”
http POST http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/orders address=“USA” productId=“3001" payStatus=“Y” phoneNo=“01030000" productName=“Mac” productPrice=3000000 qty=1 userId=“goodman” username=“John”
http POST http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/orders address=“USA” productId=“3001” payStatus=“Y” phoneNo=“01030000” productName=“Mac” productPrice=3000000 qty=1 userId=“last test” username=“last test”
[시나리오 2]
http PATCH http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/orders/1 orderStatus=“Order Canceled”
http PATCH http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/orders/3 orderStatus=“Order Canceled”
http PATCH http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/orders/5 orderStatus=“Order Canceled”
[체크]
http GET http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/orders
http GET http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/orderStatus
http GET http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/stockDeliveries
http GET http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/promotes
```


# 동기식 호출과 Fallback 처리

(Request-Response 방식의 서비스 중심 아키텍처 구현)

- 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)

요구사항대로 배송팀에서는 쿠폰이 발행된 것을 확인한 후에 배송을 시작한다.

StockDelivery.java Entity Class에 @PostPersist로 쿠폰 발행 후에 배송을 시작하도록 처리하였다.

```
    @PostPersist
    public void onPostPersist() throws Exception{

    	Promote promote = new Promote();
        promote.setPhoneNo(this.phoneNo); 
        promote.setUserId(this.userId); 
        promote.setUsername(this.userName); 
        promote.setOrderId(this.orderId); 
        promote.setOrderStatus(this.orderStatus); 
        promote.setProductId(this.productId); 
        System.out.println("\n\npostpersist() : "+this.deliveryStatus +"\n\n");
        // deliveryStatus 따라 로직 분기
        if(DELIVERY_STARTED == this.deliveryStatus){
        	
	        boolean result = (boolean) ProductdeliveryApplication.applicationContext.getBean(food.delivery.work.external.PromoteService.class).publishCoupon(promote);
	
	        if(result){
	        	System.out.println("----------------");
	            System.out.println("Coupon Published");
	            System.out.println("----------------");
		       	DeliveryStarted deliveryStarted = new DeliveryStarted();
		        BeanUtils.copyProperties(this, deliveryStarted);
		        deliveryStarted.publishAfterCommit();
	        }else {
	        	throw new RollbackException("Failed during coupon publish");
	        }
        
        }
  
    }
    
```

##### 동기식 호출은 PromoteService 클래스를 두어 FeignClient 를 이용하여 호출하도록 하였다.

- PromoteService.java

```
  
package food.delivery.work.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import food.delivery.work.Promote;

import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="marketing", url = "${api.promote.url}", fallback = PromoteServiceFallback.class)
public interface PromoteService {
  
    @RequestMapping(method=RequestMethod.POST, path="/createPromoteInfo")
    public boolean publishCoupon(@RequestBody Promote promote);
    
    @RequestMapping(method=RequestMethod.POST, path="/cancelCoupon")
    public boolean cancelCoupon(@RequestBody Promote promote);
}
```

- PromoteServiceFallback.java

```
  
package food.delivery.work.external;

import org.springframework.stereotype.Component;

import food.delivery.work.Promote;

@Component
public class PromoteServiceFallback implements PromoteService {
    @Override
    public boolean publishCoupon(Promote promote) {
        //do nothing if you want to forgive it

        System.out.println("Circuit breaker has been opened. Fallback returned instead.");
        return false;
    }
    
    @Override
    public boolean cancelCoupon(Promote promote) {
        //do nothing if you want to forgive it

        System.out.println("Circuit breaker has been opened. Fallback returned instead.");
        return false;
    }
}
```


# 비동기식 호출과 Eventual Consistency (작성완료)

(이벤트 드리븐 아키텍처)

- 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?

주문/주문취소 후에 이를 배송팀에 알려주는 트랜잭션은 Pub/Sub 관계로 구현하였다.
아래는 주문/주문취소 이벤트를 통해 kafka를 통해 배송팀 서비스에 연계받는 코드 내용이다. 

```

    @PostPersist
    public void onPostPersist(){
    	
         Logger logger = LoggerFactory.getLogger(this.getClass());

    	
        OrderPlaced orderPlaced = new OrderPlaced();
        BeanUtils.copyProperties(this, orderPlaced);
        orderPlaced.publishAfterCommit();
        System.out.println("\n\n##### OrderService : onPostPersist()" + "\n\n");
        System.out.println("\n\n##### orderplace : "+orderPlaced.toJson() + "\n\n");
        System.out.println("\n\n##### productid : "+this.productId + "\n\n");
        logger.debug("OrderService");
    }

    @PostUpdate
    public void onPostUpdate() {
    	
    	OrderCanceled orderCanceled = new OrderCanceled();
        BeanUtils.copyProperties(this, orderCanceled);
        orderCanceled.publishAfterCommit();
    }
```
- 배송팀에서는 주문/주문취소 접수 이벤트에 대해 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler를 구현한다. 

```
Service
public class PolicyHandler{
    @Autowired StockDeliveryRepository stockDeliveryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderPlaced_AcceptOrder(@Payload OrderPlaced orderPlaced){

        if(!orderPlaced.validate()) return;

        // delivery 객체 생성 //
         StockDelivery delivery = new StockDelivery();

         delivery.setOrderId(orderPlaced.getId());
         delivery.setUserId(orderPlaced.getUserId());
         delivery.setOrderDate(orderPlaced.getOrderDate());
         delivery.setPhoneNo(orderPlaced.getPhoneNo());
         delivery.setProductId(orderPlaced.getProductId());
         delivery.setQty(orderPlaced.getQty()); 
         delivery.setDeliveryStatus("delivery Started");

         System.out.println("==================================");
         System.out.println(orderPlaced.getId());
         System.out.println(orderPlaced.toJson());
         System.out.println("==================================");
         System.out.println(delivery.getOrderId());

         stockDeliveryRepository.save(delivery);

    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCanceled_CancleOrder(@Payload OrderCanceled orderCanceled) {
    	
    	if(!orderCanceled.validate()) return;
... 중략
        for (StockDelivery delivery:deliveryList)
        {
        	System.out.println("\n\n"+orderCanceled.getId());
            delivery.setDeliveryStatus("delivery Canceled");
            stockDeliveryRepository.save(delivery);
        }
     
    }

}
```


# SAGA 패턴
- 취소에 따른 보상 트랜잭션을 설계하였는가?(Saga Pattern)

상품배송팀의 기능을 수행할 수 없더라도 주문은 항상 받을 수 있게끔 설계하였다. 
다만 데이터의 원자성을 보장해주지 않기 때문에 추후 order service 에서 재고 정보를 확인한 이후에 주문수락을 진행하거나, 상품배송 서비스에서 데이터 변경전 재고 여부를 확인하여 롤백 이벤트를 보내는 로직이 필요할 것으로 판단된다. 


order 서비스가  고객으로 주문 및 결제(order and pay) 요청을 받고
[order 서비스]
Order aggegate의 값들을 추가한 이후 주문완료됨(OrderPlaced) 이벤트를 발행한다. - 첫번째 

![saga1](https://user-images.githubusercontent.com/88864433/133546289-8b2cf493-7296-4464-944a-1c112f77b500.PNG)

서비스의 트랜젝션 완료

[product delivery 서비스]

![saga2](https://user-images.githubusercontent.com/88864433/133546388-3d5da7c0-8609-4a5b-8143-270b761a7a54.PNG)

주문완료됨(OrderPlaced) 이벤트가 발행되면 상품배송 서비스에서 해당 이벤트를 확인한다.
재고배송(stockdelivery) 정보를 추가 한다. - 두번째 서비스의 트렌젝션 완료

![saga3](https://user-images.githubusercontent.com/88864433/133546519-f224c831-4a34-4360-bd79-23a5f077949e.PNG)



# CQRS
- CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

예약/결제의 요청 상태가 변경될 때마다 고객이 현재 예약 상태를 확인하고, 조회할 수 있도록 예약 서비스  내에 Reservation View를 모델링하였다

ReservationView.java 
```

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
    
    
.... 생략 
```

ReservationViewHandler 를 통해 Pub/Sub 기반으로 다른 Aggreate와 분리하여
CQRS가 가능하도록 구현하였다.
```

    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationCompleted_then_CREATE_1 (@Payload ReservationCompleted reservationCompleted) {
        try {

            if (!reservationCompleted.validate()) return;

            // view 객체 생성
            ReservationView reservationView = new ReservationView();
            // view 객체에 이벤트의 Value 를 set 함
            reservationView.setRoomId(reservationCompleted.getRoomId());
            reservationView.setRoomNo(reservationCompleted.getRoomNo());
            reservationView.setPayAmount(reservationCompleted.getAmount());
            reservationView.setReservDate(reservationCompleted.getReservDate());
            reservationView.setPayDate(reservationCompleted.getPayDate());
            ... 생략
            reservationView.setPayMethod(reservationCompleted.getPayMethod());
            reservationView.setPeopleQty(reservationCompleted.getPeopleQty());
            reservationView.setPayMethod(reservationCompleted.getPayMethod());
            reservationView.setPayCompletedYn(reservationCompleted.getPayCompletedYn());
            reservationView.setUserId(reservationCompleted.getUserId());

            reservationViewRepository.save(reservationView);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

```
객실 예약시 결제 및 예약 정보에 대해서 Pub/Sub 기반으로 reservationView에 데이터가 생성되며,예약 완료, 결제 취소 요청, 예약 취소 완료를구현하였다.

결제 및 예약이 완료되면 해당 정보를 확인할 수 있다.


- CQRS 테스트 

![CQRS](https://user-images.githubusercontent.com/88864433/133558737-0d82429e-add2-403b-9750-c1a723beeb86.PNG)




# 폴리글랏 퍼시스턴스
- pom.xml
```
		<dependency>
        	<groupId>mysql</groupId>
        	<artifactId>mysql-connector-java</artifactId>
        	<scope>provided</scope>
    	</dependency>

		<dependency>
		    <groupId>org.javassist</groupId>
    		<artifactId>javassist</artifactId>
    		<version>3.25.0-GA</version>
		</dependency>
```

application.yml
```

spring:
  profiles: docker

  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://cloud12st.ck7n6wloicx4.ap-northeast-2.rds.amazonaws.com:3306/cloud12st
    username: root
    password: cloud#1234

  jpa:
    open-in-view: false
    show-sql: true
    hibernate:
      format_sql: true
      ddl-auto: create
```

- 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?

H2 DB의 경우 휘발성 데이터의 단점이 있는데, productdelivery 서비스의 경우 타 서비스들의 비해 중요하다고 생각하였다.
productdelivery는 주문과 쿠폰발행/취소를 중간에서 모두 파악하여 처리해야 되기 때문에 백업,복원기능과 안정성이 장점이 있는 mysql을 선택하여 구현하였다.


# API 게이트웨이
- API GW를 통하여 마이크로 서비스들의 진입점을 통일할 수 있는가?

- application.yml
```

spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: reservation
          uri: http://reservation:8080
          predicates:
            - Path=/reservations/**
        - id: room
          uri: http://room:8080
          predicates:
            - Path=/roomInfos/** 
        - id: payment
          uri: http://payment:8080
          predicates:
            - Path=/payments/** 
        - id: reservationview
          uri: http://reservationview:8080
          predicates:
            - Path=/reservationviews/**     
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

server:
  port: 8080
```

Gateway의 application.yml이며, 마이크로서비스들의 진입점을 세팅하여 URI Path에 따라서 각 마이크로서비스로 라우팅되도록 설정되었다.

# 운영
--
# Deploy/Pipeline

- (CI/CD 설정) BuildSpec.yml 사용 각 MSA 구현물은 git의 source repository 에 구성되었고, AWS의 CodeBuild를 활용하여 무정지 CI/CD를 설정하였다.

- Repository 화면 캡쳐 

![CICD](https://user-images.githubusercontent.com/88864433/133468925-a9ba1fec-8331-4a68-a0b7-2b570e4182de.PNG)

- CodeBuild 설정

![Code_Build_세부정보](https://user-images.githubusercontent.com/43808557/135446598-8518551c-316a-449a-b663-c4056b84b432.png)


![IAM_정책추가(Codebuild)](https://user-images.githubusercontent.com/43808557/135446640-d9167c2c-8bf8-44d8-a623-774e3dfe65ad.png)


- 빌드 환경 설정 

환경변수 Setting

![Code_build 환경변수](https://user-images.githubusercontent.com/43808557/135446798-068aed60-d21b-48ea-95cb-905579e05b40.png)

- buildspec.yml

```
ersion: 0.2

env:
  variables:
    IMAGE_REPO_NAME: "payment"
    CODEBUILD_RESOLVED_SOURCE_VERSION: "latest"

phases:
  install:
    runtime-versions:
      java: corretto11
      docker: 18
  pre_build:
    commands:
      - echo Logging in to Amazon ECR...
      - echo $IMAGE_REPO_NAME
      - echo $AWS_ACCOUNT_IDecho $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$IMAGE_REPO_NAME:$CODEBUILD_RESOLVED_SOURCE_VERSION
      - 
      - echo $AWS_DEFAULT_REGION
      - echo $CODEBUILD_RESOLVED_SOURCE_VERSION
      - echo start command
      - $(aws ecr get-login --no-include-email --region $AWS_DEFAULT_REGION)
  build:
    commands:
      - echo Build started on `date`
      - echo Building the Docker image...
      - mvn package -Dmaven.test.skip=true
      - echo $pwd
      - docker build -t $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$IMAGE_REPO_NAME:$CODEBUILD_RESOLVED_SOURCE_VERSION  .
  post_build:
    commands:
      - echo Build completed on `date`
      - echo Pushing the Docker image...
      - echo $pwd
      - docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$IMAGE_REPO_NAME:$CODEBUILD_RESOLVED_SOURCE_VERSION

cache:
  paths:
    - '/root/.m2/**/*'
```

![code_build_결과](https://user-images.githubusercontent.com/43808557/135447122-8efc0956-35d1-46d8-83b7-07ded5a1bc73.png)


# 동기식 호출 / Circuit Breaker / 장애격리
예약이 요청이 많아 결제 쪽에 지연이 많이 될 겨우  요청이 과도할 경우 Circuit Breaker를 통해 장애 격리를 진행하고자 한다.

- 부하테스터 siege툴을 통한 Circuit Breaker 동작 확인 : 
- 동시사용자 100명
- 30초간 실시
- 결제 서비스의 req/res 호출 후 저장 (@prepost) 전에 Thread sleep 을 진행한다.

```
siege -c100 -t30S -r10 -v --content-type "application/json" 'http://reservation:8080/reservations POST {"roomId": "101",  "roomNo": "101", "reservStatus" : "Approved", "roomStatus":"Empty", "createRoomDate":"2019-01-01" , "payCompletedYn":"false", "userId":"0001",    "reservDate" : "2021-09-30" , "userName" : "YANG" ,"peopleQty" : "3", "payDate": "2021-09-30", "amount": "200000", "payMethod":"card" , "reservStartDate":"2021-11-01" ,"reservEndDate": "2021-11-03"}'
```

```
@EnableCircuitBreaker
@FeignClient(name="payment", url = "${api.Payment.url}", fallback = PaymentServiceFallback.class)
public interface PaymentService {
    @RequestMapping(method=RequestMethod.POST, path="/payRequest")
    public boolean pay(@RequestBody Payment payment);
}

```

```
@Component
public class PaymentServiceFallback implements PaymentService {
 
    @Override
    public boolean pay(Payment payment) {
        // TODO Auto-generated method stub
        System.out.println("***************Circuit Breaker***********************************");
        System.out.println("Circuit breaker has been opened. Thank you for your patience ");
        System.out.println("***************Circuit Breaker***********************************");

        return false;
    }

}
```

![circuit_breaker 결과](https://user-images.githubusercontent.com/43808557/135442840-89e7a46d-aa5f-4afa-92cb-397ef185e944.png)



# Autoscale(HPA)
앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다.

![hpa1](https://user-images.githubusercontent.com/88864433/133547537-2a3d5954-305b-443e-9f06-ecd0913fdc1a.PNG)

평소에 order pod이 정상적으로 존재하던 중에

![hpa2](https://user-images.githubusercontent.com/88864433/133547635-04bbab9e-8373-4e40-94b2-6b23cadab2bb.PNG)

Autoscale 설정 명령어 실행

![hpa3](https://user-images.githubusercontent.com/88864433/133547683-607efd3d-b1a4-47fc-b3a2-3c19700de609.PNG)

Autoscale 설정됨을 확인

![hpa4](https://user-images.githubusercontent.com/88864433/133547727-9e4fb0bd-cbc9-45d5-ab08-606088272f7c.PNG)

siege 명령어를 수행 

![hpa5](https://user-images.githubusercontent.com/88864433/133547764-705a846d-c211-44b5-ae1f-bbb683fce886.PNG)

CPU 사용량이 5% 이상인 경우 POD는 최대 10개까지 늘어나는 것을 확인

![hpa6](https://user-images.githubusercontent.com/88864433/133547800-ea2c92cc-7733-4605-b58f-bc408a5c635b.PNG)

siege 가용성은 100%을 유지하고 있다.


# Zero-downtime deploy (Readiness Probe) 
(무정지 배포) 

서비스의 무정지 배포를 위하여 오더(Order) 서비스의 배포 yaml 파일에 readinessProbe 옵션을 추가하였다.

![HPA8](https://user-images.githubusercontent.com/88864433/133559651-9169b961-c0f8-47db-b8df-8b3c274bbd91.PNG)

![readness1](https://user-images.githubusercontent.com/88864433/133539552-06cc7425-1cb5-4319-b92b-c7c20d807c69.PNG)

파일의 버전이 v1을 적용하고 siege를 실행한 상태에서 v2로 배포를 진행하였다. 

![readness2](https://user-images.githubusercontent.com/88864433/133539593-37ea6cf1-ce76-4d5e-bf21-b6f3ec85079c.PNG)

서비스의 끊김없이 무정지 배포가 실행됨을 확인하였다. 


# Self-healing (Liveness Probe)

- port 및 정보를 잘못된 값으로 변경하여 yml 적용

![liveness1](https://user-images.githubusercontent.com/88864433/133550800-5c481182-5e46-4572-b5c8-738fe5356653.PNG)

- 해당 yml을 배포

![liveness2](https://user-images.githubusercontent.com/88864433/133550866-21e9ca23-9d2c-41a0-bc60-0f6a7596279f.PNG)

- 잘못된 경로와 포트여서 kubelet이 자동으로 컨테이너를 재시작하였다. 

![LIVENESS4](https://user-images.githubusercontent.com/88864433/133563189-377ef1fe-7e86-4ea6-b387-87739edcdf61.PNG)

- POD가 재시작되었다. 

![liveness3](https://user-images.githubusercontent.com/88864433/133550970-0f13cf46-7b96-4034-aeaa-c24750597973.PNG)



# 운영유연성
- 데이터 저장소를 분리하기 위한 Persistence Volume과 Persistence Volume Claim을 적절히 사용하였는가?

- kubectl apply -f efs-provisioner-deploy.yml
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: efs-provisioner
spec:
  replicas: 1
  strategy:
    type: Recreate
  selector:
    matchLabels:
      app: efs-provisioner
      ...
    spec:
      serviceAccount: efs-provisioner
      containers:
        - name: efs-provisioner
          image: quay.io/external_storage/efs-provisioner:latest
          env:
            - name: FILE_SYSTEM_ID
              value: fs-13229953
            - name: AWS_REGION
              value: ap-southeast-1
            - name: PROVISIONER_NAME
              value: my-aws.com/aws-efs
          volumeMounts:
            - name: pv-volume
              mountPath: /persistentvolumes
      volumes:
        - name: pv-volume
          nfs:
            server: fs-13229953.efs.ap-southeast-1.amazonaws.com
            path: /
```
- kubectl apply -f volume-pvc.yml
```
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: aws-efs
  labels:
    app: test-pvc
spec:
  accessModes:
  - ReadWriteMany
  resources:
    requests:
      storage: 1Mi
  storageClassName: aws-efs
```

- kubectl get pvc
![pvc_1](https://user-images.githubusercontent.com/88864433/133474884-3f4b8c61-953d-4631-908f-783523d8846c.PNG)

- deployment.yml
```
    spec:
      containers:
        - name: order
          image: 879772956301.dkr.ecr.ap-southeast-1.amazonaws.com/order:latest
          ports:
            - containerPort: 8080
          readinessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
.... 중략
          volumeMounts:
          - name: volume
            mountPath: /logs
        volumes:
        - name: volume
          persistentVolumeClaim:
            claimName: aws-efs
```

- application.yml
```
logging:
  path: /logs/order
  file:
    max-history: 30
  level:
    org.springframework.cloud: debug
```

- 최종 테스트 화면

![pvc_최종](https://user-images.githubusercontent.com/88864433/133479414-111980fb-598b-4e5a-8f13-24255d11f53a.PNG)

