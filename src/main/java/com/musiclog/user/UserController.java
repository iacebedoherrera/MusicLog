package com.musiclog.user;

import com.musiclog.shared.security.AuthenticatedUser;
import com.musiclog.user.dto.UpdateProfileRequest;
import com.musiclog.user.dto.UserProfileResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}")
    public UserProfileResponse profile(@PathVariable String username) {
        return userService.getPublicProfile(username);
    }

    @PutMapping("/me")
    public UserProfileResponse updateMe(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(AuthenticatedUser.from(authentication).id(), request);
    }

    @PostMapping("/{username}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void follow(Authentication authentication, @PathVariable String username) {
        userService.follow(AuthenticatedUser.from(authentication).id(), username);
    }

    @DeleteMapping("/{username}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(Authentication authentication, @PathVariable String username) {
        userService.unfollow(AuthenticatedUser.from(authentication).id(), username);
    }

    @GetMapping("/{username}/followers")
    public List<UserProfileResponse> followers(@PathVariable String username) {
        return userService.followers(username);
    }

    @GetMapping("/{username}/following")
    public List<UserProfileResponse> following(@PathVariable String username) {
        return userService.following(username);
    }
}
