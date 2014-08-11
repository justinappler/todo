package com.jappler.todo.search;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.IndicesExists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchService {
    
    private static Logger logger;
    
    private static SearchService instance;
    
    @SuppressWarnings("rawtypes")
    private Map<String,SearchIndex> indices;
    
    private JestClient client;
    
    private SearchService() {
        // Configuration
        HttpClientConfig clientConfig = new HttpClientConfig
                .Builder(System.getenv("SEARCHLY_WHATEVER"))
                .multiThreaded(true)
                .build();
        
        // Construct a new Jest client according to configuration via factory
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(clientConfig);
        
        client = factory.getObject();
        
        logger = LoggerFactory.getLogger(SearchService.class);
    }
    
    public static SearchService getInstance() {
        if (instance == null) {
            instance = new SearchService();
        }
        
        return instance;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> SearchIndex<T> getIndex(Class<T> clazz, String type, String indexName) {
        if (indices == null) {
            indices = new HashMap<String,SearchIndex>();
        }
        
        String key = type + "|" + indexName;
        SearchIndex<T> index = indices.get(key);
        if (index == null) {
            index = new SearchIndex<T>(clazz, client, type, indexName);
            indices.put(key, index);
        }
        
        return index;
    }
    
    public static class SearchIndex<T> {
        private Class<T> clazz;
        
        private String indexType;
        private String indexName;
        
        private JestClient client;

        public SearchIndex(Class<T> clazz, JestClient client, String type, String index) {
            this.clazz = clazz;
            this.client = client;
            this.indexType = type;
            this.indexName = index;
            
            try {
                IndicesExists indicesExists = new IndicesExists.Builder(indexName).build();
                JestResult result = client.execute(indicesExists);
                
                if (!result.isSucceeded()) {
                    // Create articles index
                    CreateIndex createIndex = new CreateIndex.Builder(indexName).build();
                    result = client.execute(createIndex);
                    
                    if (!result.isSucceeded()) {
                        logger.error("Error creating index: " + result.getErrorMessage());
                    }
                }
            } catch (Exception exception) {
                logger.error("Error creating index: " + exception.getMessage());
            }
        }

        public void index(T obj) {
            Index index = new Index.Builder(obj).index(indexName).type(indexType).build();
            try {
                JestResult result = client.execute(index);
                
                if (!result.isSucceeded()) {
                    logger.error("Indexing failed: " + result.getErrorMessage());
                }
            } catch (Exception e) {
                logger.error("Error indexing object: " + e.getMessage());
            }
        }
        
        public List<T> query(String query) {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.queryString(query));
             
            Search search = (Search) new Search.Builder(searchSourceBuilder.toString())
                                            // multiple index or types can be added.
                                            .addIndex(indexName)
                                            .addType(indexType)
                                            .build();
            JestResult result = null;
            try {
                result = client.execute(search);
                
                if (!result.isSucceeded()) {
                    logger.error("Error searching" + result.getErrorMessage());
                }
            } catch (Exception e) {
                logger.error("Error searching" + e.getMessage());
            }
            
            return result.getSourceAsObjectList(clazz);
        }
    }
}
