# 개인과제 - 호텔예약시스템

![hotel_reservation](https://user-images.githubusercontent.com/43808557/135555012-6b055ecf-d165-4939-9882-b917f909ee15.png)
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
객실 관리, 예약 관리 , 결제 관리

[서비스 시나리오]

가. 기능적 요구사항

1. 호텔의 객실 관리팀은 객실 정보를 등록한다.( pub/sub )

2. 고객은 결제가 완료되어야 예약이 완료된다. ( Req/Res )

3. 예약이 완료되면 호텔의 객실이 charge 상태로 변경된다. ( pub/sub )

4. 고객의 객실 예약 상태를 언제든지 조회할 수 있다. (CQRS)

5. 고객은 객실 예약을 취소할 수 있다.( pub/sub )

6. 고객이 예약을 취소하면 결제 취소가 된다.( pub/sub )

7. 결제가 취소 완료되어야 최종적으로 예약 취소가 완료된다. ( pub/sub )



나. 비기능적 요구사항

1. [설계/구현]Req/Resp : 객실 예약시 결제가 반드시 완료되어야 예약이 완료된다.

2. [설계/구현]CQRS : 고객의 객실 예약 상태를 언제든지 조회할 수 있다.

3. [설계/구현]Correlation : 객실 예약 취소 요청 -> 결제 취소 -> 객실 예약 취소 완료로 예약 상태가 변경된다.

4. [설계/구현]saga : 서비스(객실 관리, 결제, 예약)는 단일 서비스 내의 데이터를 처리하고, 각자의 이벤트를 발행하면 연관된 서비스에서 이벤트에 반응하여 각자의 데이터를 변경시킨다.

5. [설계/구현/운영]circuit breaker : 예약 요청 건수가 임계치 이상 발생할 경우 Circuit Breaker 가 발동된다. 

다. 기타 

1. [설계/구현/운영]polyglot :  마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성


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
- 예약 요청됨/예약 완료 이벤트는 동기식 방식으로, 하나로 Grouping하여 정의

```

### 액터, 커맨드를 부착하여 읽기 좋게 

![DDD_3](https://user-images.githubusercontent.com/43808557/135551295-b81c8043-4157-41ca-b1ee-68be73282533.PNG)


### 어그리게잇으로 묶기

![aggre](https://user-images.githubusercontent.com/43808557/135552599-14c82ffa-c21c-4ee0-bf7b-f50f8c17ec97.PNG)

``` 
- 고객의 예약 관리 결제 관리, 호텔의 객실관리는 command와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 묶어줌
```

### 바운디드 컨텍스트로 묶기

![bounded](https://user-images.githubusercontent.com/43808557/135552598-a6a0ee40-31c0-4e31-9927-79bf30e33ad3.PNG)

 
```
- 도메인 서열 분리 
    - Core Domain:  reservation, payment : 없어서는 안될 핵심 서비스이며, 연간 Up-time SLA 수준을 99.999% 목표, 배포주기는 reservation의 경우 1주일 1회 미만, payment의 경우 1개월 1회 미만
    - Supporting Domain:  room : 호텔 객실에 대한 종합적인 관리를 하는 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 각 팀의 자율이나 표준 스프린트 주기가 1주일 이므로 1주일 1회 이상을 기준으로 함
```
### 폴리시 부착


![폴리시 부착](https://user-images.githubusercontent.com/43808557/135552363-f0d75bff-c645-4fc8-8876-4fe329430ba8.PNG)


### 폴리시의 이동과 컨텍스트 맵핑 (점선은 Pub/Sub, 실선은 Req/Resp) 

![pub_sub](https://user-images.githubusercontent.com/43808557/135552578-9cedc146-0ce4-400f-9641-75cd601dfdb2.PNG)
 

### 완성된 모형

![원본](https://user-images.githubusercontent.com/43808557/135552627-883e45c4-bfa9-4c62-8b70-f380083cfb0a.png)

 
### 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

![시나리오1](https://user-images.githubusercontent.com/43808557/135554623-8ccfc1a2-4c0b-4018-a13f-ee61d464008f.png)

```
- 호텔의 객실 관리팀은 객실 정보를 등록한다. (ok)
- 고객은 결제가 완료하여야, 예약이 완료된다. (ok)
- 예약이 완료되면 호텔에 방이 charge 상태로 변경된다. ( ok)

```
![시나리오2](https://user-images.githubusercontent.com/43808557/135552693-c0935e08-7f4a-4c93-8a96-460a6c76a2e8.png)

``` 
- 고객이 예약을 취소할 수 있다 (ok)
- 고객이 예약을 취소하면 결제가 취소된다 (ok)
- 결제 취소가 완료되면, 예약 취소가 완료된다. (ok)

```

### 비기능 요구사항에 대한 검증 (5개가 맞는지 검토 필요)

![비동기식구현](https://user-images.githubusercontent.com/43808557/135571663-5f60fdde-db01-42d0-9e8b-62805bbeb465.PNG)

```
1. [설계/구현]Req/Resp : 결제가 반드시 완료되야 예약이 완료된다.
2. [설계/구현]CQRS : 고객이 예약 정보를 언제든지 확인 할 수 있다.
3. [설계/구현]Correlation : 예약을 취소하면 -> 결제를 취소하고 -> 예약 취소가 완료된다.
4. [설계/구현]saga : 서비스(객실 관리, 결제 관리, 예약 관리)는 단일 서비스 내의 데이터를 처리하고, 각자의 이벤트를 발행하면 연관된 서비스에서 이벤트에 반응하여 각자의 데이터를 변경시킨다.
5. [설계/구현/운영]circuit breaker : 예약  건수가 임계치 이상 발생할 경우 Circuit Breaker 가 발동된다. 
``` 

### 헥사고날 아키텍처 다이어그램 도출

![hexa](https://user-images.githubusercontent.com/43808557/135553157-0485ba39-d569-43f2-8e37-228e5edd170f.PNG)

```
- Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
- 호출관계에서 PubSub 과 Req/Resp 를 구분함
```

# 구현

- 분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 바운더리 컨텍스트 별로 대변되는 마이크로 서비스들을 Spirng-boot로 구현함.
- 구현한 각 서비스를 로컬에서 실행하는 방법. (각자의 포트넘버는 8081 ~ 808n 이다)

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
결제 PaymentRepository.java
```
package hotelreservation;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.repository.CrudRepository;

@RepositoryRestResource(collectionResourceRel="payments", path="payments")
public interface PaymentRepository extends CrudRepository<Payment, Long>{


}

```
호텔의 Room Enitity
``` 
@Entity
@Table(name="RoomInfo_table")
public class RoomInfo {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String roomId;
    private String roomNo;
    private String viewInfo;
    private String status;
    private String roomSize;
    private Date createDate;
    private String amenityInfo;
    private Date reservStartDate;
    private Date reservEndDate;

    @PostPersist
    public void onPostPersist(){
        RoomRegistered roomRegistered = new RoomRegistered();
        BeanUtils.copyProperties(this, roomRegistered);
        roomRegistered.publishAfterCommit();

    }

```
RoomInfoRepository.java
```
package hotelreservation;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="roomInfos", path="roomInfos")
public interface RoomInfoRepository extends PagingAndSortingRepository<RoomInfo, Long>{

}

```

- 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
가능한 현업에서 많이 사용하는 Java와 Spring 기반의 기술을 사용하여 소스코드를 구현하였다.

- 적용 후 Rest API의 테스트

```
[시나리오 1]
- 방 등록
http POST  http://a1a0770d509a8456d801d4fce80f93d2-922306400.ap-northeast-2.elb.amazonaws.com:8080/roomInfos roomId=101 roomNo=101 viewInfo="OceanView" status="NEW" roomSize="84" createDate="2021-09-30" amenityInfo="TV"

- 예약 
http POST  http://a1a0770d509a8456d801d4fce80f93d2-922306400.ap-northeast-2.elb.amazonaws.com:8080/reservations  roomId=A0001 roomNo="101" reservStatus="Approved"  roomStatus="Empty" createRoomDate="2019-01-01"  payCompletedYn=false userId=0001 reservDate="2021-09-30"  userName="YANG" peopleQty=3 payDate="2021-09-30" amount="200000" payMethod="card"  reservStartDate="2021-11-01" reservEndDate="2021-11-03"  roomStatus="Empty" createRoomDate="2019-01-01" 

[시나리오 2]
- 예약 취소 요청
http POST http://a1a0770d509a8456d801d4fce80f93d2-922306400.ap-northeast-2.elb.amazonaws.com:8080/reservations roomId=A0001 roomNo="101" reservStatus="CancelRequest" userId=0001 reservDate="2021-09-30"  userName="YANG" peopleQty=3 payDate="2021-09-30" amount="200000"  payCompletedYn=true  payMethod="card"  reservStartDate="2021-11-01" reservEndDate="2021-11-03"

[체크]
http GET http://a1a0770d509a8456d801d4fce80f93d2-922306400.ap-northeast-2.elb.amazonaws.com:8080/roomInfos
http GET http://a1a0770d509a8456d801d4fce80f93d2-922306400.ap-northeast-2.elb.amazonaws.com:8080/reservations
http GET http://a1a0770d509a8456d801d4fce80f93d2-922306400.ap-northeast-2.elb.amazonaws.com:8080/payment
http GET http://a1a0770d509a8456d801d4fce80f93d2-922306400.ap-northeast-2.elb.amazonaws.com:8080/reservationViews

```

# 동기식 호출

(Request-Response 방식의 서비스 중심 아키텍처 구현)

- 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)

요구사항대로 결제가 완료되어야 예약이 완료되도록 구현한다.

reservaiontin.java Entity Class에 @PostPersist로 결제 완료 Req/Resp후에 예약이 완료되도록 아였다.

``` 
    
    @PostPersist
    public void onPostPersist(){
      
        Logger logger = LoggerFactory.getLogger(this.getClass());
        
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
                    this.setReservStatus("Completed");
       
                    BeanUtils.copyProperties(this, reservationCompleted);
                    reservationCompleted.publishAfterCommit();

                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
    
```

##### 동기식 호출은 PaymentService 클래스를 두어 FeignClient 를 이용하여 호출하도록 하였다.

- PaymentService.java

```

@FeignClient(name="payment", url = "${api.Payment.url}", fallback = PaymentServiceFallback.class)
public interface PaymentService {

    @RequestMapping(method=RequestMethod.POST, path="/payRequest")
    public boolean pay(@RequestBody Payment payment);

}
```
- Payment의 PaymentController.java에서 해당 요청을 처리한다.
```

 @RestController
 public class PaymentController {
   
    @Autowired
    PaymentRepository paymentRepository;

    @PostMapping(value = "/payRequest")
     public boolean createpaymentInfo(@RequestBody Map<String, String> param) {

        boolean result = false;
        Payment payment = new Payment();

        payment.setUserId(param.get("userId"));
        payment.setUserName(param.get("userName"));
        payment.setRoomNo(param.get("roomNo"));
        payment.setAmount(Long.parseLong(param.get("amount"))); 
        payment.setPayStatus(param.get("payStatus"));
        payment.setPayMethod(param.get("payMethod"));
      
        
        System.out.println("-------------------------------");
        System.out.println(param.toString());
        System.out.println("-------------------------------");
        try {
            payment = paymentRepository.save(payment);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
     
 }

```


# 비동기식 호출과 Eventual Consistency 

(이벤트 드리븐 아키텍처)

- 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?

예약 취소 요청을 하면, 결제팀이 결제 취소 후에 이를 다시 예약(Reservation) 관리 서비스에 결제 취소 완료를 알려주는 트랜잭션을 Pub/Sub 관계로 구현하였다.
아래는 예약 관리 시스템에서 예약 취소 요청 이벤트 발생, 결제 관리 시스템에서 결제 취소 완료 이벤트를 kafka를 통해 예약 관리 서비스에 다시 연계받는 코드 내용이다. 

```

    @PostPersist
    public void onPostPersist(){
      
	if(this.reservStatus.equals(RESERVATION_APPROVED) ){
          ....중략
                }
            }
        }else if(this.reservStatus.equals(RESERVATION_CACELREQUEST) ){ //예약 취소 요청 발생 Kafka에 전달
            ReservationCancelRequested reservationCancelRequested = new ReservationCancelRequested();
            BeanUtils.copyProperties(this, reservationCancelRequested);
            reservationCancelRequested.publishAfterCommit();

        }else if(this.reservStatus.equals(RESERVATION_CANCLED)){
            ReservationCanceled reservationCanceled = new ReservationCanceled();
            BeanUtils.copyProperties(this, reservationCanceled);
            reservationCanceled.publishAfterCommit();
        }
```
- 결제 관리 서비스(payment)에서는 예약 취소 요청 이벤트에 대해 이를 수신하여 결제 취소를 수행하기 위해 PolicyHandler를 구현한다. 

```
@Service
public class PolicyHandler{
    @Autowired PaymentRepository paymentRepository;
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCancelRequested_PayCancel(@Payload ReservationCancelRequested reservationCancelRequested){

        if(!reservationCancelRequested.validate()) return;

        Payment payment = new Payment();

        payment.setRoomNo(reservationCancelRequested.getRoomNo());
        payment.setUserId(reservationCancelRequested.getUserId().toString());
        payment.setUserName(reservationCancelRequested.getUserName());
        payment.setAmount(reservationCancelRequested.getAmount());
        payment.setPayDate(reservationCancelRequested.getPayDate());
        payment.setPayMethod(reservationCancelRequested.getPayMethod());
        payment.setPayMethod(reservationCancelRequested.getPayMethod());
        payment.setPayStatus("PayCanceled");
        
        System.out.println("\n\n##### listener PayCancel : " + reservationCancelRequested.toJson() + "\n\n");

        paymentRepository.save(payment);
    }

}
```
- 결제 관리 서비스(payment)에서는 예약 취소 완료를 다시 kafa로 pub하여 예약 관리 시스템에서 예약 취소를 최종적으로 수행할 수 있도록 한다. 
```
package hotelreservation;

import hotelreservation.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired ReservationRepository reservationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRoomRegistered_InsertRoom(@Payload RoomRegistered roomRegistered){

     ..중략
        }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCanceled_updateReservation(@Payload PayCanceled payCanceled){

        if(!payCanceled.validate()) return;

         Reservation reservation = new Reservation();
         reservation.setRoomId(payCanceled.getRoomId());
         reservation.setRoomNo(payCanceled.getRoomNo());
         reservation.setCreateRoomDate(null);
         reservation.setRoomSize("");
         reservation.setAmenityInfo("");
         reservation.setRoomStatus("");
         reservation.setReservStatus("CancelCompleted");
         reservation.setPayCompletedYn(payCanceled.getPayCompletedYn());
         reservation.setUserId(payCanceled.getUserId());
         reservation.setReservDate(payCanceled.getReservDate());
         reservation.setUserName(payCanceled.getUserName());
         reservation.setPeopleQty("");
         reservation.setReservStartDate(payCanceled.getReservStartDate());
         reservation.setReservEndDate(payCanceled.getReservEndDate());
         reservation.setPayMethod(payCanceled.getPayMethod());
         reservation.setAmount(payCanceled.getAmount());
         reservation.setPayDate(payCanceled.getPayDate());

         reservationRepository.save(reservation);
 
    }

}


```

![취소시나리오](https://user-images.githubusercontent.com/43808557/135556940-db63d2db-631e-4487-a23b-28aa621dcc25.PNG)

```
- 예약 취소 - 결제 취소 완료 - 예약 취소 완료에 대한 이벤트 Kafa 수신 결과 화면 
```

# SAGA 패턴

- 취소에 따른 보상 트랜잭션을 설계하였는가?(Saga Pattern)

결제 관리 서비스의 기능을 수행할 수 없더라도 예약 취소는 항상받을 수 있게끔 설계하였다. 
[Reservation 서비스]

Reservation 서비스가  고객으로  예약 취소 요청을 받고 Reservation aggegate 예약 취소 요청(ReservationCancelRequested) 이벤트를 발행한다. - 첫번째 

```   
    @PostPersist
    public void onPostPersist(){
      
        Logger logger = LoggerFactory.getLogger(this.getClass());
        
        //  Thread.sleep(5000);

         if(this.reservStatus.equals(RESERVATION_APPROVED) ){
          hotelreservation.external.Payment payment = new hotelreservation.external.Payment();
                 ////중략
                }
            }
        }else if(this.reservStatus.equals(RESERVATION_CACELREQUEST) ){
            ReservationCancelRequested reservationCancelRequested = new ReservationCancelRequested();
            BeanUtils.copyProperties(this, reservationCancelRequested);
            reservationCancelRequested.publishAfterCommit();

        }

```	
서비스의 트랜젝션 완료

[Payment 서비스]

```
@StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCancelRequested_PayCancel(@Payload ReservationCancelRequested reservationCancelRequested){

```
예약 취소 요청(ReservationCancelRequested) 이벤트가 발행되면 결제관리 서비스에서 해당 이벤트를 확인한다.

```
결제 취소 이벤트를 수행한다. - 두번째 서비스의 트렌젝션 완료


        if(!reservationCancelRequested.validate()) return;

        Payment payment = new Payment();

        payment.setRoomNo(reservationCancelRequested.getRoomNo());
        payment.setUserId(reservationCancelRequested.getUserId().toString());
        payment.setUserName(reservationCancelRequested.getUserName());
        payment.setAmount(reservationCancelRequested.getAmount());
        payment.setPayDate(reservationCancelRequested.getPayDate());
        payment.setPayMethod(reservationCancelRequested.getPayMethod());
        payment.setPayMethod(reservationCancelRequested.getPayMethod());
        payment.setPayStatus("PayCanceled");
        
        System.out.println("\n\n##### listener PayCancel : " + reservationCancelRequested.toJson() + "\n\n");

        paymentRepository.save(payment);
    }
 
```


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

ReservationViewHandler 를 통해 Pub/Sub 기반으로 다른 Aggreate와 분리하여 CQRS가 가능하도록 구현하였다.
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

결제 및 예약이 완료되면 해당 정보를 reservationView에서 확인할 수 있다.


```
- 예약 완료 후 reservationView 확인

http GET http://a1a0770d509a8456d801d4fce80f93d2-922306400.ap-northeast-2.elb.amazonaws.com:8080/reservationViews/2

```

- CQRS 테스트 

![CQRS_결과](https://user-images.githubusercontent.com/43808557/135557627-9c342ed4-4a51-409c-8472-103d5168852c.PNG)


# 폴리글랏 퍼시스턴스


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

![hpa_전](https://user-images.githubusercontent.com/43808557/135564618-457de1d3-48e7-4ea1-9edc-960e0cf642fa.PNG)

평소에 reservation pod Running 상태로 1개 존재

```
kubectl autoscale deployment reservation --min=1 --max=10 --cpu-percent=5 

```
Autoscale 설정 명령어 실행 (CPU 5퍼센트로 설정)

![hpa](https://user-images.githubusercontent.com/43808557/135563963-bdb91439-b880-4091-8d36-918d622b209e.PNG)

Autoscale 설정됨을 확인

최종 Pod 증가 확인

# Zero-downtime deploy (Readiness Probe) 
(무정지 배포) 

서비스의 무정지 배포를 위하여 결제 관리(payment) 서비스의 배포 yaml 파일에 readinessProbe 옵션을 추가하였다.

```
  spec:
      containers:
        - name: payment
          image: 050229413886.dkr.ecr.ap-northeast-2.amazonaws.com/payment:latest
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
```

Payment의 deployment.yml 파일의 이미지 버전을 v1을 적용하고 siege를 실행한 상태에서 v2로 변경 배포를 진행하였다. 

![readness_실행](https://user-images.githubusercontent.com/43808557/135561287-2aef116b-2e09-4e60-b503-a6383e1b60d9.PNG)

서비스의 끊김없이 무정지 배포가 실행됨을 확인하였다. 

![readness_최종결과](https://user-images.githubusercontent.com/43808557/135561285-bdea77b8-d746-4fdf-82f8-3455b1d29f80.PNG)


# Self-healing (Liveness Probe)

```
  spec:
      containers:
        - name: payment
          image: 050229413886.dkr.ecr.ap-northeast-2.amazonaws.com/payment:latest
          ports:
            - containerPort: 8080
          readinessProbe:
          ....
          livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5
```

- 잘못된 포트로 변경하여 yml을 배포
 : port -> 9999로 변경하여 배포
 
 ![liveness_변경](https://user-images.githubusercontent.com/43808557/135562522-32ad413b-9411-463e-9a30-01623a73d27e.PNG)

- 잘못된 경로와 포트여서 kubelet이 자동으로 컨테이너를 재시작하였다. 

![liveness_최종결과](https://user-images.githubusercontent.com/43808557/135562521-3a0caf0e-48b3-4799-baf5-60053f59daf2.PNG)

- POD가 재시작되었다. 


# 운영유연성

- 데이터 저장소를 분리하기 위한 Persistence Volume과 Persistence Volume Claim을 적절히 사용하였는가?

-- EFS(Elastic File System)생성

![EFS](https://user-images.githubusercontent.com/43808557/135567965-94e4992c-3d33-43b6-8477-24a93ab1a4d2.PNG)

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
  template:
    metadata:
      labels:
        app: efs-provisioner
    spec:
      serviceAccount: efs-provisioner
      containers:
        - name: efs-provisioner
          image: quay.io/external_storage/efs-provisioner:latest
          env:
            - name: FILE_SYSTEM_ID
              value: fs-29dc4b49
            - name: AWS_REGION
              value: ap-northeast-2
            - name: PROVISIONER_NAME
              value: my-aws.com/yang-efs
          volumeMounts:
            - name: pv-volume
              mountPath: /persistentvolumes
      volumes:
        - name: pv-volume
          nfs:
            server: fs-29dc4b49.efs.ap-northeast-2.amazonaws.com
            path: /

```
- kubectl apply -f volume-pvc.yml
```
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: yang-efs
  labels:
    app: test-pvc
spec:
  accessModes:
  - ReadWriteMany
  resources:
    requests:
      storage: 1Mi
  storageClassName: yang-efs
```

- kubectl get pvc
![pvc_1](https://user-images.githubusercontent.com/88864433/133474884-3f4b8c61-953d-4631-908f-783523d8846c.PNG)

- deployment.yml
```
   spec:
  replicas: 1
  selector:
    matchLabels:
      app: reservation
  template:
    metadata:
      labels:
        app: reservation
    spec:
      containers:
        - name: reservation
          image: 050229413886.dkr.ecr.ap-southeast-2.amazonaws.com/reservation:latest
          ports:
            - containerPort: 8080
.... 중략
          volumeMounts:
          - name: volume
            mountPath: /logs
        volumes:
        - name: volume
          persistentVolumeClaim:
            claimName: yang-efs
```

- application.yml
```
logging:
  path: /logs/payment
  file:
    max-history: 30
  level:
    org.springframework.cloud: debug
```
- Setting 

![pvc setting](https://user-images.githubusercontent.com/43808557/135567967-c3bec960-ed87-4ea4-b595-ac568b9bedcf.PNG)

- 최종 테스트 화면



