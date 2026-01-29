package com.otus.highload.controller;

import com.otus.highload.model.post.Post;
import com.otus.highload.model.post.PostResponse;
import com.otus.highload.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v0/api/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public Post createPost(@RequestBody Post post) {
        return postService.createPost(post);
    }

    @PostMapping("/ws")
    public Post createPostWs(@RequestBody Post post) {
        return postService.createPostWs(post);
    }

    @PutMapping
    public Post update(@RequestBody Post post) {
       return postService.update(post);
    }

    @GetMapping("/{id}")
    public Post getById(@PathVariable String id) {
        return postService.findById(id);
    }

    @DeleteMapping("/{postId}")
    public void delete(@PathVariable String id) {
        postService.delete(id);
    }

    @GetMapping("/feed")
    public List<PostResponse> getFeed() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = ((String) auth.getPrincipal());
        return postService.getFeed(email);
    }

    @GetMapping("/feed/count")
    public Integer getFeedCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = ((String) auth.getPrincipal());
        return postService.getFeed(email).size();
    }
}
