package certificate;

import io.netty.handler.ssl.SslContext;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CrtUtil {

  static {
    Security.addProvider(new BouncyCastleProvider());
  }


  /**
   * 生成RSA 密钥对 长度2048
   * @return KeyPair
   * @throws Exception
   */
  public static KeyPair genKeyPair() throws Exception{
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
    keyPairGenerator.initialize(2048, new SecureRandom());
    return keyPairGenerator.genKeyPair();
  }


}
