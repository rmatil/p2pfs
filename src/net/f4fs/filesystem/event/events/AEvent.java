package net.f4fs.filesystem.event.events;

public abstract class AEvent {
    
    /**
     * The event name, such as <code>filesystem.before_write_event</code>.
     * <i>Note:</i> Please use the above naming conventions
     */
    public static String eventName;
}
