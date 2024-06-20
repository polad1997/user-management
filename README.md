# Sky Identity: user-management

Sky Identity is a Spring Boot application designed to manage users and their associated external projects. It provides RESTful APIs for user management, role-based access control, and project association. The application uses PostgreSQL as its database and Liquibase for database migrations. It is containerized using Docker and orchestrated with Docker Compose for easy setup and deployment.

## Getting Started

#### Prerequisites
- Docker
- Docker Composer

#### Clone the Repository
To get started, clone the repository and navigate into the project directory:
- `git clone https://github.com/polad1997/user-management.git`
- `cd <repository-directory>`

#### Run the Application
`docker-compose up --build`


## Application Behavoir:
- ### Initial Users:
    - Admin User:
      - email: admin@admin.com
      - username: ADMIN
      - password: adminpassword
      
    - Regular User:
      - email: user@user.com
      - username: USER
      - password: userpassword
- ### Authentication Requirements:
    - All write operations require ADMIN authentication.
    - All read operations require either ADMIN or USER authentication.
    - Default role for new users is ROLE_USER.


## Features
- ### Authentication:
    - Basic authentication with two roles: `ROLE_ADMIN` and `ROLE_USER`.
- ### User Management:
    - Create a new user
    - Retrieve user information
    - Delete a user
    - Update user password
- ### External Project Management:
    - Add external projects to a user
    - Retrieve external projects associated with a user
- ### Testing
    - Unit tests to ensure the correctness of the endpoints and service logic


## API Documentation
- ### User Management Endpoint:
  - Create User: POST /users
  - Get User by ID: GET /users/{id}
  - Update User: PUT /users/{id}
  - Delete User: DELETE /users/{id}
  
- ### External Project Endpoint:
  - Add External Project to User: POST /users/{id}/external-projects
  - Get External Projects of User: GET /users/{id}/external-projects

