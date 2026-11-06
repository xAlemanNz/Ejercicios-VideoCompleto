package com.zip.usuarios;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    static final int MAX_NAME = 32;
    static final int MAX_EMAIL = 64;
    static final int MAX_USERS = 8;

    JdbcTemplate db;

    public UsuarioController(JdbcTemplate db) {
        this.db = db;
        db.execute("CREATE TABLE IF NOT EXISTS usuarios ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "email TEXT NOT NULL, "
                + "age INTEGER NOT NULL)");
        System.out.println("users: " + count() + "/" + MAX_USERS);
    }

    Usuario row(ResultSet rs, int n) throws SQLException {
        Usuario u = new Usuario();
        u.id = rs.getInt("id");
        u.name = rs.getString("name");
        u.email = rs.getString("email");
        u.age = rs.getInt("age");
        return u;
    }

    int count() {
        return db.queryForObject("SELECT COUNT(*) FROM usuarios", Integer.class);
    }

    Usuario find(int id) {
        List<Usuario> res = db.query("SELECT * FROM usuarios WHERE id = ?", this::row, id);
        if (res.size() == 0) return null;
        return res.get(0);
    }

    boolean validate(Usuario u) {
        if (u == null) return false;
        if (u.name == null || u.name.length() == 0 || u.name.length() > MAX_NAME) return false;
        if (u.email == null || u.email.indexOf('@') == -1 || u.email.length() > MAX_EMAIL) return false;
        if (u.age == null || u.age < 0 || u.age > 150) return false;
        return true;
    }

    ResponseEntity<Object> err(int status, String msg) {
        return ResponseEntity.status(status).body(Map.of("error", msg));
    }

    @GetMapping
    public List<Usuario> users_list() {
        return db.query("SELECT * FROM usuarios ORDER BY id", this::row);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> users_get(@PathVariable int id) {
        Usuario u = find(id);
        if (u == null) return err(404, "ENOENT: no existe ese id");
        return ResponseEntity.ok(u);
    }

    @PostMapping
    public synchronized ResponseEntity<Object> users_create(@RequestBody Usuario u) {
        if (!validate(u)) return err(400, "EINVAL: datos invalidos");
        if (count() >= MAX_USERS) return err(507, "ENOMEM: heap lleno");
        u.id = db.queryForObject("INSERT INTO usuarios (name, email, age) VALUES (?, ?, ?) RETURNING id",
                Integer.class, u.name, u.email, u.age);
        return ResponseEntity.status(201).body(u);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> users_update(@PathVariable int id, @RequestBody Usuario u) {
        if (find(id) == null) return err(404, "ENOENT: no existe ese id");
        if (!validate(u)) return err(400, "EINVAL: datos invalidos");
        db.update("UPDATE usuarios SET name = ?, email = ?, age = ? WHERE id = ?", u.name, u.email, u.age, id);
        return ResponseEntity.ok(find(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> users_delete(@PathVariable int id) {
        int n = db.update("DELETE FROM usuarios WHERE id = ?", id);
        if (n == 0) return err(404, "ENOENT: no existe ese id");
        return ResponseEntity.noContent().build();
    }
}
