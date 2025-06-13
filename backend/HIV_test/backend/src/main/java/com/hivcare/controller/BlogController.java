package com.hivcare.controller;

import com.hivcare.dto.response.BlogPostResponse;
import com.hivcare.enums.BlogCategory;
import com.hivcare.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/blog")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class BlogController {

    private final BlogService blogService;

    @GetMapping("/public")
    public ResponseEntity<Page<BlogPostResponse>> getPublicBlogPosts(Pageable pageable) {
        Page<BlogPostResponse> posts = blogService.getPublishedPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/public/category/{category}")
    public ResponseEntity<List<BlogPostResponse>> getPostsByCategory(@PathVariable BlogCategory category) {
        List<BlogPostResponse> posts = blogService.getPostsByCategory(category);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<BlogPostResponse> getBlogPost(@PathVariable Long id) {
        BlogPostResponse post = blogService.getPublishedPost(id);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/public/search")
    public ResponseEntity<List<BlogPostResponse>> searchPosts(@RequestParam String keyword) {
        List<BlogPostResponse> posts = blogService.searchPosts(keyword);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/public/featured")
    public ResponseEntity<List<BlogPostResponse>> getFeaturedPosts() {
        List<BlogPostResponse> posts = blogService.getFeaturedPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/public/recent")
    public ResponseEntity<List<BlogPostResponse>> getRecentPosts(@RequestParam(defaultValue = "5") int limit) {
        List<BlogPostResponse> posts = blogService.getRecentPosts(limit);
        return ResponseEntity.ok(posts);
    }
}