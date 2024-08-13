package org.example.repository;

import org.example.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnRepo extends JpaRepository<Announcement, Integer> {

    List<Announcement> findAllByAuthorId(int authorId);
}
