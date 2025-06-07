# HIV Care Backend API

Backend API cho hệ thống quản lý điều trị HIV.

## Công nghệ sử dụng

- **Spring Boot 3.2.0** - Framework chính
- **Spring Security** - Bảo mật và xác thực
- **Spring Data JPA** - Truy cập dữ liệu
- **JWT** - JSON Web Token cho xác thực
- **H2 Database** - Database trong memory (có thể chuyển sang PostgreSQL)
- **Maven** - Quản lý dependencies

## Yêu cầu hệ thống

- Java 17 hoặc cao hơn
- Maven 3.6+

## Cài đặt và chạy

### 1. Clone repository

```bash
git clone <repository-url>
cd backend
```

### 2. Cài đặt dependencies

```bash
mvn clean install
```

### 3. Chạy ứng dụng

```bash
mvn spring-boot:run
```

Hoặc chạy từ IDE:

```bash
java -jar target/hiv-care-backend-1.0.0.jar
```

### 4. Truy cập API

- **API Base URL**: `http://localhost:8080/api`
- **H2 Console**: `http://localhost:8080/api/h2-console`
- **Swagger UI** (nếu có): `http://localhost:8080/api/swagger-ui.html`

## Tài khoản mặc định

Hệ thống tự động tạo các tài khoản demo:

| Email            | Password   | Role     |
| ---------------- | ---------- | -------- |
| patient@hiv.care | patient123 | CUSTOMER |
| doctor@hiv.care  | doctor123  | DOCTOR   |
| admin@hiv.care   | admin123   | ADMIN    |
| manager@hiv.care | manager123 | MANAGER  |

## API Endpoints

### Authentication

- `POST /auth/login` - Đăng nhập
- `POST /auth/register` - Đăng ký
- `POST /auth/refresh` - Refresh token
- `POST /auth/logout` - Đăng xuất

### Users

- `GET /users/profile` - Lấy thông tin profile
- `PUT /users/profile` - Cập nhật profile
- `GET /users/role/{role}` - Lấy users theo role

### Doctors

- `GET /doctors/public` - Danh sách bác sĩ (public)
- `GET /doctors/{id}` - Chi tiết bác sĩ
- `GET /doctors/available` - Bác sĩ có lịch

### Appointments

- `GET /appointments` - Danh sách lịch hẹn
- `POST /appointments` - Tạo lịch hẹn mới
- `PUT /appointments/{id}` - Cập nhật lịch hẹn
- `DELETE /appointments/{id}` - Hủy lịch hẹn

### Test Results

- `GET /test-results` - Danh sách kết quả xét nghiệm
- `POST /test-results` - Thêm kết quả mới
- `GET /test-results/latest` - Kết quả mới nhất

### Anonymous Consultation

- `POST /anonymous/consultation` - Tạo tư vấn ẩn danh
- `GET /anonymous/consultation/{id}` - Theo dõi tư vấn

## Cấu hình Database

### H2 (Default - Development)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:hivcare
    driver-class-name: org.h2.Driver
    username: sa
    password: ""
```

### PostgreSQL (Production)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hivcare
    username: your_username
    password: your_password
```

## Environment Variables

| Variable      | Default        | Description       |
| ------------- | -------------- | ----------------- |
| JWT_SECRET    | auto-generated | JWT secret key    |
| MAIL_USERNAME | -              | Email username    |
| MAIL_PASSWORD | -              | Email password    |
| DB_URL        | h2:mem:hivcare | Database URL      |
| DB_USERNAME   | sa             | Database username |
| DB_PASSWORD   | ""             | Database password |

## Security

- JWT-based authentication
- Role-based authorization
- CORS enabled cho frontend
- Password encryption với BCrypt
- Request validation

## Logging

Logs được cấu hình để:

- DEBUG level cho application
- SQL logging cho development
- Error tracking

## Testing

```bash
# Chạy unit tests
mvn test

# Chạy integration tests
mvn verify
```

## Deployment

### Docker (Recommended)

```dockerfile
FROM openjdk:17-jre-slim
COPY target/hiv-care-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Build cho production

```bash
mvn clean package -Pprod
```

## API Documentation

Sử dụng Postman hoặc curl để test APIs:

### Đăng nhập

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient@hiv.care",
    "password": "patient123"
  }'
```

### Lấy danh sách bác sĩ

```bash
curl -X GET http://localhost:8080/api/doctors/public
```

### Tạo lịch hẹn (cần token)

```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "doctorId": 1,
    "appointmentDate": "2024-12-25T14:00:00",
    "type": "CONSULTATION",
    "reason": "Tái khám định kỳ"
  }'
```

## Troubleshooting

### Common Issues

1. **Port 8080 already in use**

   ```bash
   # Thay đổi port trong application.yml
   server:
     port: 8081
   ```

2. **JWT Secret error**

   ```bash
   # Set environment variable
   export JWT_SECRET=your-secret-key-here
   ```

3. **Database connection error**
   - Kiểm tra database configuration
   - Đảm bảo database service đang chạy

## Support

Nếu gặp vấn đề, vui lòng:

1. Kiểm tra logs trong console
2. Xác nhận configuration
3. Kiểm tra database connectivity

## License

Copyright © 2024 HIV Care System
