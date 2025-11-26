# Despliegue en Azure – Sonrisa Sana

Este documento resume un flujo sencillo para desplegar el WAR en Azure.

## Opción 1: Azure App Service (Linux) + WildFly

1. Crear un **Azure App Service**:
   - Runtime stack: `Java 21`
   - Contenedor personalizado WildFly o App Service con WildFly preconfigurado (por ejemplo, imagen de Docker Hub).

2. Configurar variables de entorno en el App Service:
   - `SONRISA_DB_URL`
   - `SONRISA_DB_USER`
   - `SONRISA_DB_PASS`
   - `AZURE_STORAGE_CONNECTION_STRING`
   - `AZURE_STORAGE_CONTAINER`

3. Base de datos:
   - Crear un **Azure Database for PostgreSQL** (Flexible Server).
   - Ajustar reglas de firewall y copiar el host, puerto y credenciales.
   - Actualizar `SONRISA_DB_URL`, por ejemplo:
     `jdbc:postgresql://mi-servidor.postgres.database.azure.com:5432/sonrisasana`

4. Desplegar el WAR:
   - Generar WAR localmente: `mvn clean package`
   - Usar FTP / SCM de Azure o GitHub Actions para copiar `target/SonrisaSana.war` al directorio de despliegue de WildFly dentro del App Service.

## Opción 2: Máquina virtual en Azure

1. Crear una VM Linux con Docker.
2. Levantar WildFly en Docker y PostgreSQL en Azure Database o en otro contenedor.
3. Copiar el WAR a la carpeta de despliegue de WildFly (`standalone/deployments`).
4. Configurar variables de entorno en el servicio / contenedor.

## Archivos clínicos en Azure Blob Storage

- Crear una cuenta de Storage en Azure.
- Crear un contenedor (por ejemplo `archivos-clinicos`).
- Obtener la *connection string* y configurarla en `AZURE_STORAGE_CONNECTION_STRING`.
- El servicio `AzureBlobService` del proyecto sube los archivos y guarda la URL pública (o SAS) en la entidad `ArchivoClinico`.
