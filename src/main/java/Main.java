import java.io.File;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;

public class Main {
    public static void main(String[] args) throws Exception {

        /*
         * JKS profile
         * key gen : keytool -genkeypair -keyalg RSA -keysize 2048 -storetype JKS -keystore keyStore01.jks
         * key store password : "storepass"
         * key password : "keypass"
         */
        //String keyStoreType = "JKS";
        //String keyStoreFile = "keyStore01.jks";
        //String keyStorePassword = "storepass";
        //String keyPassword = "keypass";

        /*
         * PKCS12 profile
         * key gen : keytool -genkeypair -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keyStore01.p12
         * key store password : "password"
         */
        String keyStoreType = "PKCS12";
        String keyStoreFile = "keyStore01.p12";
        String keyStorePassword = null;
        String keyPassword = "password";

        Server server = createServer(keyStoreType, keyStoreFile, keyStorePassword, keyPassword);

        CompletableFuture<Void> f = server.start();
        f.join();
    }

    private static Server createServer(String keyStoreType, String keyStoreFile, String keyStorePassword,
                                       String keyPassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        String keyStoreFilePath = "" + keyStoreFile;
        keyStore.load(Main.class.getClassLoader().getResourceAsStream(keyStoreFile),
                      keyStorePassword != null ? keyStorePassword.toCharArray() : null);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyPassword != null ? keyPassword.toCharArray() : null);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        return new ServerBuilder().https(8081)
                                  .tls(kmf, sslContextBuilder -> {
                                      sslContextBuilder.keyManager(kmf);
                                      sslContextBuilder.trustManager(tmf);
                                  })
                                  .service("/", ((ctx, req) -> HttpResponse.of("Hello World")))
                                  .build();
    }
}

