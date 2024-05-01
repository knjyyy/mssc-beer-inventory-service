package guru.sfg.beer.inventory.service.services.inventory;

import com.spring.brewery.model.BeerOrderDto;

public interface AllocationService {
    Boolean allocateOrder(BeerOrderDto beerOrderDto);
}
