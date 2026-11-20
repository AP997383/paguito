# Productos separados por cuenta

Room pasa de versión 1 a 2 sin eliminar filas: `ownerAccount` identifica el propietario mediante una huella del correo normalizado. Invitado tiene su propio identificador. Las consultas de productos, conteos, inversión y nombres filtran por esa identidad; las bajas exigen identidad y clave local. La migración se registra en ambas construcciones de AppDatabase.

Las filas históricas conservan sus IDs y contenido. Su propietario vacío no coincide con ninguna cuenta. Solo se recuperan al comprobar su ID remoto en la colección de productos de la cuenta o en la descarga autenticada correspondiente. Sin comprobación permanecen conservadas y ocultas. La identidad local no se envía a Firestore.

El repositorio cambia de flujo al cambiar la cuenta y la interfaz filtra también su lista retenida. Conteo y selectores usan la misma lista. Las respuestas pendientes se vinculan al correo que inició la solicitud y se rechazan si cambia la sesión. La publicación desde Productos usa una instantánea del propietario esperado. Cerrar sesión o entrar como invitado retira el perfil cifrado; una actualización tardía de otro perfil no puede reactivarlo.

Validación del 19 de noviembre de 2026:

- Compilación y APK DEV correctos; 19 pruebas unitarias aprobadas, incluidas ocho de aislamiento.
- `python3 tools/test_product_account_migration.py`: ejecuta la migración y SQL reales en SQLite aislado, conserva filas históricas, verifica columnas contra Room generado y bloquea lectura/borrado de otra cuenta.
- Lint completo detecta seis errores existentes en AuthSheetScaffold y uso del padding de Scaffold en otras pantallas, además de advertencias previas. No se agregó una línea base ni se suprimieron diagnósticos para ocultarlos.
- La validación no instala ni desinstala aplicaciones ni modifica las cuentas de un dispositivo.

La corrección del sitio web se publica en el servidor y no exige actualizar Android. Esta separación de productos sí requiere una compilación nueva de la aplicación; no se ha publicado en una tienda desde este trabajo.
