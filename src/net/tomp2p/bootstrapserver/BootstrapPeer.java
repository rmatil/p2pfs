package net.tomp2p.bootstrapserver;


public class BootstrapPeer {
    
    public static final String BOOTSTRAP_SERVER = "http://188.226.178.35";
    
    public static void main(String args[]){
        
        BoostrapServerAccess bsa = new BoostrapServerAccess(BOOTSTRAP_SERVER);
        
        System.out.println(bsa.get());
        System.out.println("");
        System.out.println(bsa.post("address", "192.168.1.11"));
    }

}
