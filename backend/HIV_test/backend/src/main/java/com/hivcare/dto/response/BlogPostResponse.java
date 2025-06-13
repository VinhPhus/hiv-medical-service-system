package com.hivcare.dto.response;

import com.hivcare.enums.BlogCategory;
import com.hivcare.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostResponse {
    
    private Long id;
    private String title;
    private String excerpt;
    private String content;
    private String authorName;
    private Boolean isAnonymous;
    private BlogCategory category;
    private PostStatus status;
    private String featuredImage;
    private Integer viewsCount;
    private Integer likesCount;
    private Integer commentsCount;
    private Integer readTime;
    private Boolean isFeatured;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
