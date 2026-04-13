---
name: Plantilla de Historia de Usuario
about: Plantilla estándar para las tareas del Sprint
title: 'HU[XX]: [Título de la funcionalidad]'
labels: 'enhancement'
assignees: ''
---

## 1. Descripción

**Como** `[rol del usuario]`
**Quiero** `[acción o funcionalidad]`
**Para** `[beneficio o valor esperado]`

---

## 2. Criterios de Aceptación

- [ ] `[Condición 1: ej. El sistema debe...]`
- [ ] `[Condición 2: ej. Si ocurre X, debe lanzar una excepción...]`
- [ ] `[Condición 3]`

---

## 3. Seguimiento Kanban y Pruebas
*(Marcar según avanza la tarjeta por el tablero)*

**Diseño y Codificación**
- [ ] Diseño de clases y datos documentado en la Memoria (Sección 1).
- [ ] Diseño de la prueba documentado en la Memoria (Sección 3).
- [ ] Cambio de rol: la persona que hace las pruebas NO es la misma que programó.

**Caja Negra y Blanca**
- [ ] **Caja Negra:** Técnica de Equivalencia (CE) en el Anexo 5.1 y programada en JUnit.
- [ ] **Cobertura:** Eclemma ejecutado y comprobado.
- [ ] **Caja Blanca:** (Solo si no hay 100% de cobertura) McCabe aplicado en el Anexo 5.2 y pruebas extra codificadas.

---

## 4. Check Final (Reglas de la Práctica)

- [ ] Todo funciona en memoria con listas/DAOs simulados (sin persistencia real).
- [ ] No hay prints por consola (`System.out.println`), solo Excepciones.
- [ ] No hay ningún método `main` en todo el código.
- [ ] Se ha hecho el commit y push en GitHub.
