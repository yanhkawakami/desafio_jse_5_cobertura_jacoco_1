package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.entities.RoleEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.repositories.UserRepository;
import com.devsuperior.dsmovie.tests.UserDetailsFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import com.devsuperior.dsmovie.utils.CustomUserUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class UserServiceTests {

	@InjectMocks
	private UserService service;

    @Mock
    UserRepository repository;

    @Mock
    CustomUserUtil userUtil;

    private UserEntity user;
    private RoleEntity roleAdmin;

    private List<UserDetailsProjection> userDetails;
    private List<UserDetailsProjection> emptyUserDetails;
    private String existingUsername;
    private String nonExistingUsername;

    @BeforeEach
    void setUp() {
        existingUsername = "maria@gmail.com";
        nonExistingUsername = "alex@gmail.com";

        roleAdmin = new RoleEntity(2L, "ROLE_ADMIN");

        user = UserFactory.createUserEntity();
        user.addRole(roleAdmin);

        userDetails = UserDetailsFactory.createCustomAdminUser(existingUsername);
        emptyUserDetails = new ArrayList<>();

        Mockito.when(repository.findByUsername(existingUsername)).thenReturn(Optional.ofNullable(user));
        Mockito.when(repository.findByUsername(nonExistingUsername)).thenThrow(UsernameNotFoundException.class);

        Mockito.when(repository.searchUserAndRolesByUsername(existingUsername)).thenReturn(userDetails);
        Mockito.when(repository.searchUserAndRolesByUsername(nonExistingUsername)).thenReturn(emptyUserDetails);


    }

	@Test
	public void authenticatedShouldReturnUserEntityWhenUserExists() {
        Mockito.when(userUtil.getLoggedUsername()).thenReturn(existingUsername);

        UserEntity result = service.authenticated();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.getUsername(), result.getUsername());

	}

	@Test
	public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
        Mockito.when(userUtil.getLoggedUsername()).thenReturn(nonExistingUsername);

        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            service.authenticated();
        });
	}

	@Test
	public void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {
        UserDetails result = service.loadUserByUsername(existingUsername);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.getUsername(), result.getUsername());
        Assertions.assertEquals(user.getAuthorities(), result.getAuthorities());
	}

	@Test
	public void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername(nonExistingUsername);
        });

	}
}
