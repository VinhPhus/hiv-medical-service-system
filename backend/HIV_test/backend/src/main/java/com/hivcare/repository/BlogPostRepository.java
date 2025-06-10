package com.hivcare.repository;

import com.hivcare.entity.BlogPost;
import com.hivcare.enums.BlogCategory;
import com.hivcare.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    
    List<BlogPost> findByStatusOrderByCreatedAtDesc(PostStatus status);
    
    Page<BlogPost> findByStatusOrderByCreatedAtDesc(PostStatus status, Pageable pageable);
    
    List<BlogPost> findByCategoryAndStatusOrderByCreatedAtDesc(BlogCategory category, PostStatus status);
    
    Page<BlogPost> findByCategoryAndStatusOrderByCreatedAtDesc(BlogCategory category, PostStatus status, Pageable pageable);
    
    @Query("SELECT bp FROM BlogPost bp WHERE bp.title LIKE %:keyword% OR bp.content LIKE %:keyword% AND bp.status = :status ORDER BY bp.createdAt DESC")
    List<BlogPost> searchByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") PostStatus status);
    
    @Query("SELECT bp FROM BlogPost bp WHERE bp.title LIKE %:keyword% OR bp.content LIKE %:keyword% AND bp.status = :status ORDER BY bp.createdAt DESC")
    Page<BlogPost> searchByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") PostStatus status, Pageable pageable);
    
    List<BlogPost> findByAuthorIdAndStatusOrderByCreatedAtDesc(Long authorId, PostStatus status);
    
    @Query("SELECT bp FROM BlogPost bp WHERE bp.createdAt BETWEEN :startDate AND :endDate AND bp.status = :status ORDER BY bp.createdAt DESC")
    List<BlogPost> findByDateRangeAndStatus(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate, 
                                           @Param("status") PostStatus status);
    
    @Query("SELECT COUNT(bp) FROM BlogPost bp WHERE bp.status = :status")
    Long countByStatus(@Param("status") PostStatus status);
    
    @Query("SELECT COUNT(bp) FROM BlogPost bp WHERE bp.category = :category AND bp.status = :status")
    Long countByCategoryAndStatus(@Param("category") BlogCategory category, @Param("status") PostStatus status);
}
