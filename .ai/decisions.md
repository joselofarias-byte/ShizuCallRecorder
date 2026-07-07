# Architectural Decisions

## Null safety Application Management

Decisión:
usar mapNotNull cuando applicationInfo pueda ser null.

Motivo:
evitar crashes sin cambiar lógica de autorización.


## TV Loading

Decisión:
no mostrar estado vacío mientras packagesResource está cargando.

Motivo:
evitar parpadeo visual.
