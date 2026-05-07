package com.axis.hraiportal.client;

import com.axis.hraiportal.properties.PineconeProperties;
import io.pinecone.clients.Index;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class PineconeClient {

    private final Index index;
    private final PineconeProperties props;

    public PineconeClient(Index index, PineconeProperties props) {
        this.index = index;
        this.props = props;
    }

    // ── Upsert resume embedding ──────────────────────────────
    // Called after HuggingFaceClient.embed() returns a vector
    public void upsert(String resumeId, List<Float> embedding) {
        index.upsert(
                resumeId,
                embedding,
                null,
                null,
                null,
                props.getNamespace()
        );
        log.info("Pinecone upsert → resumeId={}, dim={}",
                resumeId, embedding.size());
    }

    // ── Query top-K similar resumes for a JD embedding ───────
    public QueryResponseWithUnsignedIndices querySimilar(
            List<Float> jdEmbedding, int topK) {
        log.debug("Pinecone query → topK={}, namespace={}",
                topK, props.getNamespace());
        return index.query(
                topK,
                jdEmbedding,
                null,
                null,
                null,
                props.getNamespace(),
                null,
                true,
                true
        );
    }
}