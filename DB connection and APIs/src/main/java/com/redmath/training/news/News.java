package com.redmath.training.news;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class News {
    @Id
    private Long newsId;
    private String title;
    private String details;
    private String reportedBy;
    private LocalDateTime reportedAt;

    public News(long l, String title, String details, String reportedBy, LocalDateTime time) {
        this.newsId = l;
        this.title = title;
        this.details = details;
        this.reportedBy = reportedBy;
        this.reportedAt = time;
    }

}