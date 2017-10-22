package com.wifi.live.token.manager;

import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.base.Charsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class TokenManager {

    private static final String ISSUERE = "LIVE_SERVER";
    private static final String SECRET_HMAC256 = "35016f17909d46c5868d4d68b413955e";

    private Algorithm algorithm;

    public void init() {
        try {
            algorithm = Algorithm.HMAC256(SECRET_HMAC256);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String createToken(String userId) {
        String token = createJwt(userId);
        return token;
    }

    private String createJwt(String userId) {
        try {
            String token = JWT.create()
                    .withAudience(userId + "")
                    .withIssuer(ISSUERE)
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)))
                    .sign(algorithm);
            return base64encode(token);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String verifierToken(String token) {
        token = base64Decode(token);
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(ISSUERE)
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getAudience().get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        TokenManager tokenManager = new TokenManager();
        tokenManager.init();
        String token = tokenManager.createJwt("29");
        System.out.println(token);
        System.out.println(tokenManager.verifierToken(token));
    }

    private static String base64encode(String content) {
        return Base64.getEncoder().encodeToString(content.getBytes(Charsets.UTF_8));
    }

    private static String base64Decode(String token) {
        return new String(Base64.getDecoder().decode(token.getBytes(Charsets.UTF_8)));
    }
}
