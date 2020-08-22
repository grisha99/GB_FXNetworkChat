package server.service;

import server.interf.AuthService;

import java.util.LinkedList;
import java.util.List;

public class AuthServiceImpl implements AuthService {

    private class UserEntity {
        private String login;
        private String pass;
        private String nick;

        public UserEntity (String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;


        }
    }

    private List<UserEntity> users;

    public AuthServiceImpl() {
        users = new LinkedList<>();
        users.add(new UserEntity("login1", "111", "Mikhail"));
        users.add(new UserEntity("login2", "222", "Nikolay"));
        users.add(new UserEntity("login3", "333", "Konstantin"));
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
