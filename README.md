# Secure API Documentation

This API demonstrates features like data validation, encryption (at rest and in transit), and secure access control mechanisms using Spring Security, JWT, AES encryption, and Content Security Policy (CSP).

## **Endpoints**

### **1. Authentication Endpoints**

#### **Register User**
- **URL**: `/api/v1/auth/register`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "username": "string",
    "password": "string",
  } 

- **Response Body**:
  ```json
  {
    "statusCode": 200,
    "message": "Success",
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzaHVqYWEiLCJpYXQiOjE3MjczNjkwMDIsImV4cCI6MTcyNzQ1NTQwMn0.aYYD8xaP7zFx3ZRRFscPj7rkymAxDnm7oUj8MFZcLF1",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzaHVqYWEiLCJpYXQiOjE3MjczNjkwMDIsImV4cCI6MTcyNzk3MzgwMn0.5kSh2ffHFGKjCBVz6hQ2JKAYYMl4bw5YCbdl8jBC7KS"
  } 
   
#### **Login User**
- **URL**: `/api/v1/auth/login`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "username": "string",
    "password": "string",
  } 

- **Response Body**:
  ```json
  {
    "statusCode": 200,
    "message": "Success",
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzaHVqYWEiLCJpYXQiOjE3MjczNjkwMDIsImV4cCI6MTcyNzQ1NTQwMn0.aYYD8xaP7zFx3ZRRFscPj7rkymAxDnm7oUj8MFZcLF1",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzaHVqYWEiLCJpYXQiOjE3MjczNjkwMDIsImV4cCI6MTcyNzk3MzgwMn0.5kSh2ffHFGKjCBVz6hQ2JKAYYMl4bw5YCbdl8jBC7KS"
  } 
   
#### **Refresh Token**
- **URL**: `/api/v1/auth/login`
- **Method**: `POST`
- **HEADERS**:
- - `Content-Type: application/x-www-form-urlencoded
    Authorization: Bearer {{refreshToken}}`

### **2. Demo Endpoints**

#### **Secured Endpoint**
- **URL**: `/api/v1/demo/secured`
- **Method**: `POST`
- **HEADERS**: `Authorization: Bearer {{accessToken}}`
- **Request Body**:
  ```json
  {
    "message": "string",
    "names": ["string"],
    "phoneNumber": "254XXXXXXXXX",
    "callbackUrl": "https://validurl.com"
  }
  
- **Response Body**:
  ```json
  {
    "status": 0,
    "message": "From secured validated object"
  }

#### **Secured Endpoint**
- **URL**: `/api/v1/demo/unsecured`
- **Method**: `GET`
- **Response Body**:
  ```json
  {
    "status": 0,
    "message": "unsecured endpoint"
  }

## **Security Features**
### **1. Data Encryption**
- **At Rest**: AES encryption is used to protect sensitive fields like `username` in the `SystemUser` entity.
  Example of an encrypted field in the database:
 ```sql
 id |  created_at   |                           password                           |    role     | status |         username         
----+---------------+--------------------------------------------------------------+-------------+--------+--------------------------
  1 | 1727362304682 | $2a$10$RXV9xPLIGoMOItVqfHat3.7UmsF53VxJA7TKTQUkh3Ni.DkioGOnm | SYSTEM_USER |        | BNdzF/pAvSpxrcqBL9mRlQ==
  2 | 1727369002252 | $2a$10$CqVa.2nIIbLY6eKmbkCmue9f5QNBkuRQNAvm7aGiSkubAkMpg/3Ce | SYSTEM_USER |        | EOL2a+Ij3y7b0fyOX5T5hw==
```
- The `SystemUser` entity uses `AES` encryption for sensitive fields such as `username`:
```java
@Column(name = "username", unique = true)
@Convert(converter = AesEncryptor.class)
private String username;
```
- **In Transit**: Encrypted response
 ```json
  {
    "data": "8k\/eFUO\/S\/mmR1y\/+w6P3EJrqka3woF5CGFq6zAczNGPYOs9hsGNhWvQuB1MmPbv"
  }
```

### **2. Input Validation**
- Validation is handled using annotations in the DTO classes. For example, the `Hello` DTO validates that:
  The `message` field is not empty.
  The `phoneNumber` follows the format starting with 254 and contains exactly 12 digits.
  The `callbackUrl` is a valid URL.
  Example of validation in `Hello` class:

```java
@NotEmpty(message = "Message cannot be empty")
private String message;

@Pattern(regexp = "^254\\d{9}$", message = "Phone number must start with 254 and be 12 digits long")
private Long phoneNumber;
```
### **3. Content Security Policy (CSP)**
- A strong CSP is enforced in the security configuration to prevent XSS and other injection attacks:
```java
http.headers(headers -> 
    headers.contentSecurityPolicy("default-src 'none'")
           .xssProtection(xXssConfig -> 
                xXssConfig.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)));

```
### **4. JWT Authentication**
- JWT tokens are used for securing endpoints. The `secured` endpoint requires a valid token, while the `unsecured` endpoint is accessible without authentication.

### **5. Stateless Session Management**
- The API uses stateless session management, ensuring that no user session is stored on the server:
```java
http.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
```
## **License**
This project is licensed under the MIT License.

## **Contributing**
Feel free to submit issues or pull requests for any improvements or fixes.



