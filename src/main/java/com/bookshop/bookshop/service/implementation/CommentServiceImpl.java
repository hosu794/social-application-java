package com.bookshop.bookshop.service.implementation;

import com.bookshop.bookshop.exception.BadRequestException;
import com.bookshop.bookshop.exception.ResourceNotFoundException;
import com.bookshop.bookshop.model.Comment;
import com.bookshop.bookshop.model.Love;
import com.bookshop.bookshop.model.Story;
import com.bookshop.bookshop.model.User;
import com.bookshop.bookshop.payload.ApiResponse;
import com.bookshop.bookshop.payload.CommentRequest;
import com.bookshop.bookshop.payload.CommentResponse;
import com.bookshop.bookshop.payload.PagedResponse;
import com.bookshop.bookshop.repository.CommentRepository;
import com.bookshop.bookshop.repository.LoveRepository;
import com.bookshop.bookshop.repository.StoryRepository;
import com.bookshop.bookshop.repository.UserRepository;
import com.bookshop.bookshop.security.UserPrincipal;
import com.bookshop.bookshop.util.ModelMapper;
import com.bookshop.bookshop.util.ValidatePageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CommentServiceImpl implements CommentService {

    @Autowired
    public CommentServiceImpl(UserRepository userRepository, CommentRepository commentRepository, StoryRepository storyRepository, LoveRepository loveRepository) {
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.storyRepository = storyRepository;
        this.loveRepository= loveRepository;
    }

    final private LoveRepository loveRepository;
    final private UserRepository userRepository;
    final private CommentRepository commentRepository;
    final private StoryRepository storyRepository;



    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    public Comment createComment(CommentRequest commentRequest, UserPrincipal currentUser, Long storyId) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Story story = storyRepository.findById(storyId).orElseThrow(() -> new ResourceNotFoundException("Story", "id", storyId));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setStory(story);
        comment.setBody(commentRequest.getBody());

        return commentRepository.save(comment);

    }

    @Override
    public ResponseEntity<?> deleteComment(Long commentId, Long storyId, UserPrincipal currentUser) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        long creationId= user.getId();

        long commentCreationId = comment.getCreatedBy();

        if(creationId == commentCreationId) {
            commentRepository.delete(comment);
            return ResponseEntity.ok(new ApiResponse(true, "Comment deleted successfull"));
        } else {
            throw new BadRequestException("Sorry you not created this comment");
        }

    }

    @Override
    public CommentResponse getCommentById(Long commentId, UserPrincipal currentUser) {

        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        User user = userRepository.findById(comment.getCreatedBy()).orElseThrow(() -> new ResourceNotFoundException("User", "id", comment.getCreatedBy()));

        return ModelMapper.mapCommentToCommentResponse(comment, user);

    }

    @Override
    public PagedResponse<CommentResponse> getAllComment(UserPrincipal currentUser, int page, int size) {
        ValidatePageUtil.validatePageNumberAndSize(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "createdAt");
        Page<Comment> comments = commentRepository.findAll(pageable);

        if(comments.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), comments.getNumber(), comments.getSize(), comments.getTotalElements(), comments.getTotalPages(), comments.isLast());
        }

        Map<Long, User> creatorMap = getCreatorsIdsAncCreatorOfComments(comments.getContent());

        List<CommentResponse> commentResponses = comments.map(comment ->  {
            return ModelMapper.mapCommentToCommentResponse(comment, creatorMap.get(comment.getCreatedBy()));
        }).getContent();


        return new PagedResponse<>(commentResponses, comments.getNumber(), comments.getSize(), comments.getTotalElements(), comments.getTotalPages(), comments.isLast());
    }

    @Override
    public PagedResponse<CommentResponse> getCommentByCreatedBy(String username, UserPrincipal currentUser, int page, int size) {
       ValidatePageUtil.validatePageNumberAndSize(page, size);

       User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

       Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
       Page<Comment> comments = commentRepository.findByUsername(username, pageable);

       if(comments.getNumberOfElements() == 0) {
           return new PagedResponse<>(Collections.emptyList(), comments.getNumber(), comments.getSize(), comments.getTotalElements(), comments.getTotalPages(), comments.isLast());
       }

       List<Long> commentsIds = comments.map(Comment::getId).getContent();

       List<CommentResponse> commentResponses = comments.map(comment -> {
           return ModelMapper.mapCommentToCommentResponse(comment, user);
       }).getContent();


        return new PagedResponse<>(commentResponses, comments.getNumber(), comments.getSize(), comments.getTotalElements(), comments.getTotalPages(), comments.isLast());
    }

    @Override
    public PagedResponse<CommentResponse> getCommentByStory(Long storyId, UserPrincipal currentUser, int page, int size) {



        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Comment> comments = commentRepository.findByStoryId(storyId, pageable);

        if(comments.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), comments.getNumber(), comments.getSize(), comments.getTotalElements(), comments.getTotalPages(), comments.isLast());
        }

        List<Long> commentsIds = comments.map(Comment::getId).getContent();


        List<CommentResponse> commentResponses = comments.map(comment -> {

            Map<Long, User> creatorMap = getCreatorsIdsAncCreatorOfComments(comments.getContent());


            return ModelMapper.mapCommentToCommentResponse(comment, creatorMap.get(comment.getCreatedBy()));
        }).getContent();


        return new PagedResponse<>(commentResponses, comments.getNumber(), comments.getSize(), comments.getTotalElements(), comments.getTotalPages(), comments.isLast());

    }

    @Override
    public CommentResponse updateComment(CommentRequest commentRequest, Long commentId, UserPrincipal currentUser) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));



        long creationId = user.getId();

        long commentCreationId = comment.getCreatedBy();

        if(creationId == commentCreationId) {
            comment.setBody(commentRequest.getBody());
            commentRepository.save(comment);

            return ModelMapper.mapCommentToCommentResponse(comment,user );
        } else {
            throw new BadRequestException("Sorry you not created this comment");
        }
    }

    @Override
    public Map<Long, User> getCreatorsIdsAncCreatorOfComments(List<Comment> comments) {

        List<Long> creatorsIds = comments.stream().map(Comment::getCreatedBy).distinct().collect(Collectors.toList());

        List<User> creators = userRepository.findByIdIn(creatorsIds);

        Map<Long, User> creatorMap = creators.stream().collect(Collectors.toMap(User::getId, Function.identity()));

        return creatorMap;

    }

}
