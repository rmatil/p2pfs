package net.f4fs.filesystem.event.listeners;

import java.io.IOException;
import java.util.logging.Logger;

import net.f4fs.filesystem.event.events.AEvent;
import net.f4fs.filesystem.event.events.CompleteWriteEvent;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;


public class WriteFileEventListener
        implements IEventListener {
    
    private Logger logger = Logger.getLogger("WriteFileEventListener.class");

    @Override
    public void handleEvent(AEvent pEvent) {
        if (!(pEvent instanceof CompleteWriteEvent)) {
            return;
        }
        
        CompleteWriteEvent writeEvent = (CompleteWriteEvent) pEvent;
        
        try {
            writeEvent.getFsPeer().putData(Number160.createHash(writeEvent.getPath()), new Data(writeEvent.getContent().array()));
        } catch (ClassNotFoundException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
        
        logger.info("Wrote whole file on path '" + writeEvent.getPath() + "' containing '" + writeEvent.getContent().capacity() + "' bytes to DHT");
    }

    @Override
    public String getEventName() {
        return CompleteWriteEvent.eventName;
    }

}
