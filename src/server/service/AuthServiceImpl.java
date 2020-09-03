package server.service;

import server.interf.AuthService;
import server.interf.UserDAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class AuthServiceImpl implements AuthService {

    public class UserEntity {
        private String login;
        private String pass;
        private String nick;

        public UserEntity (String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;


        }

        public UserEntity(ResultSet rs) {
            try {
                if (rs != null && rs.getRow() > 0 ) {
                    this.login = rs.getString("login");
                    this.pass = rs.getString("pass");
                    this.nick = rs.getString("nick");
                }
            } catch (SQLException throwables) {
                System.out.println("Ошибка доступа к данным пользователя");
                throwables.printStackTrace();
            }

        }
    }

    private List<UserEntity> users;

    public AuthServiceImpl(UserDAO userDAO) {
        users = new LinkedList<>();

        ResultSet rs = userDAO.getUserSet();
        try {
            while (rs.next()) {
                users.add(new UserEntity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        System.out.println("Сервис авторизации запущен");
    }

    @Override
    public String getNick(String login, String pass) {
        for (UserEntity ue : users) {
            if (ue.login.equals(login) && ue.pass.equals(pass)) {
                return ue.nick;
            }
        }
        return null;
    }

    @Override
    public void stop() {
        System.out.println("Сервис авторизации остановлен");
    }
}
