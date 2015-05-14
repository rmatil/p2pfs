package net.f4fs.filesystem.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.f4fs.filesystem.event.events.AEvent;
import net.f4fs.filesystem.event.listeners.IEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Event dispatcher, on which event listeners can be registered
 * for certain events, and events can be dispatched.
 * 
 * @author Raphael
 *
 */
public class EventDispatcher {

    private Map<String, List<IEventListener>> eventListeners;

    private final Logger                      logger = LoggerFactory.getLogger("EventDispatcher.class");

    public EventDispatcher() {
        this.eventListeners = new HashMap<>();
    }

    public void addEventListener(IEventListener pEventListener) {
        if (!this.eventListeners.containsKey(pEventListener.getEventName())) {
            this.eventListeners.put(pEventListener.getEventName(), new ArrayList<>());
        }

        this.eventListeners.get(pEventListener.getEventName()).add(pEventListener);
        this.logger.debug("Add event listener for event '" + pEventListener.getEventName() + "'");
    }

    public void dispatchEvent(String pEventName, AEvent pEvent) {
        this.logger.debug("Dispatched event '" + pEventName + "'.");

        if (!this.eventListeners.containsKey(pEventName)) {
            // no event registered for this event name
            return;
        }

        for (IEventListener entry : this.eventListeners.get(pEventName)) {
            entry.handleEvent(pEvent);
        }
    }

}
