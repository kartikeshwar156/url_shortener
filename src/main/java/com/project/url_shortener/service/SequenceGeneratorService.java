package com.project.url_shortener.service;

import com.project.url_shortener.model.SequenceCounter;
import com.project.url_shortener.util.Base62Codec;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class SequenceGeneratorService {

    private static final String SEQUENCE_ID = "url_short_code";

    private final MongoOperations mongoOperations;

    public SequenceGeneratorService(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    public String nextShortCode() {
        Query query = new Query(Criteria.where("_id").is(SEQUENCE_ID));
        Update update = new Update().inc("value", 1);
        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true).upsert(true);

        SequenceCounter counter = mongoOperations.findAndModify(query, update, options, SequenceCounter.class);
        if (counter == null) {
            throw new IllegalStateException("Failed to generate sequence value");
        }
        return Base62Codec.encode(counter.getValue());
    }
}
