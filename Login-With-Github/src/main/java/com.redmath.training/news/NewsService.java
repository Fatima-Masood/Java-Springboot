package com.redmath.training.news;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class NewsService {

    private final NewsRepository newsRepository;

    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public Optional<News> findById(Long newsId) {
        return newsRepository.findById(newsId);
    }

    public List<News> findAll(Integer page, Integer size) {
        if (page < 0) {
            page = 0;
        }
        if (size > 1000) {
            size = 1000;
        }
        return newsRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    public News create(News news) {
        news.setNewsId(System.currentTimeMillis());
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        news.setReportedBy(username);
        news.setReportedAt(LocalDateTime.now());
        return newsRepository.save(news);
    }

    public Optional<News> update(Long newsId, News news) {
        Optional<News> existing = newsRepository.findById(newsId);
        if (existing.isPresent()) {
            existing.get().setTitle(news.getTitle());
            existing.get().setDetails(news.getDetails());
            existing = Optional.of(newsRepository.save(existing.get()));
        }
        return existing;
    }
}
