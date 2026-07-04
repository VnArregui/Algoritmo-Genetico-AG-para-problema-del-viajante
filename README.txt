# AE-1: PROBLEMA DEL VIAJANTE - ALGORITMO GENETICO

Resolución del problema del viajante utilizando un *Algoritmo genético*
Implementado en Java 25 y testeado utilizando TSPLib kro100A

# Ejecución

Primero compilar todo el proyecto:

    javac -d out src/Main.java src/model/*.java src/ea/*.java

## Main (ejecución unica)

Correr main para ejecución unica con los parámetros asignados en el mismo.

    java -cp out Main

## AlgorithmTest (banco de pruebas)

Para testeo extensivo se recomienda usar AlgorithmTest.java, donde se implemento un banco de pruebas
definir configuraciones a testear y los parámetros adicionales.

    java -cp out model.AlgorithmTest

Los resultados se dan por consola y un archivo con formato csv llamado "test-results.csv"

# Avisos

El algoritmo se implemento con asistencia de Claude Sonnet y Claude Opus, su participación se llevo a cabo
durante el testeo, analizando el código del algoritmo en búsqueda de errores y para generar el código del banco de pruebas
para facilitar el testeo y que además sea mas fácil de usar para los interesados.

Se utilizaron los modelos de IA mencionados como asistencia para resolver dudas acerca de java y consultas de fuentes 
acerca del tema (Bibliotecas o código ya existente acerca de los métodos utilizados), los dos casos donde me apoye
en la IA generativa fue, utilizando le herramienta de autocompletado de GitHub Copilot, y en la construcción del método de cruce DPX (distance preserving crossover), con la intención de llegar a la mejor implementación del método en un tiempo razonable.
