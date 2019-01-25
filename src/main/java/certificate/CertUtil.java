package certificate;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Map;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class CertUtil {

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


  /**
   * 动态生成服务器证书,并进行CA签授
   *
   * @param issuer 颁发机构
   */
  public static X509Certificate genCert(String issuer, PrivateKey caPriKey, Date caNotBefore,
      Date caNotAfter, PublicKey serverPubKey, String... hosts) throws Exception {

    //根据CA证书subject来动态生成目标服务器证书的issuer和subject
    String subject = "C=CN, ST=GD, L=SZ, O=lee, OU=study, CN=" + hosts[0];

    JcaX509v3CertificateBuilder jv3Builder = new JcaX509v3CertificateBuilder(new X500Name(issuer),

        BigInteger.valueOf(System.currentTimeMillis() + (long) (Math.random() * 10000) + 1000),
        caNotBefore,
        caNotAfter,
        new X500Name(subject),
        serverPubKey);
    //SAN扩展证书支持的域名，否则浏览器提示证书不安全
    GeneralName[] generalNames = new GeneralName[hosts.length];
    for (int i = 0; i < hosts.length; i++) {
      generalNames[i] = new GeneralName(GeneralName.dNSName, hosts[i]);
    }
    GeneralNames subjectAltName = new GeneralNames(generalNames);
    jv3Builder.addExtension(Extension.subjectAlternativeName, false, subjectAltName);

    ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(caPriKey);
    return new JcaX509CertificateConverter().getCertificate(jv3Builder.build(signer));
  }



}
