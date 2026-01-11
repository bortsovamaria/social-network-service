package com.otus.highload.service;

import com.otus.highload.model.user.User;
import com.otus.highload.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserService userService;

    @Transactional
    public void addFriendToUser(String friendId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userEmail = principal.toString();
        User user = userService.findByEmail(userEmail);
        friendshipRepository.addFriend(user.getId(), friendId);
    }

    @Transactional
    public void deleteFriendToUser(String friendId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userEmail = principal.toString();
        User user = userService.findByEmail(userEmail);
        friendshipRepository.deleteFriend(user.getId(), friendId);
    }

    public List<String> getFriendIds(String userId) {
        return friendshipRepository.getFriendIds(userId);
    }

    public List<String> getFollowerIds(String userId) {
        return friendshipRepository.getFollowersId(userId);
    }
}
