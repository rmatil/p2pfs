package net.f4fs.filesystem.event.listeners;

import net.f4fs.filesystem.event.events.AEvent;


public interface IEventListener {
    
    public String getEventName();
    
    public void handleEvent(AEvent pEvent);

}
