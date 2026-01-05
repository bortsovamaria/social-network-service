package com.otus.highload.service;

import com.otus.highload.model.user.User;
import com.otus.highload.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserService userService;

    @Transactional
    public void addFriendToUser(String friendId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userEmail = principal.toString();
        Optional<User> userOpt = userService.findByEmail(userEmail);
        userOpt.ifPresent(user -> friendshipRepository.addFriend(userOpt.get().getId(), friendId));
    }

    @Transactional
    public void deleteFriendToUser(String friendId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userEmail = principal.toString();
        Optional<User> userOpt = userService.findByEmail(userEmail);
        userOpt.ifPresent(user -> friendshipRepository.deleteFriend(userOpt.get().getId(), friendId));
    }

    public List<String> getFriendIds(String userId) {
        return friendshipRepository.getFriendIds(userId);
    }

    public List<String> getFollowerIds(String userId) {
        return friendshipRepository.getFollowersId(userId);
    }
}
