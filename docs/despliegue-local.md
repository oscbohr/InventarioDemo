# Despliegue local

Guía para ejecutar la API en tu máquina (Windows, Linux o macOS). Puerto por defecto: `8080`.

Hay tres opciones; elige una:

| Opción | Requiere | Cuándo usarla |
|---|---|---|
| A. Maven Wrapper | JDK 21 | Desarrollo diario |
| B. JAR | JDK 21 | Simular un entorno "empaquetado" |
| C. Docker Compose | Docker | No quieres instalar Java; levanta la API con H2 en modo servidor |

## 1. Prerrequisitos

**JDK 21** (opciones A y B). Verifica:

```bash
java -version      # debe mostrar 21.x
```

Instalación si falta:

| SO | Comando |
|---|---|
| Windows | `winget install EclipseAdoptium.Temurin.21.JDK` |
| macOS | `brew install --cask temurin@21` |
| Linux (Debian/Ubuntu) | `sudo apt install openjdk-21-jdk` |

**Docker** con Compose v2 (opción C): Docker Desktop en Windows/macOS, Docker Engine + plugin Compose en Linux. Verifica con `docker compose version`.

Maven **no** hace falta: el proyecto incluye el wrapper (`mvnw` / `mvnw.cmd`).

Obtén el código y entra en la carpeta:

```bash
git clone <url-del-repositorio> inventoryDemo
cd inventoryDemo
```

## 2. Opción A: Maven Wrapper

| SO | Comando |
|---|---|
| Linux / macOS | `./mvnw spring-boot:run` |
| Windows (CMD o PowerShell) | `.\mvnw.cmd spring-boot:run` |

Espera el log `Started InventoryDemoApplication`.

## 3. Opción B: ejecutar el JAR

```bash
# Linux / macOS
./mvnw -DskipTests package
java -jar target/inventarioDemo-0.0.1-SNAPSHOT.jar
```

```powershell
# Windows PowerShell
.\mvnw.cmd -DskipTests package
java -jar target\inventarioDemo-0.0.1-SNAPSHOT.jar
```

### Cambiar la configuración de depreciación

Por argumento (igual en todos los SO):

```bash
java -jar target/inventarioDemo-0.0.1-SNAPSHOT.jar --depreciacion.porcentaje=10 --depreciacion.metodo=SALDO_DECRECIENTE
```

Por variable de entorno:

```bash
# Linux / macOS
DEPRECIACION_PORCENTAJE=10 DEPRECIACION_METODO=SALDO_DECRECIENTE java -jar target/inventarioDemo-0.0.1-SNAPSHOT.jar
```

```powershell
# Windows PowerShell
$env:DEPRECIACION_PORCENTAJE = "10"; $env:DEPRECIACION_METODO = "SALDO_DECRECIENTE"
java -jar target\inventarioDemo-0.0.1-SNAPSHOT.jar
```

Métodos válidos: `LINEA_RECTA`, `SALDO_DECRECIENTE`. El porcentaje debe estar entre 0 y 100; si no, la aplicación no arranca.

## 4. Opción C: Docker Compose

Levanta dos contenedores con un solo comando: `h2` (servidor H2 en modo TCP, puerto 9092, BD en memoria) y `api`, conectada a él. Ambas imágenes se construyen desde `docker-compose.yml` (la API con el `Dockerfile` del proyecto), por eso no hace falta `docker build` ni `docker run`.

```bash
docker compose up --build
```

Para dejarlo en segundo plano: `docker compose up --build -d`.

- Los datos viven en la memoria del contenedor `h2`: sobreviven a reinicios de la API (`docker compose restart api`), pero se pierden si se reinicia o se baja `h2` (`docker compose down`).
- La configuración se define en `environment` del servicio `api` en `docker-compose.yml`: credenciales de ejemplo (`SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`) y, si quieres otra depreciación, agrega `DEPRECIACION_PORCENTAJE` y `DEPRECIACION_METODO` (`SALDO_DECRECIENTE` o `LINEA_RECTA`).
- Cliente SQL externo: agrega `ports: ["9092:9092"]` al servicio `h2` y conéctate a `jdbc:h2:tcp://localhost:9092/mem:inventario;DB_CLOSE_DELAY=-1`.
- Primera construcción: descarga dependencias y tarda varios minutos; las siguientes usan caché. Requiere Docker Compose v2.17 o superior (`docker compose version`).

### Alternativa sin Docker: servidor H2 como proceso

Descarga `h2-2.2.224.jar` (misma versión que usa la API) desde Maven Central y arráncalo:

```bash
java -cp h2-2.2.224.jar org.h2.tools.Server -tcp -tcpAllowOthers -ifNotExists
```

En otra terminal, ejecuta la API apuntando a él:

```bash
# Linux / macOS
SPRING_DATASOURCE_URL="jdbc:h2:tcp://localhost:9092/mem:inventario;DB_CLOSE_DELAY=-1" SPRING_DATASOURCE_USERNAME=inventario \
SPRING_DATASOURCE_PASSWORD=cambiar-esta-clave SPRING_JPA_HIBERNATE_DDL_AUTO=update ./mvnw spring-boot:run
```

```powershell
# Windows PowerShell
$env:SPRING_DATASOURCE_URL = "jdbc:h2:tcp://localhost:9092/mem:inventario;DB_CLOSE_DELAY=-1"; $env:SPRING_DATASOURCE_USERNAME = "inventario"
$env:SPRING_DATASOURCE_PASSWORD = "cambiar-esta-clave"; $env:SPRING_JPA_HIBERNATE_DDL_AUTO = "update"
.\mvnw.cmd spring-boot:run
```

> Sin estas variables, la API usa H2 **en memoria** (las opciones A y B). Con `ddl-auto: create-drop` (el valor por defecto) la BD compartida se borraría al apagar la API, por eso se usa `update`.

## 5. Verificar

```bash
curl http://localhost:8080/actuator/health      # {"status":"UP"}
```

(En PowerShell, si `curl` no funciona como esperas: `Invoke-RestMethod http://localhost:8080/actuator/health`.)

- Swagger UI: http://localhost:8080/swagger-ui.html
- Contrato OpenAPI 3: http://localhost:8080/v1/api-docs

Crear un artículo:

```bash
# Linux / macOS / Git Bash
curl -X POST http://localhost:8080/api/articulos -H "Content-Type: application/json" \
  -d '{"nombre":"Laptop Dell","serial":"SN-0001","fechaCompra":"2024-03-01","valorCompra":3500.00}'
```

```powershell
# Windows PowerShell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/articulos -ContentType "application/json" `
  -Body '{"nombre":"Laptop Dell","serial":"SN-0001","fechaCompra":"2024-03-01","valorCompra":3500.00}'
```
Luego consulta `GET /api/articulos/1/depreciaciones`.

Postman:
```Postman
Desde Postman importe la colección que esta ubicada en  "/postman/ArticuloController.postman_collection.json"
   (Viene preconfigurada con endpoints, ejemplos de response y variables de entorno)
```


## 6. Detener

- Opciones A y B: `Ctrl + C` en la terminal.
- Opción C: `Ctrl + C` si está en primer plano y luego `docker compose down` (borra los contenedores y, con ellos, los datos de H2).

## 7. Problemas comunes

| Síntoma | Causa y solución |
|---|---|
| `Port 8080 was already in use` | Usa otro puerto: `--server.port=8081` (A: `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`; Docker Compose: cambia `"8080:8080"` por `"8081:8080"` en `ports` del servicio `api`). |
| `Permission denied: ./mvnw` (Linux/macOS) | `chmod +x mvnw`. |
| `JAVA_HOME is not defined correctly` (Windows) | Define `JAVA_HOME` con la ruta del JDK 21 y abre una terminal nueva. |
| `release version 21 not supported` | Estás usando un JDK anterior; instala el 21. |
| `mvnw: bad interpreter` / `\r` en Docker (Windows) | El `mvnw` se clonó con saltos CRLF; el repo fuerza LF vía `.gitattributes`, vuelve a clonar o ejecuta `git add --renormalize .`. |
| Los datos desaparecen al reiniciar | Es esperado en las opciones A y B (H2 dentro del proceso). Con la opción C sobreviven a reinicios de la API, no a los del contenedor `h2`. |
| `Connection refused` a `9092` | El servidor H2 no está arrancado, o la API arrancó antes. Reintenta. |
