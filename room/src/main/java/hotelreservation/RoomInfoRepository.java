package hotelreservation;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="roomInfos", path="roomInfos")
public interface RoomInfoRepository extends PagingAndSortingRepository<RoomInfo, Long>{

}
