# 📡 TeleMeter — Telecom Billing Management Platform

A **Java EE web application** for telecom billing automation. TeleMeter processes **Call Detail Records (CDRs)**, rates them against subscriber rate plans using a billing engine, generates PDF invoices, and manages the full billing cycle — backed by a **PostgreSQL** database with stored functions and triggers.

---

## 🏗️ Architecture Overview

```
CDR Files (.txt)
      │
      ▼
CdrUploadServlet  ──►  CdrParser  ──►  PostgreSQL DB
                                             │
                                   ┌─────────┴───────────┐
                                   │                     │
                             Rating Engine        Billing Functions
                                   │                     │
                                   └─────────┬───────────┘
                                             │
                                    InvoicePdfGenerator
                                             │
                                        PDF Invoice
```

---

## ✨ Features

- **CDR Upload & Parsing** — Upload CDR `.txt` files via the web interface; `CdrParser` extracts and stores records in the database
- **Rating Engine** — Rates each CDR against active subscriber contracts and rate plans (PL/pgSQL)
- **Automated Billing** — `bill_all_active_contracts` function bills all active subscribers in one run
- **Bill Generation** — `generate_bill` computes charges per contract and billing period
- **Billing Cycle Reset** — `reset_billing_cycle` resets usage counters after each cycle
- **PDF Invoice Generation** — Java-based `InvoicePdfGenerator` produces downloadable customer invoices
- **CDR Archiving** — Processed CDR files are moved from `CDRs/` to `CDRArchive/` automatically
- **Rate Plan Management** — Seeded rate plans and service packages configurable in the database
- **Database Triggers** — Automated DB-level business logic via PostgreSQL triggers

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java (EE / Jakarta EE) |
| Web Layer | Java Servlet (`CdrUploadServlet`) |
| Build Tool | Apache Maven |
| Packaging | WAR (`Telemeter-one.war`) |
| Database | PostgreSQL |
| DB Logic | PL/pgSQL (Functions & Triggers) |
| PDF Generation | Java (`InvoicePdfGenerator`) |
| Persistence | JPA (`persistence.xml`) |
| IDE | NetBeans |

---

## 📁 Project Structure

```
TeleMeter-Billing-Management-Platform/
│
├── Database/
│   ├── docs/
│   │   ├── ERD Diagram.jpg               # Entity-Relationship Diagram
│   │   └── billing_mapping.png           # Billing flow mapping
│   ├── functions/
│   │   ├── rating_engine.sql             # Core CDR rating logic
│   │   ├── generate_bill.sql             # Bill generation per contract
│   │   ├── bill_all_active_contracts.sql # Bulk billing runner
│   │   └── reset_billing_cycle.sql       # Billing cycle reset
│   ├── schema/
│   │   ├── Database.sql                  # Full DB schema (tables, types)
│   │   └── Alterations.sql               # Schema modifications
│   ├── seeds/
│   │   ├── rateplan.sql                  # Rate plan seed data
│   │   ├── rateplan packages.sql         # Rate plan packages
│   │   └── service packages.sql          # Service package seed data
│   └── triggers/
│       └── triggers.sql                  # PostgreSQL triggers
│
├── TeleMeter/
│   ├── CDRs/                             # Incoming CDR files (pending processing)
│   ├── CDRArchive/                       # Processed & archived CDR files
│   ├── pom.xml                           # Maven build configuration
│   └── src/main/
│       ├── java/
│       │   ├── com/telecom/
│       │   │   ├── db/DBConnection.java         # PostgreSQL JDBC connection
│       │   │   ├── model/cdrRecord.java          # CDR record model
│       │   │   ├── model/Contract.java           # Subscriber contract model
│       │   │   ├── model/Rateplan.java           # Rate plan model
│       │   │   ├── parser/CdrParser.java         # CDR file parser & DB loader
│       │   │   └── servlet/CdrUploadServlet.java # CDR upload HTTP endpoint
│       │   └── com/a3m/billing/invoice/
│       │       ├── InvoiceMain.java              # Invoice generation entry point
│       │       ├── InvoiceData.java              # Invoice data model
│       │       ├── InvoiceDataLoader.java        # Loads invoice data from DB
│       │       └── InvoicePdfGenerator.java      # Generates PDF invoices
│       ├── resources/META-INF/persistence.xml    # JPA persistence config
│       └── webapp/
│           ├── index.html                        # Web UI entry page
│           ├── META-INF/context.xml              # Tomcat context / datasource
│           └── WEB-INF/
│               ├── web.xml                       # Servlet mappings
│               └── beans.xml                     # CDI config
│
└── README.md
```

---

## ⚙️ Prerequisites

- **Java JDK 11+**
- **Apache Maven 3.6+**
- **PostgreSQL 13+**
- A Java EE servlet container: **Apache Tomcat 9+** or **WildFly**
- **NetBeans IDE** (recommended) or IntelliJ IDEA with Java EE support

---

## 🗄️ Database Setup

1. Create the database:
   ```sql
   CREATE DATABASE telemeter;
   ```

2. Run the scripts in this order:
   ```bash
   psql -U postgres -d telemeter -f Database/schema/Database.sql
   psql -U postgres -d telemeter -f Database/schema/Alterations.sql
   psql -U postgres -d telemeter -f Database/seeds/rateplan.sql
   psql -U postgres -d telemeter -f "Database/seeds/rateplan packages.sql"
   psql -U postgres -d telemeter -f "Database/seeds/service packages.sql"
   psql -U postgres -d telemeter -f Database/functions/rating_engine.sql
   psql -U postgres -d telemeter -f Database/functions/generate_bill.sql
   psql -U postgres -d telemeter -f Database/functions/bill_all_active_contracts.sql
   psql -U postgres -d telemeter -f Database/functions/reset_billing_cycle.sql
   psql -U postgres -d telemeter -f Database/triggers/triggers.sql
   ```

---

## 🔧 Configuration

Update the DB credentials in `src/main/java/com/telecom/db/DBConnection.java`:

```java
String url      = "jdbc:postgresql://localhost:5432/telemeter";
String user     = "your_postgres_user";
String password = "your_postgres_password";
```

If using a JNDI datasource with Tomcat, configure it in `src/main/webapp/META-INF/context.xml`.

---

## 🚀 Build & Deploy

```bash
cd TeleMeter

# Build the WAR
mvn clean package

# Deploy to Tomcat
cp target/Telemeter-one.war /opt/tomcat/webapps/
```

Start Tomcat and navigate to:
```
http://localhost:8080/Telemeter-one/
```

---

## 🔄 CDR Processing Flow

```
1. Place CDR .txt files in  TeleMeter/CDRs/
2. Upload via web UI        →  CdrUploadServlet  →  CdrParser  →  DB
3. Rate CDRs                →  rating_engine() runs against active contracts
4. Generate bills           →  bill_all_active_contracts()
5. Export PDF invoices      →  InvoiceMain  →  InvoicePdfGenerator
6. Archive processed CDRs   →  TeleMeter/CDRArchive/
```

---

## 🗺️ Database Diagrams

Visual documentation is available in `Database/docs/`:

| File | Description |
|---|---|
| `ERD Diagram.jpg` | Full entity-relationship diagram of all tables |
| `billing_mapping.png` | Billing data flow and rate mapping overview |

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "feat: describe your change"`
4. Push and open a Pull Request

---

## 👤 Authors

**Mohamed Hesham**  
GitHub: [@mohesham59](https://github.com/mohesham59)

**Mahmoud Osama**  
GitHub: [@Mahmoud0ssama](https://github.com/Mahmoud0ssama)

**Mahmoud Eissa**  
GitHub: [@mahmoudeissa9](https://github.com/mahmoudeissa9)

**Ali Omran**  
GitHub: [@aliomran10](https://github.com/aliomran10)
