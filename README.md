# API de Gestión de Pólizas

API REST para gestionar pólizas de arrendamiento (individuales y colectivas) y sus riesgos. La hice en Spring Boot 3 con Java 21, organizada en capas (controller, service, repository) y con una base de datos H2 en memoria que se carga con datos de ejemplo al iniciar.

Incluye los endpoints para listar pólizas, consultar riesgos, renovar, cancelar y agregar o cancelar riesgos, las reglas de negocio del enunciado, el mock del CORE (`/core-mock/evento`) y la validación del header `x-api-key`.

## Requisitos

- Java 21
- Maven, o un IDE que lo traiga integrado (por ejemplo, IntelliJ IDEA)

## Cómo ejecutarlo

Desde la raíz del proyecto:

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

Para correr las pruebas:

```bash
mvn test
```

Para usar IntelliJ, abrir la carpeta del proyecto, esperar a que cargue las dependencias de Maven y ejecutar la clase `PolizasApiApplication`. Las pruebas se corren con clic derecho sobre `src/test/java` → *Run 'All Tests'*.

Para usar un puerto distinto al 8080:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

## Cómo probarlo

Todas las peticiones necesitan este header:

```
x-api-key: 123456
```

Algunos ejemplos:

```bash
# Listar pólizas colectivas activas
curl -H "x-api-key: 123456" "http://localhost:8080/polizas?tipo=COLECTIVA&estado=ACTIVA"

# Ver los riesgos de la póliza 2
curl -H "x-api-key: 123456" http://localhost:8080/polizas/2/riesgos

# Renovar la póliza 1
curl -X POST -H "x-api-key: 123456" http://localhost:8080/polizas/1/renovar

# Agregar un riesgo a la póliza colectiva 2
curl -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" \
  -d '{"direccionInmueble":"Calle 10 # 5-20","arrendatario":"Laura Díaz","arrendador":"Andrés Mora"}' \
  http://localhost:8080/polizas/2/riesgos

# Cancelar el riesgo 3
curl -X POST -H "x-api-key: 123456" http://localhost:8080/riesgos/3/cancelar

# Cancelar la póliza 2 (también cancela sus riesgos)
curl -X POST -H "x-api-key: 123456" http://localhost:8080/polizas/2/cancelar
```

Cada operación que cambia el estado de una póliza o un riesgo llama al mock del CORE, y en la consola aparece una línea con `[CORE-MOCK] Evento recibido...`.

Los datos de ejemplo que se cargan al iniciar son:

| id | Número   | Tipo       | Estado    |
|----|----------|------------|-----------|
| 1  | IND-0001 | INDIVIDUAL | ACTIVA    |
| 2  | COL-0001 | COLECTIVA  | ACTIVA    |
| 3  | IND-0002 | INDIVIDUAL | CANCELADA |