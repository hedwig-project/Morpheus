package com.hedwig.morpheus.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import javax.net.ssl.*;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Created by hugo on 14/05/17. All rights reserved. All rights reserved.
 */
public class Securities {
    private static String TLS_VERSION = "TLSv1.2";

    private static KeyFactory getKeyFactoryIntance() throws NoSuchAlgorithmException {
        return KeyFactory.getInstance("RSA");
    }

    private static X509Certificate makeX509CertificateFromFileName(final String fileName)
            throws CertificateException, IOException {
        final Path certificateFilePath = Paths.get(fileName);

        if (!Files.exists(certificateFilePath))
            throw new FileNotFoundException(String.format("The file %s was not found", fileName));

        final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        final BufferedInputStream bufferedInputStream =
                new BufferedInputStream(Files.newInputStream(certificateFilePath));

        final X509Certificate certificate =
                (X509Certificate) certificateFactory.generateCertificate(bufferedInputStream);

        return certificate;
    }

    private static PrivateKey makePrivateKeyFromPemFile(final String keyFileName)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        final PemReader pemReader = new PemReader(new FileReader(keyFileName));
        final PemObject pemObject = pemReader.readPemObject();
        final byte[] pemContent = pemObject.getContent();

        pemReader.close();

        final PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(pemContent);
        final KeyFactory keyFactory = getKeyFactoryIntance();
        final PrivateKey privateKey = keyFactory.generatePrivate(encodedKeySpec);
        return privateKey;
    }

    private static KeyManagerFactory makeKeyManagerFactory(final String clientCertificateFileName,
                                                           final String clientKeyFileName,
                                                           final String clientKeyPassword)
            throws CertificateException, IOException, KeyStoreException, InvalidKeySpecException, NoSuchAlgorithmException, UnrecoverableKeyException {
        final X509Certificate clientCertificate = makeX509CertificateFromFileName(clientCertificateFileName);
        final PrivateKey privateKey = makePrivateKeyFromPemFile(clientKeyFileName);
        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

        keyStore.load(null, null);
        keyStore.setCertificateEntry("certificate", clientCertificate);
        keyStore.setKeyEntry("private-key",
                             privateKey,
                             clientKeyPassword.toCharArray(),
                             new Certificate[]{clientCertificate});

        final KeyManagerFactory keyManagerFactory =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

        keyManagerFactory.init(keyStore, clientKeyPassword.toCharArray());
        return keyManagerFactory;
    }

    private static TrustManagerFactory makeTrustManagerFactory(final String caCertificateFileName)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        final X509Certificate caCertificate = makeX509CertificateFromFileName(caCertificateFileName);
        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca-certificate", caCertificate);

        final TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        trustManagerFactory.init(keyStore);
        return trustManagerFactory;
    }

    public static SSLSocketFactory createSocketFactory(final String caCertificateFileName,
                                                       final String clientCertificateFileName,
                                                       final String clientKeyFileName) throws Exception {
        final String clientKeyPassword = ""; // This should be changed, in order to use the actual password
        try {
            Security.addProvider(new BouncyCastleProvider());

            final KeyManager[] keyManagers = makeKeyManagerFactory(clientCertificateFileName,
                                                                   clientKeyFileName,
                                                                   clientKeyPassword).getKeyManagers();
            final TrustManager[] trustManagers = makeTrustManagerFactory(caCertificateFileName).getTrustManagers();
            final SSLContext context = SSLContext.getInstance(TLS_VERSION);

            context.init(keyManagers, trustManagers, new SecureRandom());
            return context.getSocketFactory();
        } catch (Exception e) {
            throw new Exception("The TLS Socket Factory Could Not Be Created", e);
        }
    }

}
