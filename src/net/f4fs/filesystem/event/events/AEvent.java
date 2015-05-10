package net.f4fs.filesystem.event.events;

/**
 * An object representing an event for the defined <code>eventName</code>
 * 
 * @author Raphael
 *
 */
public abstract class AEvent {
    
    /**
     * The event name, such as <code>filesystem.before_write_event</code>.
     * <i>Note:</i> Please use the above naming conventions
     */
    public static String eventName;
}
