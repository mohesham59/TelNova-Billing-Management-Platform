# TeleMeter Billing Management Platform – Docker Setup

## بنية المشروع

```
TeleMeter-Billing-Management-Platform/
├── apps/
│   ├── telecom-billing/        ← Admin Portal (telnova.war)
│   └── TeleMeter/              ← CDR Parser + Billing Job (Telemeter-one.war)
├── database/
│   ├── schema/                 ← SQL schema & alterations
│   ├── functions/              ← Stored procedures
│   ├── seeds/                  ← Initial data
│   └── triggers/               ← DB triggers
├── docker/
│   ├── telecom-billing/        ← Dockerfile + context.xml للـ billing portal
│   ├── telemeter/              ← Dockerfile + context.xml للـ TeleMeter
│   └── billing-cron/           ← Dockerfile + entrypoint للـ cron job
├── scripts/
│   └── run_billing.sh          ← Billing cycle script
├── logs/                       ← Log files (auto-created)
├── docker-compose.yml
└── .env.example
```

## Services

| Service | Container | Port | الوصف |
|---------|-----------|------|-------|
| `postgres` | tbmp-postgres | 5432 | PostgreSQL 16 |
| `telecom-billing` | tbmp-billing-portal | 8080 | Admin Portal |
| `telemeter` | tbmp-telemeter | 8081 | CDR Parser + Billing Job |
| `billing-cron` | tbmp-billing-cron | - | Cron لتشغيل الـ billing |

---

## الخطوات

### 1. تجهيز ملف الـ environment

```bash
cp .env.example .env
# عدّل القيم زي ما تحتاج
```

### 2. تشغيل المشروع كله

```bash
docker compose up --build -d
```

أو لو عاوز تشوف الـ logs مباشرة:

```bash
docker compose up --build
```

### 3. التحقق إن كل حاجة شغالة

```bash
docker compose ps
```

### 4. فتح المشروع في المتصفح

- **Admin Portal (telecom-billing):** http://localhost:8080/telnova
- **TeleMeter:** http://localhost:8081/Telemeter-one

---

## أوامر مفيدة

### وقف المشروع
```bash
docker compose down
```

### وقف المشروع وحذف الـ volumes (بيانات الـ DB)
```bash
docker compose down -v
```

### عرض logs لـ service معين
```bash
docker compose logs -f telecom-billing
docker compose logs -f telemeter
docker compose logs -f postgres
docker compose logs -f billing-cron
```

### تشغيل الـ billing cycle يدوياً (بدون انتظار الـ cron)
```bash
docker compose exec billing-cron /scripts/run_billing.sh
```

### الدخول على الـ database مباشرة
```bash
docker compose exec postgres psql -U telecom -d telecom_billing
```

### Rebuild service معين بس
```bash
docker compose up --build telecom-billing -d
```

---

## CDR Files

ملفات الـ CDR بتتحط في:
```
apps/TeleMeter/CDRArchive/
```
وبتتعمل mount تلقائياً جوه الـ TeleMeter container على المسار `/app/CDRArchive`.

---

## تغيير موعد الـ Billing Cron

في ملف `.env`:
```env
# كل يوم الساعة 2 الصبح
BILLING_CRON=0 2 * * *

# كل أول الشهر الساعة 12 الليل
BILLING_CRON=0 0 1 * *
```

---

## المتطلبات

- Docker Engine 24+
- Docker Compose v2.20+

---

## Architecture Diagram

```
                        ┌─────────────────────────────────────────┐
                        │         Docker Compose Network           │
                        │                                          │
  Browser ──:8080──► ┌──┴──────────────┐                         │
                      │ telecom-billing │                         │
  Browser ──:8081──► │   (telnova.war) │                         │
                      └────────┬────────┘                         │
                               │ JDBC                             │
                      ┌────────▼────────┐   ┌──────────────────┐  │
                      │    postgres     │◄──│    telemeter     │  │
                      │  (port 5432)   │   │ (CDR Parser/Job) │  │
                      └────────▲────────┘   └──────────────────┘  │
                               │                                   │
                      ┌────────┴────────┐                         │
                      │  billing-cron   │                         │
                      │ (run_billing.sh)│                         │
                      └─────────────────┘                         │
                        └─────────────────────────────────────────┘
```
