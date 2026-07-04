# AE-1: PROBLEMA DEL VIAJANTE - ALGORITMO GENETICO

Resolucion del problema del viajante utilizando un *Algoritmo genetico*
Implementado en Java 25 y testeado utilizando TSPLib kro100A

# Ejecucion

Primero compilar todo el proyecto:

    javac -d out src/Main.java src/model/*.java src/ea/*.java

## Main (ejecucion unica)

Correr main para ejecucion unica con los parametros asignados en el mismo.

    java -cp out Main

## AlgorithmTest (banco de pruebas)

Para testeo extensivo se recomienda usar AlgorithmTest.java, donde se implemento un banco de pruebas
definir configuraciones a testear y los parametros adicionales.

    java -cp out model.AlgorithmTest

Los resultados se dan por consola y un archivo con formato csv llamado "test-result.csv"

# Avisos

El algorimo se implemento con asistencia de Claude Sonnet y Claude Opus, su participacion se llevo a cabo
durante el testeo, analizando el codigo del algoritmo en busqueda de errores y para generar el codigo del banco de pruebas
para facilitar el testeo y que ademas sea mas facil de usar para los interesados.

Se utilizaron los modelos de IA mencionados como asistencia para resolver dudas acerca de java y consultas de fuentes 
acerca del tema (Bibliotecas o codigo ya existente acerca de los metodos utilizados), los dos casos donde me apoye
en la IA generativa fue, utilizando le herramienta de autocompletado de Github Copilot, y en la construccion del metodo de cruce DPX (distance preserving crossover), con la intencion de llegar a la mejor implementacion del metodo en un tiempo razonable.
