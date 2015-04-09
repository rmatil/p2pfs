package net.f4fs.util;


public class HexFactory {
    
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String stringToHex(String string) {
        return bytesToHex(string.getBytes());
    }
    
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        String ret = new String(hexChars);
        
        return "0x" + ret; 
    }
    
    public static String hexStringToString(String hexString) {
        return new String(hexStringToByteArray(hexString));
    }
    
    public static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                                 + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }
}
