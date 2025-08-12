
## Description
This application serves as a digital record keeper for users to manage their personal expenses. Users can register, log in, and perform full CRUD operations — including adding, editing, viewing, and deleting their expenditure records. It provides a secure backend powered by Spring Boot and MongoDB, and is designed to be integrated with any frontend interface.


## Features

- User Registration & Login (JWT)
- Secure endpoints (Spring Security)
- Add, View, Update, and Delete Expenditures
- MongoDB for persistent storage
- Swagger/OpenAPI integration for easy testing

## API Endpoints

### Auth

| Method | Endpoint          | Description         |
|--------|-------------------|---------------------|
| POST   | `/api/users/register` | Register new user   |
| POST   | `/api/users/login`    | Login, returns JWT  |

### Expenditures

> Requires valid JWT in Authorization header (`Bearer <token>`)

| Method | Endpoint             | Description             |
|--------|----------------------|-------------------------|
| POST   | `/api/expenditures`  | Add new expenditure     |
| GET    | `/api/expenditures`  | Get all expenditures    |
| PUT    | `/api/expenditures/{id}` | Update expenditure  |
| DELETE | `/api/expenditures/{id}` | Delete expenditure  |

### User

> Requires valid JWT in Authorization header (`Bearer <token>`)

| Method | Endpoint                | Description                 |
|--------|-------------------------|-----------------------------|
| PUT    | `/api/users/password`   | Update password of the user |
| DELETE | `/api/users/{username}` | Delete user                 |

---

## Security

- Passwords hashed using `BCrypt`
- JWT-based stateless authentication
- Spring Security with filter chain
- CSRF protection enabled for web