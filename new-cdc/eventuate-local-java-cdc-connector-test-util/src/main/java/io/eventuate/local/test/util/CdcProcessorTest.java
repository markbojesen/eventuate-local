package io.eventuate.local.test.util;

import io.eventuate.Int128;
import io.eventuate.javaclient.commonimpl.EntityIdVersionAndEventIds;
import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.PublishedEvent;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class CdcProcessorTest extends AbstractCdcTest implements CdcProcessorCommon{

  @Test
  public void shouldReadNewEventsOnly() throws InterruptedException {
    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();
    CdcProcessor<PublishedEvent> cdcProcessor = createCdcProcessor();
    cdcProcessor.start(publishedEvent -> {
      publishedEvents.add(publishedEvent);
      onEventSent(publishedEvent);
    });

    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds entityIdVersionAndEventIds = saveEvent(accountCreatedEventData);
    waitForEvent(publishedEvents, entityIdVersionAndEventIds.getEntityVersion(), LocalDateTime.now().plusSeconds(10), accountCreatedEventData);
    cdcProcessor.stop();

    publishedEvents.clear();
    cdcProcessor.start(publishedEvent -> {
      publishedEvents.add(publishedEvent);
      onEventSent(publishedEvent);
    });
    List<String> excludedIds = entityIdVersionAndEventIds.getEventIds().stream().map(Int128::asString).collect(Collectors.toList());

    accountCreatedEventData = generateAccountCreatedEvent();
    entityIdVersionAndEventIds = updateEvent(entityIdVersionAndEventIds.getEntityId(), entityIdVersionAndEventIds.getEntityVersion(), accountCreatedEventData);
    waitForEventExcluding(publishedEvents, entityIdVersionAndEventIds.getEntityVersion(), LocalDateTime.now().plusSeconds(10), accountCreatedEventData, excludedIds);
    cdcProcessor.stop();
  }

  @Test
  public void shouldReadUnprocessedEventsAfterStartup() throws InterruptedException {
    BlockingQueue<PublishedEvent> publishedEvents = new LinkedBlockingDeque<>();

    String accountCreatedEventData = generateAccountCreatedEvent();
    EntityIdVersionAndEventIds entityIdVersionAndEventIds = saveEvent(accountCreatedEventData);

    CdcProcessor<PublishedEvent> cdcProcessor = createCdcProcessor();
    cdcProcessor.start(publishedEvents::add);

    waitForEvent(publishedEvents, entityIdVersionAndEventIds.getEntityVersion(), LocalDateTime.now().plusSeconds(20), accountCreatedEventData);
    cdcProcessor.stop();
  }

  private PublishedEvent waitForEventExcluding(BlockingQueue<PublishedEvent> publishedEvents, Int128 eventId, LocalDateTime deadline, String eventData, List<String> excludedIds) throws InterruptedException {
    PublishedEvent result = null;
    while (LocalDateTime.now().isBefore(deadline)) {
      long millis = ChronoUnit.MILLIS.between(deadline, LocalDateTime.now());
      PublishedEvent event = publishedEvents.poll(millis, TimeUnit.MILLISECONDS);
      if (event != null) {
        if (event.getId().equals(eventId.asString()) && eventData.equals(event.getEventData())) {
          result = event;
          break;
        }
        if (excludedIds.contains(event.getId()))
          throw new RuntimeException("Event with excluded id found in the queue");
      }
    }
    if (result != null)
      return result;
    throw new RuntimeException("event not found: " + eventId);
  }
}
