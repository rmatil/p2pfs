package net.f4fs.controller;


public class Main {

    public static void main(String[] args) {
        Startup startup = new Startup();
        
        if(args.length == 0){
            // normal startup, get information of bootstrap peer (if one exists) from server,
            // or start as bootstrap peer if no information exists
            startup.startWithBootstrapServer();
        } else if (args.length == 2){
            // Bootstrap peer is known in advance
            // Direct connection to bootstrap peer with bootstrap peer ip = args[1] 
            // and bootstrap peer port = args[2]
            startup.startWithoutBootstrapServer(args[1], args[2]);
        } else {
            System.out.println("[START] Wrong arguments: No arguments for normal start with bootrstrap server"
                    + "or <ConnectionIP><ConnectionPort> for start with known bootstrap peer");
            System.exit(1);
        }
    }
}
