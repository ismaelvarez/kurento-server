package org.gtc.kurentoserver.dao;

import com.google.common.collect.Lists;
import org.gtc.kurentoserver.entities.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDAO {
    private final List<User> users;

    public UserDAO() {
        this.users = readUsers();
    }

    private List<User> readUsers() {
        /*
            String expectedData = "Hello, world!";

            FileInputStream fis = new FileInputStream("src/test/resources/fileTest.txt");
            String data = IOUtils.toString(fis, "UTF-8");

            assertEquals(expectedData, data.trim());
         */
        return Lists.newArrayList(new User("admin", "admin"));
    }

    public User getUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)){
                return user;
            }
        }
        return null;
    }
}
