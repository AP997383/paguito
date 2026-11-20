# Sincronización iOS y Android — 2.0.0

Las aplicaciones comparten Firestore y el servicio `obtenerDatosCompletos` del entorno compilado. No se comunican directamente entre teléfonos. La versión anterior podía descargar solo registros nuevos y conservar copias locales desactualizadas; iOS no descargaba el conjunto completo desde Inicio.

## Recepción de cambios

- Ambas apps escuchan exclusivamente `deudores/{cuenta}/deudores`, `productos/{cuenta}/productos` y el perfil `users/{cuenta}` mientras están visibles. Se retiran las escuchas en segundo plano o al cambiar de cuenta.
- Una modificación real agrupa los avisos cercanos durante 650 ms y actualiza la caché desde el servicio. Una lectura no vuelve a escribir en Firestore.
- Las consultas simultáneas y las visitas repetidas a Inicio reutilizan la descarga correcta durante 30 segundos. Los cambios recibidos y el gesto de actualizar permiten saltar esta caducidad. Los errores conservan la caché anterior y permiten reintentar.
- Los cambios de sesión se verifican después de las operaciones asíncronas. La cuenta anterior no puede completar una importación dentro de la nueva.
- Las pantallas iOS recargan los datos locales tras aplicar la instantánea; Android observa Room. La recepción requiere conexión y permisos de Firestore; no promete sincronización instantánea sin red.

## Identidad y conservación

Una importación actualiza por cuenta y documento remoto, conserva los IDs locales existentes y distingue los movimientos por cliente, colección y documento. Ventas y pagos pueden tener el mismo ID sin ocupar la misma fila. Se rechazan identidades vacías o duplicadas antes de sustituir una instantánea válida. iOS usa una transacción SwiftData; Android una transacción Room.

Los clientes retirados usan `isDeleted=true`: no vuelven a la lista activa y conservan su historial en el servicio. Los cinco clientes históricos confirmados por el usuario quedaron retirados mediante este marcador, conservando todos sus importes e historiales. No se reconstruyen saldos a partir de documentos de clientes retirados.

## Importes y escrituras

Se conservan los saldos negativos, centavos y métodos de pago originales. Android migra Room 1→2→3 sin borrar los datos y admite importes de movimientos con decimales. iOS añade `PaymentEntity.paymentMethod=1` con valor predeterminado compatible con registros anteriores y lee el JSON histórico sin ese campo.

Los disparadores existentes del servicio aplican el saldo al crear o eliminar un pago o una venta. La creación de un cliente con deuda inicial publica cliente con saldo cero y venta `initial-sale` en el mismo lote: el servicio aplica el importe una sola vez. Editar un contacto publica sus campos de contacto, sin reescribir un saldo tomado de la caché. Una descarga o la recepción de un movimiento ya importado tampoco vuelve a aplicar el importe.

## Detalles de venta entre plataformas

iOS admite productos planos guardados por Android y conserva su codificación histórica al escribir. Android lee productos anidados de iOS y sus cantidades además de su propio formato plano. Los campos de productos del cliente admiten `idsProductos` y la clave histórica `idsPorductos`; iOS mantiene ambas al publicar. Estos cambios no recalculan el importe de los movimientos.

## Guía y proporciones

La guía de Inicio usa cinco objetivos reales, recorta una sola zona, reserva espacio según la altura del texto y oculta la navegación de tabs hasta terminar u omitir. Campos, botones y tarjetas conservan tamaños táctiles proporcionados; los formularios y botones inferiores limitan su ancho en iPad. La barra de recordatorios permanece visible también en Premium, con la etiqueta Ilimitados.

## Verificación reproducible

- `swift test --scratch-path /tmp/paguito-ios-domain-redesign-tests --cache-path /tmp/paguito-spm-cache`: contratos, saldos, caducidad, sesiones y meses de análisis.
- `python3 tools/sync-tests/run.py`: implementación SwiftData real en memoria; importaciones repetidas, rollback, colisiones entre cuentas, clientes retirados, IDs, centavos y métodos.
- Android `python3 tools/test_product_account_migration.py`: ejecuta el SQL real y compara el esquema resultante con Room. `--database <copia>` verifica una copia de una instalación previa sin modificarla.
- UI tests usan exclusivamente el simulador de revisión y una cuenta sintética. No crean ventas, pagos ni recordatorios en producción.
