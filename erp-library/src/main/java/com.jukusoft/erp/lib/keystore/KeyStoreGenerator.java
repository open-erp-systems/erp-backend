package com.jukusoft.erp.lib.keystore;

import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;

public class KeyStoreGenerator {

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    /**
    * generate an X.509 RSA256WithRSA certificate for SSL
     *
     * @param savePath file path to save
     * @param password password for key store
     * @param keySize number of bits, by default 1024
    */
    public static void generateJKSKeyStore (String savePath, String password, int keySize) throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, InvalidKeyException, NoSuchProviderException, SignatureException {
        //create a key store of type "Java Key Store"
        KeyStore store = KeyStore.getInstance("JKS");

        //load key store
        store.load(null, null);

        //generate an key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(/*"SHA256WithRSAEncryption"*/"RSA");
        keyPairGenerator.initialize(keySize);
        KeyPair kPair = keyPairGenerator.generateKeyPair();

        //generate private key
        PrivateKey privKey = kPair.getPrivate();

        //CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // Generate a self-signed key pair and certificate.
        /*store.load(null, null);
        CertAndKeyGen keypair = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
        X500Name x500Name = new X500Name("localhost", "IT", "unknown", "unknown", "unknown", "unknown");
        keypair.generate(1024);
        PrivateKey privKey = keypair.getPrivateKey();
        X509Certificate[] chain = new X509Certificate[1];
        chain[0] = keypair.getSelfCertificate(x500Name, new Date(), (long) 365 * 24 * 60 * 60);
        store.setKeyEntry("selfsigned", privKey, "changeit".toCharArray(), chain);
        store.store(new FileOutputStream(".keystore"), "changeit".toCharArray());*/

        X509Certificate[] chain = new X509Certificate[1];
        chain[0] = generateCertificate(kPair);

        //store key
        store.setKeyEntry("selfsigned", privKey, password.toCharArray(), chain);
        store.store(new FileOutputStream(savePath), password.toCharArray());
    }

    public static X509Certificate generateCertificate(KeyPair keyPair) throws NoSuchAlgorithmException, CertificateEncodingException, NoSuchProviderException, InvalidKeyException, SignatureException {
        final Calendar calendar = Calendar.getInstance();

        X509V3CertificateGenerator cert = new X509V3CertificateGenerator();
        cert.setSerialNumber(BigInteger.valueOf(1));   //or generate a random number
        cert.setSubjectDN(new X509Principal("CN=localhost"));  //see examples to add O,OU etc
        cert.setIssuerDN(new X509Principal("CN=localhost")); //same since it is self-signed
        cert.setPublicKey(keyPair.getPublic());

        //cert.setNotBefore(<date>);
        //cert.setNotAfter(<date>);

        int hoursBefore = 0;

        //6 months
        int hoursAfter = 24 * 30 * 6;

        calendar.add(Calendar.HOUR, -hoursBefore);
        cert.setNotBefore(calendar.getTime());

        calendar.add(Calendar.HOUR, hoursBefore + hoursAfter);
        cert.setNotAfter(calendar.getTime());

        cert.setSignatureAlgorithm("SHA256WithRSAEncryption");
        PrivateKey signingKey = keyPair.getPrivate();
        return cert.generate(signingKey, "BC");
    }

}
