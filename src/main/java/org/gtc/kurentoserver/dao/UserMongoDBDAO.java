package org.gtc.kurentoserver.dao;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.gtc.kurentoserver.api.UserDAO;
import org.gtc.kurentoserver.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Component
public class UserMongoDBDAO implements UserDAO {

    private final List<User> users = new ArrayList<>();

    @Value( "${mongo.host}" )
    private String host;
    @Value( "${mongo.user}" )
    private String user;
    @Value( "${mongo.password}" )
    private String password;
    @Value( "${mongo.database}" )
    private String databaseName;

    @Autowired
    private BCryptPasswordEncoder encoder;
    private MongoDatabase database;
    private MongoClient mongoClient;

    @PostConstruct
    public void connect() {
        String uri = "mongodb://"+host+":27017/?maxPoolSize=20&w=majority";
        mongoClient = MongoClients.create(uri);
        test(this.mongoClient.getDatabase(databaseName));

    }
    private void test(MongoDatabase database) throws MongoException{
        Bson command = new BsonDocument("ping", new BsonInt64(1));
        Document commandResult = database.runCommand(command);
    }

    @Override
    public User getUser(String username, String password) {
        reconnect();
        MongoCollection<Document> collection =  this.mongoClient.getDatabase(databaseName).getCollection("users");
        Bson projectionFields = Projections.fields(
                Projections.include("username", "password"),
                Projections.excludeId());
        Document doc = collection.find(eq("username", username))
                .projection(projectionFields)
                .first();


        if (doc != null && encoder.matches(password, doc.get("password").toString())) {
            return toModel(doc);
        }
        return null;
    }

    private void reconnect() {
        try {
            test(this.mongoClient.getDatabase(databaseName));
        } catch (Exception e) {
            connect();
        }
    }

    private User toModel(Document user) {
        return new User(user.get("username").toString(), user.get("password").toString());
    }
}
