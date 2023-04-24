package info.ponciano.lab.spalodwfs.controller.security;

import java.security.*;

import org.springframework.stereotype.Component;

@Component
public class RsaKeys {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;


    public RsaKeys() throws NoSuchAlgorithmException
    {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom secureRandom = new SecureRandom();
        keyPairGenerator.initialize(2048, secureRandom);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
    }

    
    public PrivateKey getPrivateKey() {
        return privateKey;
    }
    
    public PublicKey getPublicKey() {
        return publicKey;
    }
    
}
    
