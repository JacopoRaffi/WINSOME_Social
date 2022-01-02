import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.*;
import java.security.*;
import java.util.*;

public class ServerHashFunction {
    /**
     *	Metodo che calcola il valore hash SHA-256 di una stringa
     * 	@param s la stringa di input
     *  @return i byte corrispondenti al valore hash dell'input
     */
    public static byte[] sha256(String s) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(s.getBytes());
        return md.digest();
    }
    /**
     *	Metodo per convertire un array di byte in una stringa esadecimale
     *	@param hash un array di byte
     *	@return una stringa esadecimale leggibile
     */
    public static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
