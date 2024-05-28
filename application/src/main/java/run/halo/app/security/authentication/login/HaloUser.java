package run.halo.app.security.authentication.login;

import java.util.Collection;
import java.util.Objects;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import run.halo.app.security.HaloUserDetails;

public class HaloUser implements HaloUserDetails, CredentialsContainer {

    private final UserDetails delegate;

    private final boolean twoFactorAuthEnabled;

    private String totpEncryptedSecret;

    public HaloUser(UserDetails delegate,
        boolean twoFactorAuthEnabled,
        String totpEncryptedSecret) {
        Assert.notNull(delegate, "Delegate user must not be null");
        this.delegate = delegate;
        this.twoFactorAuthEnabled = twoFactorAuthEnabled;
        this.totpEncryptedSecret = totpEncryptedSecret;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getPassword() {
        return delegate.getPassword();
    }

    @Override
    public String getUsername() {
        return delegate.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return delegate.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return delegate.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return delegate.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    @Override
    public void eraseCredentials() {
        if (delegate instanceof CredentialsContainer container) {
            container.eraseCredentials();
        }
        this.totpEncryptedSecret = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HaloUser user) {
            return Objects.equals(this.delegate, user.delegate);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.delegate.hashCode();
    }

    @Override
    public boolean isTwoFactorAuthEnabled() {
        return this.twoFactorAuthEnabled;
    }

    @Override
    public String getTotpEncryptedSecret() {
        return this.totpEncryptedSecret;
    }

    public static class Builder {

        private final UserDetails user;

        private boolean twoFactorAuthEnabled;

        private String totpEncryptedSecret;

        public Builder(UserDetails user) {
            this.user = user;
        }

        public Builder twoFactorAuthEnabled(boolean twoFactorAuthEnabled) {
            this.twoFactorAuthEnabled = twoFactorAuthEnabled;
            return this;
        }

        public Builder totpEncryptedSecret(String totpEncryptedSecret) {
            this.totpEncryptedSecret = totpEncryptedSecret;
            return this;
        }

        public HaloUserDetails build() {
            return new HaloUser(user, twoFactorAuthEnabled, totpEncryptedSecret);
        }
    }
}
