package net.f4fs.filesystem.event.listeners;

import net.f4fs.filesystem.event.events.AEvent;

/**
 * An interface which all event listeners must implement
 * to use {@link net.f4fs.filesystem.event.EventDispatcher EventDispatcher}.
 * 
 * @author Raphael
 *
 */
public interface IEventListener {
   
    /**
     * Name of the event to which the event listener is registered
     * 
     * @return The event to which the listener should be registered 
     */
    public String getEventName();
    
    /**
     * The method which gets called on event.
     * 
     * @param pEvent The context of the dispatched event
     */
    public void handleEvent(AEvent pEvent);

}
