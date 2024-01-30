package run.halo.app.security.authentication.pat;

import com.nimbusds.jose.jwk.JWK;

public interface PatJwkSupplier {

    JWK getJwk();

}
