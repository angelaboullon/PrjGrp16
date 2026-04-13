---
name: "🚀 Nueva Historia de Usuario"
about: "Plantilla guiada para crear tareas del Sprint siguiendo el estándar de la práctica."
title: "HU[00]: [Escribe aquí un resumen corto]"
labels: "enhancement"
assignees: ""
---

> 💡 **Instrucciones:** Sustituye todos los textos que están entre corchetes `[ ]` con la información de la nueva tarea. Cuando termines, borra este bloque amarillo.

## 👤 1. Definición de la Historia

| Rol (Quién lo pide) | Acción (Qué necesita) | Beneficio (Para qué lo quiere) |
| :--- | :--- | :--- |
| **Como** `[Ej: Administrador]` | **Quiero** `[Ej: registrar una máquina y sus coordenadas]` | **Para** `[Ej: localizarla en el mapa del inventario]` |

---

## ✅ 2. Criterios de Aceptación (Condiciones de Éxito)
*Añade las condiciones exactas que deben cumplirse para dar esta tarea por válida.*

- [ ] **Condición 1:** `[Ej: El sistema permite crear una máquina con un ID único]`
- [ ] **Condición 2:** `[Ej: Si la máquina no existe al buscarla, lanza una excepción]`
- [ ] **Condición 3:** `[Añade más si es necesario, o borra esta línea]`

---

## 🚦 3. Checklist de Desarrollo y Calidad (IEEE 829)
*El equipo debe marcar estas casillas a medida que la tarea avanza por las columnas del tablero Kanban.*

### 🛠️ Fase de Desarrollo (Columnas: `Codificación` ➡️ `DiseñoPruebas`)
- [ ] **1. Diseño de Código:** Diagramas y excepciones documentados en la Memoria (Sección 1).
- [ ] **2. Diseño de Pruebas:** Objetivo y alcance documentados en la Memoria (Sección 3).
- [ ] **3. Cambio de Rol:** La persona responsable de programar **ha pasado el relevo** a otro compañero para hacer las pruebas.

### 🧪 Fase de Testing (Columnas: `CajaNegra` ➡️ `CajaBlanca`)
- [ ] **4. Caja Negra:** Técnica de Equivalencia (CE) documentada en el Anexo 5.1 y programada (JUnit).
- [ ] **5. Cobertura (Eclemma):** Se ha ejecutado el análisis de cobertura en Eclipse.
- [ ] **6. Caja Blanca:** *(Solo si la cobertura no es del 100%)* Técnica de McCabe aplicada en Anexo 5.2 y programada.

---

## 🛑 4. Control Final de Reglas de la Práctica
*Antes de mover la tarjeta a la columna `Entregada`, verifica esto:*

- [ ] 💾 **Sin Persistencia Real:** Todo funciona en memoria con listas/DAOs simulados.
- [ ] 🔇 **Sin Prints:** No hay ningún `System.out.println` (los errores lanzan Excepciones).
- [ ] 🚫 **Sin Main:** No existe ningún método `public static void main` en el código.
- [ ] 🚀 **Commit:** Se ha subido el código final a la rama principal de GitHub.
