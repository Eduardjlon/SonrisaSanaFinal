# Sistema Web de Gestión Clínica – Sonrisa Sana

Proyecto Jakarta EE / JSF 4 con PrimeFaces 15, PostgreSQL en Docker y despliegue pensado para Azure. Implementa la mayor parte de los requerimientos del enunciado de **Programación IV** para la clínica odontológica *Sonrisa Sana*.  

## Tecnologías

- Jakarta EE 10 (JSF, CDI, JPA)
- Java 21
- PrimeFaces 15 (Jakarta)
- PrimeFlex 3
- PostgreSQL 16 (Docker)
- JPA + Hibernate (RESOURCE_LOCAL)
- JasperReports (facturas y reportes PDF)
- Azure Blob Storage (archivos clínicos)
- WildFly (local / Azure)

## Estructura principal

- Gestión de usuarios y roles (ADMINISTRADOR, ODONTOLOGO, RECEPCIONISTA)
- Gestión de pacientes
- Agenda de citas con estados (PENDIENTE, CONFIRMADA, ATENDIDA, CANCELADA, REPROGRAMADA)
- Catálogo de tratamientos
- Facturación básica (sub‑total, descuentos, seguro, total a pagar)
- Archivos clínicos con URL en Azure Blob Storage
- Dashboard simple para ADMIN con tarjetas de métricas

> El proyecto está preparado para correr localmente con Docker (PostgreSQL) y desplegarse como WAR en WildFly tanto en local como en Azure App Service / máquina virtual. fileciteturn0file0

## Cómo levantar PostgreSQL con Docker

En la raíz del proyecto:

```bash
docker compose up -d
```

Se levanta PostgreSQL en:

- Host: `localhost`
- Puerto: `5555`
- Base de datos: `sonrisasana`
- Usuario: `postgres`
- Password: `admin123`

La configuración por defecto de JPA (persistence.xml) apunta a estos valores.

## Build y despliegue local

```bash
mvn clean package
```

Obtendrás `target/SonrisaSana.war`, que puedes desplegar en WildFly 37+.

Ejemplo (si usas wildfly-maven-plugin y tu WildFly corre localmente):

```bash
mvn wildfly:deploy
```

## Variables de entorno (Azure / producción)

Para despliegues en Azure se recomienda configurar estas variables:

- `SONRISA_DB_URL` – URL JDBC completa (ej: `jdbc:postgresql://<host>:5432/sonrisasana`)
- `SONRISA_DB_USER`
- `SONRISA_DB_PASS`
- `AZURE_STORAGE_CONNECTION_STRING`
- `AZURE_STORAGE_CONTAINER` (ej: `archivos-clinicos`)

En local puedes usar un archivo `.env` en la raíz del proyecto. El código del módulo `config.AzureEnv` las lee y, si existen, sobreescribe la configuración por defecto.
