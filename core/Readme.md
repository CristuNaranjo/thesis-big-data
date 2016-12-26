Instrucciones para generar el código

  1 - Cambiar en el fichero /src/main/scala/App.scala el import (primeras lineas) que se desea utilizar en la versión de análisis

  2 - (Opcional) Cambiar en el fichero /pom.xml la versión de la app (línea 9) para identificarla mejor

  3 - Ejecutar el comando "mvn package" en la carpeta del proyecto (core) y generara tres ficheros .jar en la carpeta /core/target . El fichero que contiene toda la aplicación es el fichero llamado tfg-core-versión(paso 2)-allinone.jar

  4 - Mover el JAR generado a la carpeta web y renombrarlo con el nombre core.jar
