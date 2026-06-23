package com.musiclog.user;

import com.musiclog.shared.security.JwtService;
import com.musiclog.user.dto.LoginRequest;
import com.musiclog.user.dto.LoginResponse;
import com.musiclog.user.dto.RegisterRequest;
import com.musiclog.user.dto.UpdateProfileRequest;
import com.musiclog.user.dto.UserProfileResponse;
import com.musiclog.user.events.UserFollowedEvent;
import com.musiclog.user.events.UserRegisteredEvent;
import com.musiclog.user.events.UserUnfollowedEvent;
import jakarta.persistence.EntityNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventPublisher;

    public UserService(
            UserRepository userRepository,
            FollowRepository followRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        User user = new User(
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.displayName());
        User saved = userRepository.save(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(saved.getId(), saved.getUsername()));
        return UserProfileResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository
                .findFirstByUsernameIgnoreCaseOrEmailIgnoreCase(request.usernameOrEmail(), request.usernameOrEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return new LoginResponse(jwtService.createToken(user.getId(), user.getUsername()), user.getId(), user.getUsername());
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getPublicProfile(String username) {
        return UserProfileResponse.from(findByUsername(username));
    }

    @Transactional(readOnly = true)
    public UUID getUserIdByUsername(String username) {
        return findByUsername(username).getId();
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.updateProfile(request.displayName(), request.bio(), request.avatarUrl());
        return UserProfileResponse.from(user);
    }

    @Transactional
    public void follow(UUID followerId, String usernameToFollow) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new EntityNotFoundException("Follower not found"));
        User followed = findByUsername(usernameToFollow);
        if (follower.getId().equals(followed.getId())) {
            throw new IllegalArgumentException("Users cannot follow themselves");
        }
        if (!followRepository.existsByFollowerIdAndFollowedId(follower.getId(), followed.getId())) {
            followRepository.save(new Follow(follower.getId(), followed.getId()));
            eventPublisher.publishEvent(new UserFollowedEvent(follower.getId(), followed.getId()));
        }
    }

    @Transactional
    public void unfollow(UUID followerId, String usernameToUnfollow) {
        User followed = findByUsername(usernameToUnfollow);
        long deleted = followRepository.deleteByFollowerIdAndFollowedId(followerId, followed.getId());
        if (deleted > 0) {
            eventPublisher.publishEvent(new UserUnfollowedEvent(followerId, followed.getId()));
        }
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> followers(String username) {
        UUID followedId = findByUsername(username).getId();
        List<UUID> ids = followRepository.findByFollowedIdOrderByCreatedAtDesc(followedId).stream()
                .map(Follow::getFollowerId)
                .toList();
        return profilesByIds(ids);
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> following(String username) {
        UUID followerId = findByUsername(username).getId();
        List<UUID> ids = followRepository.findByFollowerIdOrderByCreatedAtDesc(followerId).stream()
                .map(Follow::getFollowedId)
                .toList();
        return profilesByIds(ids);
    }

    @Transactional(readOnly = true)
    public List<UUID> followersOf(UUID userId) {
        return followRepository.findByFollowedIdOrderByCreatedAtDesc(userId).stream()
                .map(Follow::getFollowerId)
                .toList();
    }

    private List<UserProfileResponse> profilesByIds(List<UUID> ids) {
        Map<UUID, User> users = new LinkedHashMap<>();
        userRepository.findByIdIn(ids).forEach(user -> users.put(user.getId(), user));
        return ids.stream()
                .map(users::get)
                .filter(user -> user != null)
                .map(UserProfileResponse::from)
                .toList();
    }

    private User findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }
}
