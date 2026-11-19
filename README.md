# paguito
app para cobradores

## Autenticación automática de Nexus Payments

Al sincronizar o compilar con conexión, Gradle obtiene un token nuevo de AWS CodeArtifact usando AWS CLI. La credencial fija `nexusCodeArtifactToken` que dejó `nexus-login` se ignora en este modo. La renovación no escribe tokens en archivos versionados ni en `gradle.properties`; la salida del comando se captura para no mostrar la credencial en los logs.

Se utiliza el perfil AWS habitual. Para seleccionar otro perfil o resolver AWS CLI desde Android Studio, se pueden añadir estas propiedades al `gradle.properties` del usuario de Gradle:

```properties
nexusAwsProfile=mi-perfil
nexusAwsExecutable=/usr/local/bin/aws
```

Ambas propiedades son opcionales. El ejecutable se busca en PATH y en las ubicaciones habituales de macOS. También se respetan `AWS_PROFILE`, `AWS_DEFAULT_PROFILE` y `AWS_CLI_EXECUTABLE`.

La sesión AWS debe estar vigente y autorizar la descarga del SDK. Si esa sesión SSO vence, Gradle indica el comando `aws sso login` correspondiente; renovar el token de CodeArtifact no sustituye la autenticación de la cuenta AWS. Con `--offline`, no se ejecuta AWS CLI y Gradle utiliza las dependencias ya descargadas.

Después de actualizar `settings.gradle.kts` en Android Studio, ejecutar **Sync Project with Gradle Files** antes de volver a compilar.

Pruebas del flujo de autenticación, sin solicitudes a AWS:

```sh
python3 tools/test_codeartifact_auth.py
```

Referencia: [autenticación de Gradle con CodeArtifact](https://docs.aws.amazon.com/codeartifact/latest/ug/maven-gradle.html).

## Revisión de la barra de navegación

`RoundedBottomBar` dibuja el contorno y un único indicador en Canvas, sin elevación ni sombras. Una posición animada de 280 ms sincroniza curva, círculo e iconos; los cambios de selección mantienen métricas de texto estables y los tabs conservan sus roles de accesibilidad.

`FluidBottomBarTest` comprueba que mantener pulsado no modifica los píxeles, que los frames de transición no proyectan halos fuera del contorno y que los cambios rápidos llegan a la ruta seleccionada. Usa rutas vacías y no modifica perfiles ni datos de negocio.

Ejecutar las pruebas visuales en la variante **DEV** y en un emulador o dispositivo de pruebas dedicado:

```sh
bash gradlew :app:connectedDevDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.nexusystem.paguito.ui.FluidBottomBarTest
```

`android.injected.androidTest.leaveApksInstalledAfterRun=true` conserva los APK al finalizar la instrumentación. El valor predeterminado de AGP es false y su limpieza puede desinstalar la app y borrar sus datos; por eso estas pruebas deben mantenerse separadas de la instalación de uso diario.
