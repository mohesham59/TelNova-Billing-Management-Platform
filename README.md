# 📡 TelNova — Telecom Billing Management Platform

A **Java web billing platform** with two deployable modules:
- **`apps/TelNova`**: CDR ingestion, rating cycle orchestration, and invoice generation.
- **`apps/telecom-billing`**: portal/admin flows for contracts, users, plans, and invoices.

Both modules use a shared PostgreSQL data model and SQL billing logic under `database/`.

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
## 🔄 Billing System Project Flow

<img width="1472" height="735" alt="image" src="https://github.com/user-attachments/assets/6f2f53d2-7cfc-4cf5-9ee9-92d8d128bdac" />

<img width="984" height="694" alt="image" src="https://github.com/user-attachments/assets/3560afa5-e353-4329-99d8-2befe837dbbe" />


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
- **Portal/Admin Module** — Additional servlet-based module for contracts, customers, plans, and invoice operations

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Web Layer | Java Servlets (multiple modules) |
| Build Tool | Apache Maven |
| Packaging | WAR (`Telemeter-one.war`, `telnova.war`) |
| Database | PostgreSQL |
| DB Logic | PL/pgSQL (Functions & Triggers) |
| PDF Generation | iText, JasperReports |
| Persistence | JPA (`persistence.xml`) |
| IDE | NetBeans / IntelliJ IDEA |

---

## 📁 Project Structure

```
TelNova-Billing-Management-Platform/
│
├── database/
│   ├── docs/
│   │   ├── ERD.png                       # Entity-Relationship Diagram
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
├── apps/
│   ├── TeleMeter/
│   │   ├── CDRs/                         # Incoming CDR files (runtime; gitignored)
│   │   ├── CDRArchive/                   # Processed CDR files (runtime; gitignored)
│   │   ├── pom.xml                       # Maven build configuration
│   │   └── src/main/
│   │       ├── java/
│   │       │   ├── com/telecom/
│   │       │   │   ├── db/DBConnection.java         # PostgreSQL JDBC connection
│   │       │   │   ├── model/cdrRecord.java          # CDR record model
│   │       │   │   ├── model/Contract.java           # Subscriber contract model
│   │       │   │   ├── model/Rateplan.java           # Rate plan model
│   │       │   │   ├── parser/CdrParser.java         # CDR file parser & DB loader
│   │       │   │   └── servlet/CdrUploadServlet.java # CDR upload HTTP endpoint
│   │       │   └── com/a3m/billing/invoice/
│   │       │       ├── InvoiceMain.java              # Invoice generation entry point
│   │       │       ├── InvoiceData.java              # Invoice data model
│   │       │       ├── InvoiceDataLoader.java        # Loads invoice data from DB
│   │       │       └── InvoicePdfGenerator.java      # Generates PDF invoices
│   │       ├── resources/META-INF/persistence.xml    # JPA persistence config
│   │       └── webapp/
│   │           ├── index.html                        # Web UI entry page
│   │           ├── META-INF/context.xml              # Tomcat context / datasource
│   │           └── WEB-INF/
│   │               ├── web.xml                       # Servlet mappings
│   │               └── beans.xml                     # CDI config
│   │
│   └── telecom-billing/
│       ├── pom.xml                       # Alternative/legacy module (Maven)
│       └── src/                          # Java source and web resources
│
├── logs/                                 # Runtime logs (gitignored)
├── scripts/
│   └── run_billing.sh                    # Local helper script
│
└── README.md
```

---

## ⚙️ Prerequisites

- **Java JDK 17+**
- **Apache Maven 3.6+**
- **PostgreSQL 13+**
- A Java EE servlet container: **Apache Tomcat 9+** or **WildFly**
- **NetBeans IDE** (recommended) or IntelliJ IDEA

---

## 🚦 Quick Start (Fresh Clone)

```bash
# 1) Build TeleMeter module
cd apps/TeleMeter
mvn clean package

# 2) Build telecom-billing module
cd ../telecom-billing
mvn clean package

# 3) (Optional) Run monthly billing job from repo root
cd ../..
chmod +x scripts/run_billing.sh
./scripts/run_billing.sh
```

`scripts/run_billing.sh` auto-detects repository paths and `java`.  
You can override them if needed:

```bash
PROJECT_DIR=/abs/path/to/repo/apps/TeleMeter JAVA=/usr/bin/java ./scripts/run_billing.sh
```

---

## 🗄️ Database Setup

1. Create the database:
   ```sql
   CREATE DATABASE telemeter;
   ```

2. Run the scripts in this order:
   ```bash
   psql -U postgres -d telemeter -f database/schema/Database.sql
   psql -U postgres -d telemeter -f database/schema/Alterations.sql
   psql -U postgres -d telemeter -f database/seeds/rateplan.sql
   psql -U postgres -d telemeter -f "database/seeds/rateplan packages.sql"
   psql -U postgres -d telemeter -f "database/seeds/service packages.sql"
   psql -U postgres -d telemeter -f database/functions/rating_engine.sql
   psql -U postgres -d telemeter -f database/functions/generate_bill.sql
   psql -U postgres -d telemeter -f database/functions/bill_all_active_contracts.sql
   psql -U postgres -d telemeter -f database/functions/reset_billing_cycle.sql
   psql -U postgres -d telemeter -f database/triggers/triggers.sql
   ```

---

## 🔧 Configuration

Update the DB credentials in `apps/TeleMeter/src/main/java/com/telecom/db/DBConnection.java`:

```java
String url      = "jdbc:postgresql://localhost:5432/telemeter";
String user     = "your_postgres_user";
String password = "your_postgres_password";
```

If using a JNDI datasource with Tomcat, configure it in `apps/TeleMeter/src/main/webapp/META-INF/context.xml`.

---

## 🚀 Build & Deploy

```bash
# Build TeleMeter
cd apps/TeleMeter
mvn clean package
cp target/Telemeter-one.war /opt/tomcat/webapps/

# Build telecom-billing
cd ../telecom-billing
mvn clean package
cp target/telnova.war /opt/tomcat/webapps/
```

Start Tomcat and navigate to one of:
```
http://localhost:8080/Telemeter-one/
http://localhost:8080/telnova/
```

---

## 🔄 CDR Processing Flow

```
1. Place CDR .txt files in  apps/TeleMeter/CDRs/
2. Upload via web UI        →  CdrUploadServlet  →  CdrParser  →  DB
3. Rate CDRs                →  rating_engine() runs against active contracts
4. Generate bills           →  bill_all_active_contracts()
5. Export PDF invoices      →  InvoiceMain  →  InvoicePdfGenerator
6. Archive processed CDRs   →  apps/TeleMeter/CDRArchive/
```

---

## 🗺️ Database Diagrams

Visual documentation is available in `database/docs/`:

| File | Description |
|---|---|
| `ERD.png` | Full entity-relationship diagram of all tables |
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
