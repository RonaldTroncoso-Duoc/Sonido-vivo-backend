@ECHO OFF
SETLOCAL
SET "WRAPPER_JAR=%~dp0.mvn\wrapper\maven-wrapper.jar"
IF NOT EXIST "%WRAPPER_JAR%" (
  ECHO Maven Wrapper no encontrado. Ejecute Docker o instale Maven 3.9+ para generar el wrapper.
  EXIT /B 1
)
SET "MAVEN_PROJECTBASEDIR=%~dp0."
java -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*