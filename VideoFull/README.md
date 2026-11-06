# allin1practices
Linux & MacOS
```
./mvnw spring-boot:run
```

On Windows:

```
mvnw.cmd spring-boot:run
```

Run http://localhost:3000

## API

| method | route | |
|---|---|---|
| GET | /usuarios | list all |
| GET | /usuarios/{id} | get one |
| POST | /usuarios | create |
| PUT | /usuarios/{id} | update |
| DELETE | /usuarios/{id} | delete |

Body: `{ "name": "...", "email": "...", "age": 0 }`
