package guru.sfg.beer.inventory.service.services.inventory;

import com.spring.msscbeerservice.events.common.BeerDto;
import com.spring.msscbeerservice.events.common.NewInventoryEvent;
import guru.sfg.beer.inventory.service.config.JmsConfig;
import guru.sfg.beer.inventory.service.domain.BeerInventory;
import guru.sfg.beer.inventory.service.repositories.BeerInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryListener {
    private final BeerInventoryRepository beerInventoryRepository;

    @JmsListener(destination = JmsConfig.NEW_INVENTORY_REQUEST_QUEUE)
    public void listen(NewInventoryEvent newInventoryEvent) {
        log.debug("Got Inventory : " + newInventoryEvent.toString());
        BeerDto beerDto = newInventoryEvent.getBeerDto();
        beerInventoryRepository.save(BeerInventory.builder()
                .beerId(beerDto.getId())
                .upc(beerDto.getUpc())
                .quantityOnHand(beerDto.getQuantityOnHand())
                .build());
    }
}
