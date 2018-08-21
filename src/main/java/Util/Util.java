package Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public class Util {

    private static final String CERT_PATH="E:\\code\\5B\\1.pfx";
    private static final String CERT_PWD="d5np2b4s";

    private static KeyStore loadServerKeyStore() throws Exception {
        KeyStore keyStore=KeyStore.getInstance("PKCS12");
        InputStream inputStream=new FileInputStream(new File(CERT_PATH));
        try{
            keyStore.load(inputStream,CERT_PWD.toCharArray());
        }catch (Exception e){
            e.printStackTrace();
        }
        return keyStore;
    }
    private static String getAlias(KeyStore keyStore)throws Exception{
        Enumeration enumeration = keyStore.aliases();
        String keyAlias=null;
        if(enumeration.hasMoreElements()){
            keyAlias=(String)enumeration.nextElement();
        }
        return keyAlias;
    }
    public static X509Certificate getCertficate(){
        try{
            KeyStore keyStore=loadServerKeyStore();
            String alias=getAlias(keyStore);
            return (X509Certificate) keyStore.getCertificate(alias);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static PrivateKey getPrivateKey(){
        try{
            KeyStore keyStore=loadServerKeyStore();
            String alias=getAlias(keyStore);
            return (PrivateKey) keyStore.getKey(alias, CERT_PWD.toCharArray());
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
