package run.halo.app.core.extension.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ExtensionClient;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    ExtensionClient client;

    @InjectMocks
    UserServiceImpl userService;

    @Test
    void shouldGetEmptyUserIfUserNotFoundInExtension() {
        when(client.fetch(User.class, "faker")).thenReturn(Optional.empty());

        StepVerifier.create(userService.getUser("faker"))
            .verifyComplete();

        verify(client, times(1)).fetch(eq(User.class), eq("faker"));
    }

    @Test
    void shouldGetUserIfUserFoundInExtension() {
        User fakeUser = new User();
        when(client.fetch(User.class, "faker")).thenReturn(Optional.of(fakeUser));

        StepVerifier.create(userService.getUser("faker"))
            .assertNext(user -> assertEquals(fakeUser, user))
            .verifyComplete();

        verify(client, times(1)).fetch(eq(User.class), eq("faker"));
    }

    @Test
    void shouldUpdatePasswordIfUserFoundInExtension() {
        User fakeUser = new User();
        fakeUser.setSpec(new User.UserSpec());

        when(client.fetch(User.class, "faker")).thenReturn(Optional.of(fakeUser));

        StepVerifier.create(userService.updatePassword("faker", "new-fake-password"))
            .verifyComplete();

        verify(client, times(1)).fetch(eq(User.class), eq("faker"));
        verify(client, times(1)).update(argThat(extension -> {
            var user = (User) extension;
            return "new-fake-password".equals(user.getSpec().getPassword());
        }));
    }

    @Test
    void shouldNotUpdatePasswordIfUserNotFoundInExtension() {
        when(client.fetch(User.class, "faker")).thenReturn(Optional.empty());

        StepVerifier.create(userService.updatePassword("faker", "new-fake-password"))
            .verifyComplete();

        verify(client, times(1)).fetch(eq(User.class), eq("faker"));
        verify(client, times(0)).update(any());
    }
}
