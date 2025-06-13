package com.hivcare.service;

import com.hivcare.dto.response.BlogPostResponse;
import com.hivcare.enums.BlogCategory;
import com.hivcare.enums.PostStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BlogService {

    public Page<BlogPostResponse> getPublishedPosts(Pageable pageable) {
        List<BlogPostResponse> allPosts = getMockBlogPosts();
        List<BlogPostResponse> publishedPosts = allPosts.stream()
                .filter(post -> post.getStatus() == PostStatus.PUBLISHED)
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), publishedPosts.size());
        
        List<BlogPostResponse> pageContent = publishedPosts.subList(start, end);
        return new PageImpl<>(pageContent, pageable, publishedPosts.size());
    }

    public List<BlogPostResponse> getPostsByCategory(BlogCategory category) {
        return getMockBlogPosts().stream()
                .filter(post -> post.getCategory() == category && post.getStatus() == PostStatus.PUBLISHED)
                .collect(Collectors.toList());
    }

    public BlogPostResponse getPublishedPost(Long id) {
        return getMockBlogPosts().stream()
                .filter(post -> post.getId().equals(id) && post.getStatus() == PostStatus.PUBLISHED)
                .findFirst()
                .orElse(null);
    }

    public List<BlogPostResponse> searchPosts(String keyword) {
        return getMockBlogPosts().stream()
                .filter(post -> post.getStatus() == PostStatus.PUBLISHED)
                .filter(post -> post.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                               post.getContent().toLowerCase().contains(keyword.toLowerCase()) ||
                               post.getExcerpt().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<BlogPostResponse> getFeaturedPosts() {
        return getMockBlogPosts().stream()
                .filter(post -> post.getStatus() == PostStatus.PUBLISHED && post.getIsFeatured())
                .collect(Collectors.toList());
    }

    public List<BlogPostResponse> getRecentPosts(int limit) {
        return getMockBlogPosts().stream()
                .filter(post -> post.getStatus() == PostStatus.PUBLISHED)
                .filter(post -> post.getCreatedAt().isAfter(LocalDateTime.now().minusDays(30)))
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<BlogPostResponse> getMockBlogPosts() {
        return Arrays.asList(
            BlogPostResponse.builder()
                .id(1L)
                .title("Sống tích cực với HIV: Câu chuyện của tôi")
                .excerpt("Chia sẻ hành trình vượt qua khó khăn và sống tích cực với HIV")
                .content("Khi được chẩn đoán HIV, tôi đã trải qua nhiều cảm xúc khác nhau. Từ sốc, sợ hãi đến tuyệt vọng. Nhưng với sự hỗ trợ từ gia đình, bạn bè và đội ngũ y tế, tôi đã học cách sống tích cực với tình trạng này...")
                .authorName("Nguyễn Văn A")
                .isAnonymous(true)
                .category(BlogCategory.STORIES)
                .status(PostStatus.PUBLISHED)
                .featuredImage("/images/blog/positive-living.jpg")
                .viewsCount(1250)
                .likesCount(89)
                .commentsCount(23)
                .readTime(5)
                .isFeatured(true)
                .publishedAt(LocalDateTime.now().minusDays(2))
                .createdAt(LocalDateTime.now().minusDays(3))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build(),

            BlogPostResponse.builder()
                .id(2L)
                .title("10 mẹo để tuân thủ điều trị ARV hiệu quả")
                .excerpt("Những lời khuyên thực tế giúp bạn duy trì việc uống thuốc ARV đều đặn")
                .content("Việc tuân thủ điều trị ARV là rất quan trọng để kiểm soát HIV. Dưới đây là 10 mẹo hữu ích: 1. Đặt báo thức nhắc nhở, 2. Sử dụng hộp thuốc theo ngày...")
                .authorName("BS. Trần Thị Lan")
                .isAnonymous(false)
                .category(BlogCategory.TIPS)
                .status(PostStatus.PUBLISHED)
                .featuredImage("/images/blog/arv-tips.jpg")
                .viewsCount(2100)
                .likesCount(156)
                .commentsCount(45)
                .readTime(7)
                .isFeatured(true)
                .publishedAt(LocalDateTime.now().minusDays(5))
                .createdAt(LocalDateTime.now().minusDays(6))
                .updatedAt(LocalDateTime.now().minusDays(4))
                .build(),

            BlogPostResponse.builder()
                .id(3L)
                .title("Hiểu về xét nghiệm CD4 và Viral Load")
                .excerpt("Tầm quan trọng của việc theo dõi CD4 và Viral Load trong điều trị HIV")
                .content("CD4 và Viral Load là hai chỉ số quan trọng nhất trong việc theo dõi tình trạng HIV. CD4 cho biết tình trạng hệ miễn dịch, còn Viral Load cho biết lượng virus trong máu...")
                .authorName("BS. Lê Hoàng Nam")
                .isAnonymous(false)
                .category(BlogCategory.MEDICAL)
                .status(PostStatus.PUBLISHED)
                .featuredImage("/images/blog/cd4-viral-load.jpg")
                .viewsCount(1800)
                .likesCount(134)
                .commentsCount(67)
                .readTime(8)
                .isFeatured(false)
                .publishedAt(LocalDateTime.now().minusDays(7))
                .createdAt(LocalDateTime.now().minusDays(8))
                .updatedAt(LocalDateTime.now().minusDays(6))
                .build(),

            BlogPostResponse.builder()
                .id(4L)
                .title("Tìm kiếm hỗ trợ tâm lý khi sống với HIV")
                .excerpt("Các nguồn hỗ trợ tâm lý và cộng đồng dành cho người sống với HIV")
                .content("Sống với HIV không chỉ là thách thức về mặt y tế mà còn về mặt tâm lý. Việc tìm kiếm hỗ trợ từ chuyên gia tâm lý, nhóm hỗ trợ và cộng đồng là rất quan trọng...")
                .authorName("Tâm lý viên Phạm Thị Mai")
                .isAnonymous(false)
                .category(BlogCategory.SUPPORT)
                .status(PostStatus.PUBLISHED)
                .featuredImage("/images/blog/psychological-support.jpg")
                .viewsCount(950)
                .likesCount(78)
                .commentsCount(34)
                .readTime(6)
                .isFeatured(false)
                .publishedAt(LocalDateTime.now().minusDays(10))
                .createdAt(LocalDateTime.now().minusDays(11))
                .updatedAt(LocalDateTime.now().minusDays(9))
                .build(),

            BlogPostResponse.builder()
                .id(5L)
                .title("Dinh dưỡng và lối sống lành mạnh cho người HIV+")
                .excerpt("Hướng dẫn về chế độ ăn uống và lối sống giúp tăng cường sức khỏe")
                .content("Một chế độ dinh dưỡng cân bằng và lối sống lành mạnh đóng vai trò quan trọng trong việc duy trì sức khỏe cho người sống với HIV. Bao gồm: ăn đủ chất, tập thể dục đều đặn...")
                .authorName("Chuyên gia dinh dưỡng Nguyễn Thị Hoa")
                .isAnonymous(false)
                .category(BlogCategory.LIFESTYLE)
                .status(PostStatus.PUBLISHED)
                .featuredImage("/images/blog/healthy-lifestyle.jpg")
                .viewsCount(1650)
                .likesCount(112)
                .commentsCount(28)
                .readTime(9)
                .isFeatured(true)
                .publishedAt(LocalDateTime.now().minusDays(12))
                .createdAt(LocalDateTime.now().minusDays(13))
                .updatedAt(LocalDateTime.now().minusDays(11))
                .build()
        );
    }
}