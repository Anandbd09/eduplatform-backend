package com.eduplatform.social.service;

import com.eduplatform.social.model.*;
import com.eduplatform.social.repository.*;
import com.eduplatform.social.dto.*;
import com.eduplatform.social.exception.SocialException;
import com.eduplatform.social.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@Transactional
public class SocialService {

    @Autowired
    private UserFollowRepository followRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ForumThreadRepository threadRepository;

    @Autowired
    private ForumPostRepository postRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private FollowService followService;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private ForumService forumService;

    /**
     * FOLLOW USER
     */
    public UserFollowResponse followUser(String followingId, String userId, String tenantId) {
        try {
            SocialValidator.validateUserId(userId);
            SocialValidator.validateUserId(followingId);

            if (userId.equals(followingId)) {
                throw new SocialException("Cannot follow yourself");
            }

            // Check if already following
            Optional<UserFollow> existing = followRepository
                    .findByFollowerIdAndFollowingIdAndTenantId(userId, followingId, tenantId);

            if (existing.isPresent() && existing.get().isActive()) {
                throw new SocialException("Already following this user");
            }

            UserFollow follow = UserFollow.builder()
                    .id(UUID.randomUUID().toString())
                    .followerId(userId)
                    .followingId(followingId)
                    .relationshipKey(userId + ":" + followingId)
                    .status("ACTIVE")
                    .isPublic(true)
                    .notificationsEnabled(true)
                    .followedAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();

            followRepository.save(follow);

            // Send notification
            NotificationHelper.notifyUserFollowed(followingId, userId, tenantId);

            log.info("User followed: follower={}, following={}", userId, followingId);

            return UserFollowResponse.builder()
                    .id(follow.getId())
                    .followerId(follow.getFollowerId())
                    .followingId(follow.getFollowingId())
                    .status(follow.getStatus())
                    .followedAt(follow.getFollowedAt())
                    .build();

        } catch (SocialException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error following user", e);
            throw new SocialException("Failed to follow user");
        }
    }

    /**
     * UNFOLLOW USER
     */
    public void unfollowUser(String followingId, String userId, String tenantId) {
        try {
            Optional<UserFollow> follow = followRepository
                    .findByFollowerIdAndFollowingIdAndTenantId(userId, followingId, tenantId);

            if (follow.isPresent()) {
                UserFollow f = follow.get();
                f.setStatus("INACTIVE");
                f.setUnfollowedAt(LocalDateTime.now());
                followRepository.save(f);

                log.info("User unfollowed: follower={}, following={}", userId, followingId);
            }

        } catch (Exception e) {
            log.error("Error unfollowing user", e);
            throw new SocialException("Failed to unfollow user");
        }
    }

    /**
     * GET FOLLOWING LIST
     */
    public Page<UserFollowResponse> getFollowing(String userId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("followedAt").descending());

            Page<UserFollow> follows = followRepository
                    .findByFollowerIdAndStatusAndTenantId(userId, "ACTIVE", tenantId, pageable);

            return follows.map(f -> UserFollowResponse.builder()
                    .id(f.getId())
                    .followerId(f.getFollowerId())
                    .followingId(f.getFollowingId())
                    .status(f.getStatus())
                    .followedAt(f.getFollowedAt())
                    .build());

        } catch (Exception e) {
            log.error("Error fetching following list", e);
            throw new SocialException("Failed to fetch following list");
        }
    }

    /**
     * GET FOLLOWERS LIST
     */
    public Page<UserFollowResponse> getFollowers(String userId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("followedAt").descending());

            Page<UserFollow> follows = followRepository
                    .findByFollowingIdAndStatusAndTenantId(userId, "ACTIVE", tenantId, pageable);

            return follows.map(f -> UserFollowResponse.builder()
                    .id(f.getId())
                    .followerId(f.getFollowerId())
                    .followingId(f.getFollowingId())
                    .status(f.getStatus())
                    .followedAt(f.getFollowedAt())
                    .build());

        } catch (Exception e) {
            log.error("Error fetching followers list", e);
            throw new SocialException("Failed to fetch followers list");
        }
    }

    /**
     * SEND MESSAGE
     */
    public MessageResponse sendMessage(MessageRequest request, String senderId, String tenantId) {
        try {
            ContentValidator.validateMessage(request);

            Message message = Message.builder()
                    .id(UUID.randomUUID().toString())
                    .senderId(senderId)
                    .recipientId(request.getRecipientId())
                    .subject(request.getSubject())
                    .content(request.getContent())
                    .status("SENT")
                    .sentAt(LocalDateTime.now())
                    .isReply(false)
                    .tenantId(tenantId)
                    .build();

            messageRepository.save(message);

            // Send notification
            NotificationHelper.notifyNewMessage(request.getRecipientId(), senderId, tenantId);

            log.info("Message sent: from={}, to={}", senderId, request.getRecipientId());

            return MessageResponse.builder()
                    .id(message.getId())
                    .senderId(message.getSenderId())
                    .recipientId(message.getRecipientId())
                    .subject(message.getSubject())
                    .status(message.getStatus())
                    .sentAt(message.getSentAt())
                    .build();

        } catch (Exception e) {
            log.error("Error sending message", e);
            throw new SocialException("Failed to send message");
        }
    }

    /**
     * GET INBOX
     */
    public Page<MessageResponse> getInbox(String userId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());

            Page<Message> messages = messageRepository
                    .findByRecipientIdAndStatusAndTenantId(userId, "SENT", tenantId, pageable);

            return messages.map(m -> MessageResponse.builder()
                    .id(m.getId())
                    .senderId(m.getSenderId())
                    .recipientId(m.getRecipientId())
                    .subject(m.getSubject())
                    .status(m.getStatus())
                    .sentAt(m.getSentAt())
                    .readAt(m.getReadAt())
                    .build());

        } catch (Exception e) {
            log.error("Error fetching inbox", e);
            throw new SocialException("Failed to fetch inbox");
        }
    }

    /**
     * MARK MESSAGE AS READ
     */
    public void markMessageAsRead(String messageId, String tenantId) {
        try {
            Optional<Message> msg = messageRepository.findById(messageId);

            if (msg.isPresent()) {
                Message m = msg.get();
                m.setStatus("READ");
                m.setReadAt(LocalDateTime.now());
                messageRepository.save(m);
            }

        } catch (Exception e) {
            log.error("Error marking message as read", e);
        }
    }

    /**
     * CREATE FORUM THREAD
     */
    public ForumThreadResponse createForumThread(ForumThreadRequest request, String userId, String tenantId) {
        try {
            ContentValidator.validateForumThread(request);

            ForumThread thread = ForumThread.builder()
                    .id(UUID.randomUUID().toString())
                    .courseId(request.getCourseId())
                    .creatorId(userId)
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .category(request.getCategory())
                    .status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .viewCount(0L)
                    .replyCount(0L)
                    .isPinned(false)
                    .isLocked(false)
                    .tenantId(tenantId)
                    .build();

            threadRepository.save(thread);

            log.info("Forum thread created: threadId={}, courseId={}", thread.getId(), request.getCourseId());

            return ForumThreadResponse.builder()
                    .id(thread.getId())
                    .courseId(thread.getCourseId())
                    .title(thread.getTitle())
                    .category(thread.getCategory())
                    .status(thread.getStatus())
                    .createdAt(thread.getCreatedAt())
                    .replyCount(thread.getReplyCount())
                    .build();

        } catch (Exception e) {
            log.error("Error creating forum thread", e);
            throw new SocialException("Failed to create forum thread");
        }
    }

    /**
     * POST FORUM REPLY
     */
    public ForumPostResponse postReply(ForumPostRequest request, String userId, String tenantId) {
        try {
            ContentValidator.validateForumPost(request);

            // Parse mentions
            String[] mentions = MentionParser.extractMentions(request.getContent());

            ForumPost post = ForumPost.builder()
                    .id(UUID.randomUUID().toString())
                    .threadId(request.getThreadId())
                    .authorId(userId)
                    .content(request.getContent())
                    .isAnswer(false)
                    .createdAt(LocalDateTime.now())
                    .likeCount(0L)
                    .replyCount(0L)
                    .mentions(mentions)
                    .isEdited(false)
                    .editCount(0)
                    .tenantId(tenantId)
                    .build();

            postRepository.save(post);

            // Update thread reply count
            Optional<ForumThread> thread = threadRepository.findById(request.getThreadId());
            if (thread.isPresent()) {
                ForumThread t = thread.get();
                t.setReplyCount((t.getReplyCount() != null ? t.getReplyCount() : 0) + 1);
                t.setLastActivityAt(LocalDateTime.now());
                threadRepository.save(t);
            }

            // Notify mentioned users
            for (String mention : mentions) {
                NotificationHelper.notifyMention(mention, userId, tenantId);
            }

            log.info("Forum post created: postId={}, threadId={}", post.getId(), request.getThreadId());

            return ForumPostResponse.builder()
                    .id(post.getId())
                    .threadId(post.getThreadId())
                    .authorId(post.getAuthorId())
                    .content(post.getContent())
                    .createdAt(post.getCreatedAt())
                    .likeCount(post.getLikeCount())
                    .build();

        } catch (Exception e) {
            log.error("Error posting forum reply", e);
            throw new SocialException("Failed to post forum reply");
        }
    }

    /**
     * GET FORUM THREAD POSTS
     */
    public Page<ForumPostResponse> getThreadPosts(String threadId, int page, int size, String tenantId) {
        try {
            page = Math.max(page, 0);
            size = Math.min(Math.max(size, 1), 100);
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());

            Page<ForumPost> posts = postRepository
                    .findByThreadIdAndTenantId(threadId, tenantId, pageable);

            return posts.map(p -> ForumPostResponse.builder()
                    .id(p.getId())
                    .threadId(p.getThreadId())
                    .authorId(p.getAuthorId())
                    .content(p.getContent())
                    .createdAt(p.getCreatedAt())
                    .likeCount(p.getLikeCount())
                    .isAnswer(p.getIsAnswer())
                    .build());

        } catch (Exception e) {
            log.error("Error fetching thread posts", e);
            throw new SocialException("Failed to fetch thread posts");
        }
    }

    /**
     * LIKE CONTENT
     */
    public void likeContent(LikeRequest request, String userId, String tenantId) {
        try {
            Optional<Like> existing = likeRepository
                    .findByUserIdAndContentIdAndTenantId(userId, request.getContentId(), tenantId);

            if (existing.isPresent()) {
                throw new SocialException("Already liked this content");
            }

            Like like = Like.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .contentType(request.getContentType())
                    .contentId(request.getContentId())
                    .likeKey(userId + ":" + request.getContentId())
                    .likedAt(LocalDateTime.now())
                    .tenantId(tenantId)
                    .build();

            likeRepository.save(like);

            // Update like count on content
            if ("POST".equals(request.getContentType())) {
                Optional<ForumPost> post = postRepository.findById(request.getContentId());
                if (post.isPresent()) {
                    ForumPost p = post.get();
                    p.setLikeCount((p.getLikeCount() != null ? p.getLikeCount() : 0) + 1);
                    postRepository.save(p);
                }
            }

            log.info("Content liked: userId={}, contentId={}", userId, request.getContentId());

        } catch (SocialException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error liking content", e);
            throw new SocialException("Failed to like content");
        }
    }

    /**
     * UNLIKE CONTENT
     */
    public void unlikeContent(String contentId, String userId, String tenantId) {
        try {
            Optional<Like> like = likeRepository
                    .findByUserIdAndContentIdAndTenantId(userId, contentId, tenantId);

            if (like.isPresent()) {
                likeRepository.delete(like.get());

                // Update like count
                Optional<ForumPost> post = postRepository.findById(contentId);
                if (post.isPresent()) {
                    ForumPost p = post.get();
                    p.setLikeCount(Math.max(0, (p.getLikeCount() != null ? p.getLikeCount() : 1) - 1));
                    postRepository.save(p);
                }
            }

        } catch (Exception e) {
            log.error("Error unliking content", e);
            throw new SocialException("Failed to unlike content");
        }
    }

    /**
     * GET FOLLOWER COUNT
     */
    public Long getFollowerCount(String userId, String tenantId) {
        try {
            return followRepository.countByFollowingIdAndStatusAndTenantId(userId, "ACTIVE", tenantId);
        } catch (Exception e) {
            log.error("Error getting follower count", e);
            return 0L;
        }
    }

    /**
     * GET FOLLOWING COUNT
     */
    public Long getFollowingCount(String userId, String tenantId) {
        try {
            return followRepository.countByFollowerIdAndStatusAndTenantId(userId, "ACTIVE", tenantId);
        } catch (Exception e) {
            log.error("Error getting following count", e);
            return 0L;
        }
    }

    /**
     * GET UNREAD MESSAGE COUNT
     */
    public Long getUnreadMessageCount(String userId, String tenantId) {
        try {
            return messageRepository.countByRecipientIdAndStatusAndTenantId(userId, "SENT", tenantId);
        } catch (Exception e) {
            log.error("Error getting unread message count", e);
            return 0L;
        }
    }
}