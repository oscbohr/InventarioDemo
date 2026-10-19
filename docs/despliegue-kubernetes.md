# Despliegue en Kubernetes (clúster local)

Guía para desplegar la API en un clúster Kubernetes local (kind, minikube o Docker Desktop) desde Windows, Linux o macOS. No requiere registry: la imagen se carga directamente al clúster.

Arquitectura: la base de datos H2 corre en su **propio pod** (modo servidor TCP, puerto 9092) con la base **en memoria** (`jdbc:h2:mem`), y la API se conecta a ella. Así la API puede escalar a varias réplicas compartiendo los mismos datos.

> **Persistencia:** los datos viven en la memoria del pod `h2-server`. Sobreviven a reinicios y actualizaciones de la API, pero se pierden si el pod `h2-server` se reinicia o se elimina.

```
API (Deployment, N réplicas) ──jdbc:h2:tcp://h2-server:9092/mem:inventario──► h2-server (Deployment, 1 réplica, BD en memoria)
```

Manifiestos en `k8s/`: `namespace.yaml`, `h2-secret.yaml`, `configmap.yaml`, `h2-service.yaml`, `h2-deployment.yaml`, `deployment.yaml`, `service.yaml`.

> **Credenciales:** `k8s/h2-secret.yaml` trae usuario y contraseña de ejemplo. Cámbialos antes de desplegar en algo más que una prueba local. H2 crea la base con estas credenciales en la primera conexión.

## 1. Prerrequisitos

| Herramienta | Windows | macOS | Linux |
|---|---|---|---|
| Docker | Docker Desktop | Docker Desktop | Docker Engine |
| kubectl | `winget install Kubernetes.kubectl` | `brew install kubectl` | https://kubernetes.io/docs/tasks/tools/ |
| kind (opción 1) | `winget install Kubernetes.kind` | `brew install kind` | https://kind.sigs.k8s.io/docs/user/quick-start/#installation |
| minikube (opción 2) | `winget install Kubernetes.minikube` | `brew install minikube` | https://minikube.sigs.k8s.io/docs/start/ |

Alternativa sin instalar nada más: **Docker Desktop → Settings → Kubernetes → Enable Kubernetes**.

Verifica:

```bash
docker version
kubectl version --client
```

## 2. Crear el clúster (elige uno)

```bash
# kind
kind create cluster --name inventory

# minikube
minikube start

# Docker Desktop: ya está creado; selecciona el contexto
kubectl config use-context docker-desktop
```

Comprueba que responde: `kubectl get nodes` (debe mostrar `Ready`).

## 3. Construir las imágenes

Desde la raíz del proyecto (igual en todos los SO):

```bash
docker compose build
```

Esto genera las dos imágenes definidas en `docker-compose.yml`: `inventory-demo:0.0.1` (API, desde el `Dockerfile`) e `inventory-h2:2.2.224` (servidor H2). Requiere Docker Compose v2.17 o superior.

La versión de H2 del servidor debe coincidir con la del cliente de la API (2.2.224, la que trae Spring Boot 3.3.6).

## 4. Cargar las imágenes en el clúster

| Clúster | Comando |
|---|---|
| kind | `kind load docker-image inventory-demo:0.0.1 inventory-h2:2.2.224 --name inventory` |
| minikube | `minikube image load inventory-demo:0.0.1` y `minikube image load inventory-h2:2.2.224` |
| Docker Desktop | No requiere paso (comparte el daemon de Docker) |

## 5. Desplegar

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/h2-secret.yaml -f k8s/configmap.yaml
kubectl apply -f k8s/h2-service.yaml -f k8s/h2-deployment.yaml
kubectl rollout status deployment/h2-server -n inventory
kubectl apply -f k8s/deployment.yaml -f k8s/service.yaml
```

## 6. Verificar

```bash
kubectl get pods -n inventory                          # h2-server y la API en Running 1/1
kubectl rollout status deployment/inventory-demo -n inventory
kubectl logs -n inventory deployment/inventory-demo
```

El pod de la API espera a que H2 esté disponible (*initContainer*) y tarda ~30-60 s en estar `READY` (la *readiness probe* consulta `/actuator/health`).

## 7. Acceder a la API

```bash
kubectl port-forward -n inventory svc/inventory-demo 8080:80
```

Deja esa terminal abierta y, en otra:

```bash
curl http://localhost:8080/actuator/health        # {"status":"UP"}
```

(PowerShell: `Invoke-RestMethod http://localhost:8080/actuator/health`.)

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI 3: http://localhost:8080/v1/api-docs

Prueba de crear un artículo: ver la sección 5 de [despliegue-local.md](despliegue-local.md).

### Acceso opcional a H2 desde tu máquina

```bash
kubectl port-forward -n inventory svc/h2-server 9092:9092
```

Con un cliente SQL (DBeaver, IntelliJ) usa `jdbc:h2:tcp://localhost:9092/mem:inventario;DB_CLOSE_DELAY=-1` y las credenciales del Secret. Debe ser un cliente H2 2.2.224.

### Escalar la API

```bash
kubectl scale deployment/inventory-demo -n inventory --replicas=2
```

Ambas réplicas leen y escriben en la misma base. `h2-server` siempre es 1 réplica: H2 no admite varias instancias sobre el mismo archivo.

## 8. Cambiar la configuración de depreciación

Edita `k8s/configmap.yaml` (`DEPRECIACION_PORCENTAJE`, `DEPRECIACION_METODO`) y aplica:

```bash
kubectl apply -f k8s/configmap.yaml
kubectl rollout restart deployment/inventory-demo -n inventory
```

El reinicio es necesario porque la configuración se lee al arrancar. Los datos se conservan (están en el pod `h2-server`, que no se reinicia) y las depreciaciones ya registradas no se recalculan.

## 9. Actualizar la aplicación

Tras cambiar código, reconstruye con una etiqueta nueva (p. ej. `0.0.2`), cárgala en el clúster (paso 4), cambia `image:` en `k8s/deployment.yaml` y ejecuta `kubectl apply -f k8s/deployment.yaml`. La base de datos no se toca.

## 10. Limpieza

```bash
kubectl scale deployment --all --replicas=0 -n inventory
kubectl wait --for=delete pod --all -n inventory --timeout=60s
kubectl delete namespace inventory   # elimina todo (los datos se pierden)
kind delete cluster --name inventory     # o: minikube delete
```

## 11. Problemas comunes

| Síntoma | Causa y solución |
|---|---|
| `ErrImagePull` / `ImagePullBackOff` | La imagen no está en el clúster. Repite el paso 4 (son dos imágenes) y verifica que nombre y etiqueta coinciden con los manifiestos. En el `initContainer` también puede fallar la descarga de `busybox:1.36`. |
| Pod de la API en `Init:0/1` | Espera a `h2-server`. Revisa `kubectl get pods -n inventory` y `kubectl logs -n inventory deployment/h2-server`. |
| Se perdieron los datos | `h2-server` se reinició (la BD está en memoria). Es esperado; la API no los recupera. |
| API en `CrashLoopBackOff` con `Wrong user name or password` | Las credenciales se fijan en la primera conexión tras arrancar `h2-server`. Restaura las del Secret o reinicia `h2-server` (`kubectl rollout restart deployment/h2-server -n inventory`) y luego la API. |
| Error de versión (`Driver does not support the database version` / protocolo) | El servidor H2 no es 2.2.224. Corrige la versión en el `dockerfile_inline` de `docker-compose.yml` y ejecuta `docker compose build h2`. |
| `CrashLoopBackOff` | Revisa `kubectl logs -n inventory deployment/inventory-demo`. Causa típica: porcentaje fuera de 0–100 o método inválido en el ConfigMap. |
| Pod `Running` pero `0/1` READY | Aún arrancando, o memoria insuficiente (`kubectl describe pod -n inventory`). Si es `OOMKilled`, sube `limits.memory`. |
| `port-forward` falla por puerto ocupado | Usa otro puerto local: `8081:80`. |
| `kubectl` apunta a otro clúster | `kubectl config get-contexts` y `kubectl config use-context <nombre>`. |
