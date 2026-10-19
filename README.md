# InventoryDemo

API REST para gestionar activos (artículos) de un inventario y calcular su depreciación anual.

## Stack

- Java 21, Spring Boot 3.3.6 (Web, Data JPA, Validation), Maven (`./mvnw`)
- Spring Boot Actuator + Micrometer (health y métricas)
- OpenAPI 3 + Swagger UI con springdoc-openapi (`springdoc-openapi-starter-webmvc-ui` 2.6.0)
- H2 en memoria por defecto (`ddl-auto: create-drop`, `open-in-view: false`); en los despliegues con servidor (`docker-compose.yml`, `k8s/`) se usa H2 en modo servidor TCP con la BD en memoria del servidor (`jdbc:h2:tcp://.../mem:inventario;DB_CLOSE_DELAY=-1`), configurado por variables `SPRING_DATASOURCE_*` y `ddl-auto: update`
- Tests: JUnit 5 + AssertJ + Mockito + MockMvc (`spring-boot-starter-test`)

## Diagramas

Diagramas del proyecto (arquitectura, flujos y modelo): [docs/diagramas.md](docs/diagramas.md)

Diagramas de Git: 
  - https://gitdiagram.com/oscbohr/InventarioDemo 
  - https://gitingest.com/oscbohr/InventarioDemo)

## Comandos

```bash
./mvnw test                 # ejecutar todos los tests
./mvnw spring-boot:run      # levantar la API
```

## URLs

Con la aplicación corriendo en el puerto por defecto (`8080`):

| Qué | URL |
|---|---|
| **Swagger UI** | http://localhost:8080/swagger-ui.html (redirige a `/swagger-ui/index.html`) |
| **OpenAPI 3 (JSON)** | http://localhost:8080/v1/api-docs |
| **Actuator: health** | http://localhost:8080/actuator/health |
| **Actuator: info** | http://localhost:8080/actuator/info |
| **Actuator: lista de métricas** | http://localhost:8080/actuator/metrics |
| Métrica: artículos creados | http://localhost:8080/actuator/metrics/inventario.articulos.creados |
| Métrica: artículos dados de baja | http://localhost:8080/actuator/metrics/inventario.articulos.baja |
| Métrica: tiempo de cálculo de depreciación | http://localhost:8080/actuator/metrics/inventario.depreciacion.calculo |
| Métrica: registros de depreciación generados | http://localhost:8080/actuator/metrics/inventario.depreciacion.registros |
| Métrica: peticiones HTTP | http://localhost:8080/actuator/metrics/http.server.requests |
| API REST | http://localhost:8080/api/articulos |

Las métricas `inventario.*` aparecen después de la primera operación (antes devuelven 404). Las rutas de Swagger y OpenAPI se configuran en `springdoc.*` de `application.yml`.

## Reglas de negocio

- Un artículo nuevo nace activo; `DELETE` es baja lógica (`activo = false`). Un artículo dado de baja no se puede actualizar (`ArticuloInactivoException` → 409).
- `valorCompra` debe ser > 0; los montos se guardan con escala 2 y redondeo `HALF_UP`.
- Depreciación: se calcula un registro por año, desde el año de compra hasta el año actual (según `Clock`), y se guarda con el porcentaje usado y el valor en libros (`vrActualArticulo`) al inicio de ese año. Termina cuando el valor depreciado llega a 0; nunca supera el valor en libros.
- Se recalcula al crear un artículo y al actualizarlo **solo si cambian `valorCompra` o `fechaCompra`** (borra y regenera los registros).
- El porcentaje debe estar entre 0 y 100 (ambos incluidos); si no, `PorcentajeInvalidoException`. Se valida en los constructores de `ConfiguracionDepreciacion` y `DepreciacionArticulo`.
- Configuración de depreciación: viene **solo** de `application.yml` (`depreciacion.porcentaje`, `depreciacion.metodo`). Cambiarla requiere reiniciar y no recalcula depreciaciones ya registradas. Métodos disponibles: 
  - `LINEA_RECTA` (monto fijo: porcentaje sobre el valor de compra)
  - `SALDO_DECRECIENTE` (porcentaje sobre el valor en libros de cada año).


## Despliegue

- Local (Maven, JAR o Docker): [docs/despliegue-local.md](docs/despliegue-local.md)
- Kubernetes (kind/minikube/Docker Desktop): [docs/despliegue-kubernetes.md](docs/despliegue-kubernetes.md)
- Explicación de cada manifiesto de `k8s/`: [docs/k8s-manifiestos.md](docs/k8s-manifiestos.md)

## Estructura del proyecto

```
inventoryDemo/
├── README.md
├── pom.xml
├── Dockerfile
├── docker-compose.yml                  # API + servidor H2 (modo TCP); define también la imagen de H2
├── k8s/                                # manifiestos Kubernetes
├── docs/
│   ├── diagramas.md
│   ├── despliegue-local.md
│   ├── despliegue-kubernetes.md
│   └── k8s-manifiestos.md
├── postman/
│   └── ArticuloController.postman_collection.json
└── src/
    ├── main/
    │   ├── java/com/inventory/activos/
    │   │   ├── InventoryDemoApplication.java
    │   │   ├── domain/                         # núcleo: reglas de negocio
    │   │   │   ├── Articulo.java
    │   │   │   ├── DepreciacionArticulo.java
    │   │   │   ├── ConfiguracionDepreciacion.java
    │   │   │   ├── MetodoDepreciacion.java
    │   │   │   ├── estrategia/                 # Strategy de depreciación
    │   │   │   │   ├── EstrategiaDepreciacion.java
    │   │   │   │   ├── EstrategiaFactory.java
    │   │   │   │   ├── LineaRecta.java
    │   │   │   │   └── SaldoDecreciente.java
    │   │   │   └── excepciones/                # ExcepcionNegocio y derivadas
    │   │   ├── application/                    # casos de uso + puertos
    │   │   │   ├── ArticuloService.java
    │   │   │   ├── DepreciacionService.java
    │   │   │   └── port/                       # interfaces (entrada y salida)
    │   │   └── infrastructure/                 # adaptadores
    │   │       ├── web/                        # REST (entrada)
    │   │       ├── metrics/                    # MicrometerMetricasAdapter (salida)
    │   │       ├── persistence/                # JPA/H2 (salida)
    │   │       │   ├── adapter/                # implementaciones de los puertos *Repository
    │   │       │   └── repository/             # interfaces Spring Data (*JpaRepository)
    │   │       └── config/                     # AppConfig, OpenApiConfig, DepreciacionProperties
    │   └── resources/application.yml
    └── test/java/com/inventory/activos/        # refleja la estructura de main
        ├── domain/  application/
        └── infrastructure/{web,persistence,config,flujo}/
```

## API

Base: `/api/articulos`

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| POST | `/` | Crear (y calcular depreciación) | 201 |
| PUT | `/{id}` | Actualizar | 200 |
| DELETE | `/{id}` | Dar de baja | 204 |
| GET | `/{id}` | Obtener | 200 |
| GET | `/` | Listar | 200 |
| GET | `/{id}/depreciaciones` | Artículo con sus depreciaciones por año (`ArticuloDepreciacionesResponse`) | 200 |

Errores (`ProblemDetail`): 404 artículo no encontrado, 409 artículo inactivo, 400 validación (`errores` por campo) o cuerpo ilegible.

## Métricas (Actuator / Micrometer)

- Endpoints expuestos (`management.endpoints.web.exposure.include`): `/actuator/health`, `/actuator/info`, `/actuator/metrics`.
- Tag común `application` en todas las métricas. Automáticas: `jvm.*`, `http.server.requests`, `hikaricp.*`, `tomcat.*`.
- Métricas de negocio (consultar con `/actuator/metrics/<nombre>`): `inventario.articulos.creados`, `inventario.articulos.baja`, `inventario.depreciacion.calculo` (timer) e `inventario.depreciacion.registros` (registros anuales generados).
- Tests: `MicrometerMetricasAdapterTest`, `ActuatorMetricasTest` y la verificación de rollback en `ArticuloRollbackTest`.

## Documentación OpenAPI 3 (Swagger)

- Contrato JSON: `/v1/api-docs` (`springdoc.api-docs.path`) · Swagger UI: `/swagger-ui.html`.
- Metadatos generales en `infrastructure/config/OpenApiConfig`; rutas configuradas bajo `springdoc.*` en `application.yml`.
- Todo endpoint nuevo en `web/` debe llevar `@Operation` y `@ApiResponses` (incluyendo los códigos de error, con `ProblemDetail` como esquema); el controlador lleva `@Tag`.
- `OpenApiTest` verifica que el contrato y la UI estén disponibles.

## Documentación

- `docs/diagramas.md`: diagramas del proyecto.
- `docs/despliegue-local.md` y `docs/despliegue-kubernetes.md`: guías de despliegue paso a paso.
- `docs/k8s-manifiestos.md`: explicación de cada manifiesto de `k8s/`.
- `postman/ArticuloController.postman_collection.json`: colección de Postman (endpoints, ejemplos de respuesta, tests y rutas de OpenAPI/Swagger). Mantenerla sincronizada al cambiar la API.
