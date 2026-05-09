package com.axis.hraiportal.repository.mongo;

import com.axis.hraiportal.domain.resume.ResumeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository
        extends MongoRepository<ResumeDocument, String> {

    // Spring auto-generates all these queries
    // you just define the method signature

    // find by filename
    Optional<ResumeDocument> findByFilename(String filename);

    // find all resumes for a candidate
    List<ResumeDocument> findByPineconeVectorId(String pineconeVectorId);

    // check if resume already exists (duplicate detection)
    boolean existsByFilename(String filename);

    // custom query — find resumes that have a specific skill
    @Query("{ 'skills': { $in: [?0] } }")
    List<ResumeDocument> findBySkill(String skill);

    // find resumes uploaded after a certain date
    @Query("{ 'uploaded_at': { $gte: ?0 } }")
    List<ResumeDocument> findRecentResumes(String date);
}