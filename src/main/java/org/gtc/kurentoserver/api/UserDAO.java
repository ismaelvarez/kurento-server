package org.gtc.kurentoserver.api;

import org.gtc.kurentoserver.model.User;

public interface UserDAO {
    User getUser(String username, String password);
}
