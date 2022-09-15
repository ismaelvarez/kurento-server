package org.gtc.kurentoserver.dao;

import org.apache.commons.io.IOUtils;
import org.gtc.kurentoserver.api.UserDAO;
import org.gtc.kurentoserver.model.User;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class UserLocalSessionDAO implements UserDAO {

    private final List<User> users = new ArrayList<>();

    @Value( "${server.users}" )
    private String userListPath;

    @PostConstruct
    private void getUsers() {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(userListPath);
            String[] lines = IOUtils.toString(fis, StandardCharsets.UTF_8).split("\n");
            for (String line : lines) {
                String[] split = line.split(",");
                users.add(new User(split[0], split[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public User getUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)){
                return user;
            }
        }
        return null;
    }
}
