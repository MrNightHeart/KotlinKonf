package org.example

import org.example.workshop.examples.coroutineSachbearbeiter.CoroutineSachbearbeiter
import org.example.workshop.examples.coroutineSachbearbeiter.arbeitet
import org.example.workshop.examples.errorSachbearbeiter.ErrorSachbearbeiter
import org.example.workshop.examples.errorSachbearbeiter.exceptionHandlerBeispiel
import org.example.workshop.examples.errorSachbearbeiter.fehlerAusbreitungBeispiel
import org.example.workshop.examples.errorSachbearbeiter.supervisorScopeBeispiel
import org.example.workshop.examples.errorSachbearbeiter.tryCatchBeispiel
import org.example.workshop.examples.flowSachbearbeiter.FlowSachbearbeiter
import org.example.workshop.examples.flowSachbearbeiter.collectBeispiel
import org.example.workshop.examples.flowSachbearbeiter.operatorsBeispiel
import org.example.workshop.examples.flowSachbearbeiter.stateFlowBeispiel
import org.example.workshop.examples.klassischerSachbearbeiter.KlassischerSachbearbeiter
import org.example.workshop.examples.klassischerSachbearbeiter.arbeitet
import org.example.workshop.examples.threadSachbearbeiter.ThreadSachbearbeiter
import org.example.workshop.examples.threadSachbearbeiter.arbeitet


suspend fun main() {
    // Sequentiell arbeiten
//    KlassischerSachbearbeiter(name = "Helmut", alter = 30).arbeitet()
//
//    // Mit Threads arbeiten
//    ThreadSachbearbeiter(name = "Peter", alter = 60).arbeitet()
//
//    // Mit Coroutines arbeiten
//    CoroutineSachbearbeiter(name = "Maria", alter = 45).arbeitet()


    // ________________________________________________________________________________________


    // Error Handling – Beispiel 1: try-catch (bevorzugter Weg)
//    ErrorSachbearbeiter(name = "Anna", alter = 35).tryCatchBeispiel()

    // Error Handling – Beispiel 2: Fehlerausbreitung mit coroutineScope
//    ErrorSachbearbeiter(name = "Klaus", alter = 42).fehlerAusbreitungBeispiel()

    // Error Handling – Beispiel 3: supervisorScope als Fehlergrenze
//    ErrorSachbearbeiter(name = "Brigitte", alter = 50).supervisorScopeBeispiel()

    // Error Handling – Beispiel 4: CoroutineExceptionHandler
//    ErrorSachbearbeiter(name = "Dieter", alter = 55).exceptionHandlerBeispiel()

    // Flows – Beispiel 1: collect (Cold Flow)
//    FlowSachbearbeiter(name = "Sophie", alter = 28).collectBeispiel()

    // Flows – Beispiel 2: Intermediate Operators (map, filter, onEach)
//    FlowSachbearbeiter(name = "Thomas", alter = 33).operatorsBeispiel()

    // Flows – Beispiel 3: StateFlow (Hot Flow)
//    FlowSachbearbeiter(name = "Laura", alter = 40).stateFlowBeispiel()
}