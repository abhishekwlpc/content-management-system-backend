package org.example.content_upload_system.repository;

import org.example.content_upload_system.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    List<Content> findByOwnerId(Integer ownerId);
}
