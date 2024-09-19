# Vaccine-Appointment-Scheduling-System
## Project Description
This project implements a command-line application for scheduling COVID-19 vaccination appointments. The system allows patients to register, book appointments with available caregivers, and track their scheduled appointments. Caregivers can manage their availability and track the vaccine stock. The project uses Java, JDBC, and Microsoft Azure SQL for database management.
## Project Goals
- Enable patients to create accounts, log in, and schedule vaccination appointments.
- Allow caregivers to manage their schedules and update vaccine availability.
- Build a relational database to store information about patients, caregivers, vaccines, and appointments.
- Implement secure user authentication using password salting and hashing techniques.
## Technologies Used
- Java: Primary programming language.
- JDBC: Java Database Connectivity for interacting with the Azure SQL database.
- Microsoft Azure SQL: Cloud database for storing system data.
- SQL: For creating and managing the database schema.
- Cryptography: Used for securely hashing user passwords with salting techniques.
## Data Model
The system stores the following information in the database:
![design (1)](https://github.com/user-attachments/assets/b2c721a9-66c5-47c0-843e-06c1df81d561)

- Patients: Stores patient details and login credentials.
- Caregivers: Stores caregiver details, availability, and login credentials.
- Vaccines: Tracks vaccine names and available doses.
- Appointments: Records appointments between patients and caregivers, including the vaccine administered and the appointment date.
