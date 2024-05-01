package guru.sfg.beer.inventory.service.services.inventory;

import com.spring.brewery.model.BeerOrderDto;
import com.spring.brewery.model.BeerOrderLineDto;
import guru.sfg.beer.inventory.service.domain.BeerInventory;
import guru.sfg.beer.inventory.service.repositories.BeerInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Service
@Slf4j
public class AllocationServiceImpl implements AllocationService {
    private final BeerInventoryRepository beerInventoryRepository;

    @Override
    public Boolean allocateOrder(BeerOrderDto beerOrderDto) {
        log.debug("Allocating OrderId: " + beerOrderDto.getId());

        AtomicInteger totalOrdered = new AtomicInteger();
        AtomicInteger totalAllocated =new AtomicInteger();

        beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
            if(((beerOrderLineDto.getOrderQuantity() != null ? beerOrderLineDto.getOrderQuantity() : 0)
                    - (beerOrderLineDto.getQuantityAllocated() != null ? beerOrderLineDto.getQuantityAllocated() : 0)) > 0) {
                allocateBeerOrderLine(beerOrderLineDto);
            }

            totalOrdered.set(totalOrdered.get() + beerOrderLineDto.getOrderQuantity());
            totalAllocated.set(totalAllocated.get() + (beerOrderLineDto.getQuantityAllocated() != null ? beerOrderLineDto.getQuantityAllocated() : 0));
        });

        log.debug("Total Ordered : " + totalOrdered.get() + "Total Allocated: " + totalAllocated.get());
        return totalOrdered.get() == totalAllocated.get();
    }

    private void allocateBeerOrderLine(BeerOrderLineDto beerOrderLineDto) {
        List<BeerInventory> beerInventoryList = beerInventoryRepository.findAllByUpc(beerOrderLineDto.getUpc());

        beerInventoryList.forEach(beerInventory -> {
            int inventory = beerInventory.getQuantityOnHand() != null ? beerInventory.getQuantityOnHand() : 0;
            int orderQuantity = beerOrderLineDto.getOrderQuantity() != null ? beerOrderLineDto.getOrderQuantity() : 0;
            int allocatedQuantity = beerOrderLineDto.getQuantityAllocated() != null ? beerOrderLineDto.getQuantityAllocated() : 0;
            int quantityToAllocate = orderQuantity - allocatedQuantity;

            if(inventory >= quantityToAllocate) {
                inventory -= quantityToAllocate;
                beerOrderLineDto.setQuantityAllocated(orderQuantity);
                beerInventory.setQuantityOnHand(inventory);

                beerInventoryRepository.save(beerInventory);
            } else if (inventory > 0) {
                beerOrderLineDto.setQuantityAllocated(allocatedQuantity + inventory);
                beerInventory.setQuantityOnHand(0);
                beerInventoryRepository.delete(beerInventory);
            }
        });
    }
}
