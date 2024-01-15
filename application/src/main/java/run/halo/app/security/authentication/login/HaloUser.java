package run.halo.app.security.authentication.login;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import run.halo.app.core.extension.User;

public class HaloUser implements UserDetails, CredentialsContainer {

    private final User delegate;

    private final Collection<? extends GrantedAuthority> authorities;

    public HaloUser(User delegate, Collection<? extends GrantedAuthority> authorities) {
        Assert.notNull(delegate, "Delegate user must not be null");
        Assert.notNull(authorities, "Authorities must not be null");
        this.delegate = delegate;

        this.authorities = authorities.stream()
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(GrantedAuthority::getAuthority))
            .toList();
    }

    public HaloUser(User delegate) {
        this(delegate, List.of());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return delegate.getSpec().getPassword();
    }

    @Override
    public String getUsername() {
        return delegate.getMetadata().getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        var disabled = delegate.getSpec().getDisabled();
        return disabled == null || !disabled;
    }

    public User getDelegate() {
        return delegate;
    }

    @Override
    public void eraseCredentials() {
        delegate.getSpec().setPassword(null);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HaloUser user) {
            var username = this.delegate.getMetadata().getName();
            var otherUsername = user.delegate.getMetadata().getName();
            return username.equals(otherUsername);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.delegate.getMetadata().getName().hashCode();
    }

    public static class Builder {

        private final User user;

        private Collection<? extends GrantedAuthority> authorities;

        public Builder(User user) {
            this.user = user;
        }

        public Builder authorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities = authorities;
            return this;
        }

        public HaloUser build() {
            return new HaloUser(user, authorities);
        }
    }
}
