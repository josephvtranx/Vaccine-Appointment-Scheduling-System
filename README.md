# Vaccine-Appointment-Scheduling-System
## Project Overview
This project implements a command-line application for scheduling COVID-19 vaccination appointments. The system allows patients to register, book appointments with available caregivers, and track their scheduled appointments. Caregivers can manage their availability and track the vaccine stock. The project uses Java, JDBC, and Microsoft Azure SQL for database management.
## Features
- Patient Management: Register, log in, view appointments, and schedule vaccine doses.
- Caregiver Management: Manage availability, update vaccine inventory, and view appointments.
- Appointment Scheduling: Search for caregivers and book appointments based on available vaccine stock.
## Technologies 
- Java: Primary programming language.
- JDBC: Java Database Connectivity for interacting with the Azure SQL database.
- Microsoft Azure SQL: Cloud database for storing system data.
- SQL: For creating and managing the database schema.
- Cryptography: Used for securely hashing user passwords with salting techniques.
## Data Architecture
The system stores the following information in the database:
![design (1)](https://github.com/user-attachments/assets/b2c721a9-66c5-47c0-843e-06c1df81d561)

- Patients: Stores patient details and login credentials.
- Caregivers: Stores caregiver details, availability, and login credentials.
- Vaccines: Tracks vaccine names and available doses.
- Appointments: Records appointments between patients and caregivers, including the vaccine administered and the appointment date.

## Setup Instructions
1. Database:
- Use create.sql to set up your Azure SQL database.
2. Environment:
- Set environment variables (Server, DBName, UserID, Password) for the database connection.
3. Dependencies:
- Download and configure the JDBC driver in lib/.
4. Run:
- Compile and execute Scheduler.java from an IDE (e.g., IntelliJ).
### Key Commands
## Create a patient:
create_patient \<username\> \<password\>
## Log in as caregiver:
login_caregiver \<username\> \<password\>
## Search caregivers:
search_caregiver_schedule \<date\>
## Book appointment:
reserve \<date\> \<vaccine\>
## Log out:
logout
