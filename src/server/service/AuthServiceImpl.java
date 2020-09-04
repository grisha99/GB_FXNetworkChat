package server.service;

import server.interf.AuthService;
import server.interf.UserDAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class AuthServiceImpl implements AuthService {

    private UserDAO userDAO;

    public AuthServiceImpl(UserDAO userDAO) {

        this.userDAO = userDAO;
    }

    @Override
    public void start() {

        System.out.println("Сервис авторизации запущен");
    }

    @Override
    public String getNick(String login, String pass) {

        return userDAO.getUserNick(login, pass);
    }

    @Override
    public void stop() {
        System.out.println("Сервис авторизации остановлен");
    }
}
