package com.otus.highload.mapper;

import com.otus.highload.model.post.Post;
import com.otus.highload.model.post.PostResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {

    PostResponse toResponse(Post post);

    List<PostResponse> toResponse(List<Post> post);
}
