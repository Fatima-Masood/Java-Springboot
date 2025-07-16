package com.redmath.training.news;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class NewsController {
    private static final Logger logData = LoggerFactory.getLogger(NewsController.class);
    private final NewsRepository newsRepository;

    public NewsController(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @GetMapping("/api/v1/news/{newsId}")
    public ResponseEntity<News> get(@PathVariable("newsId") Long newsId) {
        Optional<News> news = newsRepository.findById(newsId);
        if (news.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(news.get());
    }

    @GetMapping("/api/v1/news")
    public ResponseEntity<Page<News>> get(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "reportedBy", required = false) String reportedBy,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Page<News> news;
        Pageable pageable = PageRequest.of(page, size);
        if (title != null && !title.isEmpty()) {
            news = newsRepository.findByTitleLike("%" + title + "%", pageable);
        } else if(reportedBy != null && !reportedBy.isEmpty()){
            news = newsRepository.findByReportedByLike("%" + reportedBy + "%", pageable);
        }else {
            logData.info("Returning all news!");
            news = newsRepository.findAll(pageable);
        }

        if (news.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(news);
    }


    @PostMapping("/api/v1/news")
    public ResponseEntity<News> post(@RequestBody News request) {
        News news = new News(
                System.currentTimeMillis(),
                request.getTitle(),
                request.getDetails(),
                request.getReportedBy(),
                LocalDateTime.now()
        );

        News saved = newsRepository.save(news);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/api/v1/news/{newsId}")
    public ResponseEntity<News> update(@PathVariable("newsId") Long newsId, @RequestBody News updatedNews) {
        Optional<News> existing = newsRepository.findById(newsId);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        News news = existing.get();
        news.setTitle(updatedNews.getTitle());
        news.setDetails(updatedNews.getDetails());
        news.setReportedBy(updatedNews.getReportedBy());
        news.setReportedAt(LocalDateTime.now());

        News saved = newsRepository.save(news);
        return ResponseEntity.ok(saved);
    }



}