---
name: 📖 Plantilla de Historia de Usuario
about: Usa esta plantilla para crear las funcionalidades del Sprint.
title: 'HU[XX]: [Escribe aquí un título corto y descriptivo]'
labels: 'enhancement'
assignees: ''
---

## 👤 1. Historia de Usuario
*(Sustituye el texto entre corchetes por tu información)*

> **Como** `[Escribe el rol, ej: Administrador del sistema]`
> **Quiero** `[Escribe la acción, ej: registrar una máquina y sus coordenadas]`
> **Para** `[Escribe el beneficio, ej: tenerla localizada en el mapa]`

---

## ✅ 2. Criterios de Aceptación (Criterio de Paso/Fallo)
[cite_start]*(Añade las condiciones exactas para que esta historia se considere "Superada" [cite: 156])*

- [ ] **C1:** `[Ej: El sistema permite crear una máquina con un ID único]`
- [ ] **C2:** `[Ej: Si la máquina no existe al buscarla, lanza una excepción]`
- [ ] **C3:** `[Ej: La información se guarda en la lista del DAO simulado]`

---

## 🏗️ 3. Trazabilidad y Pruebas (IEEE 829)
[cite_start]*(El equipo debe marcar estas casillas a medida que la tarea avanza por el Kanban [cite: 187-197])*

**Fase de Codificación y Diseño (`DoneC` ➡️ `DoneP`)**
- [ ] [cite_start]**Código:** Clases, métodos y excepciones documentados en la Memoria (Sección 1) [cite: 124-129].
- [ ] [cite_start]**Diseño Pruebas:** Objetivo y clases a probar descritas en la Memoria (Sección 3) [cite: 242-269].
- [ ] [cite_start]**Rotación:** El responsable de codificar ha pasado el relevo a otro compañero para las pruebas [cite: 152-154].

**Fase de Pruebas (`DoneCN` ➡️ `DoneCB`)**
- [ ] [cite_start]**Caja Negra:** Técnica de Clases de Equivalencia (CE) aplicada en el Anexo 5.1 y programada en JUnit[cite: 149, 191, 302].
- [ ] [cite_start]**Cobertura:** Ejecutado Eclemma en Eclipse para comprobar la cobertura de decisión[cite: 150].
- [ ] [cite_start]**Caja Blanca:** (Si aplica) Técnica de McCabe en el Anexo 5.2 y caminos extra programados[cite: 150, 192, 304].

---

## 🚀 4. Check Final (Reglas de Oro)
*(Antes de mover a la columna de `Entregada`)*

- [ ] [cite_start]🛑 **NO** hay bases de datos reales ni ficheros (todo está en memoria) .
- [ ] [cite_start]🛑 **NO** hay `System.out.println` para errores (se usan excepciones).
- [ ] [cite_start]🛑 **NO** hay método `main`[cite: 137].
- [ ] [cite_start]✅ Se ha hecho el `commit` correspondiente en GitHub[cite: 178].
