package com.spring.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class BlogContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_content_id")
    private Long id;
    private String nickname;
    private String title;
    private String content;
    private int viewer;
    private int likes;
    private String cover;
    private boolean state;
    private LocalDateTime writeDateTime;
}
