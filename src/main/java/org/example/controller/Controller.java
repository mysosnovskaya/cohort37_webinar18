package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Announcement;
import org.example.model.User;
import org.example.repository.AnRepo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/dashboard")
@Slf4j
public class Controller {
    private AnRepo anRepo;

    private JdbcTemplate tmpl;

    @PostMapping("/users")
    public void create(@RequestBody @Valid User user) {
        tmpl.update("INSERT INTO users(user_name, user_phone) VALUES ("+ user.getName()+ "," + user.getPhoneNumber()+ ")");
        log.info("Пользователь создан");
    }

    @GetMapping("/users/{id}")
    public User get(@PathVariable int id) {
        log.info("Сейчас буду отдавать пользователя по айди");
        return tmpl.queryForObject("select * from users where id = ?", new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new User(rs.getInt("id"), rs.getString("user_name"), rs.getString("user_phone"));
            }
        }, id);
    }

    @PostMapping("/ans")
    public Announcement createAn(@RequestBody @Valid Announcement announcement) {
        if (!announcement.announcementValid(true)) {
            return null;
        }

//        tmpl.update("INSERT INTO announcements(title, description, when, award, an_type) VALUES (?, ?, ?, ?, ?)",
//                announcement.getTitle(), announcement.getDescription(), announcement.getDate(),
//                announcement.getAward(), announcement.getType());

        User u = tmpl.queryForObject("select * from users where id = ?", new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new User(rs.getInt("id"), rs.getString("user_name"), rs.getString("user_phone"));
            }
        }, announcement.getAuthorId());

        if (u == null) {
            return null;
        }

        if (anRepo.findAllByAuthorId(announcement.getAuthorId()).size() >= 5) {
            log.info("Не могу создать объявление, превышено количество");
            return null;
        }

        log.info("Создаю объявление....");

        // ищем похожие объявления
        Set<String> words = new HashSet<>(Arrays.asList(announcement.getDescription().replace(", ", "").replace(".", "").toLowerCase().split(" ")));
        List<Announcement> all = anRepo.findAll();
        for (Announcement a: all) {
            Set<String> words2 = new HashSet<>(Arrays.asList(a.getDescription().replace(", ", "").replace(".", "").toLowerCase().split(" ")));
            words.retainAll(words2);
            if (words.size() > words2.size() * 0.2) {
                return a;
            }
        }

        return anRepo.save(announcement);
    }

    @GetMapping("/announcements/{id}")
    public Announcement getAn(@PathVariable int id) {
//        return tmpl.queryForObject("select * from announcements where id = ?", new RowMapper<Announcement>() {
//            @Override
//            public Announcement mapRow(ResultSet rs, int rowNum) throws SQLException {
//                return new Announcement(
//                        rs.getInt("id"), rs.getString("title"), rs.getString("description"),
//                        rs.getDate("when"), rs.getDouble("award"), rs.getString("an_type"));
//            }
//        }, id);

        log.info("GET /announcements/{id}");
        return anRepo.findById(id).orElse(null);
    }

    @DeleteMapping("/announcements/{id}")
    public void deleteAn(@PathVariable int id) {
//        tmpl.update("delete from announcements where id = ?", id);
        anRepo.deleteById(id);
        log.info("объявление удалено");
    }

    @GetMapping("/announcements")
    public Map<User, List<Announcement>> all() {
//        return tmpl.query("select * from announcements where id = ?", new RowMapper<Announcement>() {
//            @Override
//            public Announcement mapRow(ResultSet rs, int rowNum) throws SQLException {
//                return new Announcement(
//                        rs.getInt("id"), rs.getString("title"), rs.getString("description"),
//                        rs.getDate("when"), rs.getDouble("award"), rs.getString("an_type"), rs.getInt("user_id"));
//            }
//        });

        log.info("Нужно получить все объявления");
        List<Announcement> anns = anRepo.findAll();
        Map<User, List<Announcement>> all = new HashMap<>();
        for (Announcement a: anns) {
            User u = tmpl.queryForObject("select * from users where id = ?", new RowMapper<User>() {
                @Override
                public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new User(rs.getInt("id"), rs.getString("user_name"), rs.getString("user_phone"));
                }
            }, a.getAuthorId());
            if (!all.containsKey(u)) {
                all.put(u, new ArrayList<>());
            }
            all.get(u).add(a);
        }
        return all;
    }
}
