# E-Commerce Backend

A production-style e-commerce backend built with Spring Boot. It includes JWT-based authentication, role-based authorization, product management, cart operations, order creation, and Stripe payment intent integration.

## Features

- JWT authentication with secure password hashing using BCrypt
- Role-based access control for `USER` and `ADMIN`
- Product CRUD APIs
- Cart management with stock validation
- Order creation from cart with total calculation and cart clearing
- Stripe payment intent creation and payment status confirmation
- Global exception handling with meaningful HTTP responses
- Unit tests for core service layers using JUnit 5 and Mockito

## Tech Stack

- Java 17+
- Spring Boot 3
- Spring Security
- Spring Data JPA / Hibernate
- MySQL
- Maven
- Lombok
- Stripe
- JUnit 5
- Mockito
- Postman

## Project Structure

```text
src/
  main/
    java/com/pranay/ecommerce_backend/
      config/
      controller/
      dto/
      entity/
      exception/
      repository/
      security/
      service/
        impl/
    resources/
      application.properties
  test/
    java/com/pranay/ecommerce_backend/
      service/
```

## API Endpoints

### Authentication

- `POST /api/auth/register`
- `POST /api/auth/login`

### Products

- `GET /api/products`
- `GET /api/products/{id}`
- `POST /api/products` `ADMIN`
- `PUT /api/products/{id}` `ADMIN`
- `DELETE /api/products/{id}` `ADMIN`

### Cart

- `GET /api/cart`
- `POST /api/cart/items`
- `PUT /api/cart/items/{cartItemId}`
- `DELETE /api/cart/items/{cartItemId}`

### Orders

- `POST /api/orders`
- `GET /api/orders`
- `GET /api/orders/{id}`

### Payments

- `POST /api/payments/intent`
- `POST /api/payments/confirm`

## Database Design

- `User` -> one-to-one with `Cart`
- `User` -> one-to-many with `CustomerOrder`
- `Cart` -> one-to-many with `CartItem`
- `CustomerOrder` -> one-to-many with `OrderItem`
- `Product` is linked to orders through `OrderItem`

## Configuration

Update `src/main/resources/application.properties` before running locally:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
spring.datasource.username=root
spring.datasource.password=your_password

app.jwt.secret=your_base64_secret
app.jwt.expiration=86400000

stripe.secret-key=dummy_key
stripe.currency=inr
```
## Sample Flow

1. Register a user
2. Login and copy the JWT token
3. Create or promote an admin user in MySQL
4. Login as admin
5. Create products
6. Login as user
7. Add products to cart
8. Create an order from the cart
9. Create a Stripe payment intent
10. Confirm payment status

## Admin Note

Public registration only creates `USER` accounts. This is intentional to prevent privilege escalation through the open registration API.

To test admin-only endpoints, create a normal user first and then update the role in MySQL:

```sql
UPDATE users
SET role = 'ADMIN'
WHERE email = 'admin@example.com';
```

## Testing Coverage

Unit tests are included for:

- Auth service
- Product service
- Cart service
- Order service

