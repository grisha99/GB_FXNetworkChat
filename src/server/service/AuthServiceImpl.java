package server.service;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.interf.AuthService;
import server.interf.UserDAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class AuthServiceImpl implements AuthService {

    private UserDAO userDAO;
    private static final Logger LOGGER = LogManager.getLogger(AuthServiceImpl.class.getName());

    public AuthServiceImpl(UserDAO userDAO) {

        this.userDAO = userDAO;
    }

    @Override
    public void start() {

        LOGGER.log(Level.INFO, "Сервер авторизации запущен");
    }

    @Override
    public String getNick(String login, String pass) {

        return userDAO.getUserNick(login, pass);
    }

    @Override
    public void stop() {

        LOGGER.log(Level.INFO, "Сервис авторизации остановлен");
    }
}
