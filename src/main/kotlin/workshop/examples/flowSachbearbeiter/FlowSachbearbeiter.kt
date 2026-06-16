package org.example.workshop.examples.flowSachbearbeiter

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.example.workshop.examples.*

data class FlowSachbearbeiter(
    override val name: String,
    override val alter: Int
) : Sachbearbeiter

// ─────────────────────────────────────────────────────────────────────────────
// Cold Flow: Quelle des Bewerbungsprozesses
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Gibt einen Cold Flow zurück, der jeden Bewerbungsschritt als String emittiert.
 *
 * Cold Flow: Es passiert nichts, bis jemand collect aufruft.
 * Für jeden Collector wird die Flow-Logik neu ausgeführt.
 *
 * Wichtig: Flow-erzeugende Funktionen sollten NICHT suspend sein –
 * das Erstellen eines Flows ist schnell. Die suspendierende Arbeit
 * findet im Builder-Lambda statt.
 */
fun FlowSachbearbeiter.bewerbungsprozessFlow(): Flow<String> = flow {
    emit("Stelle ausschreiben")
    schreibtStelleAusSus(this@bewerbungsprozessFlow)

    emit("Auf Bewerbungsende warten")
    wartetAufBewerbungsEndeSus(this@bewerbungsprozessFlow)

    emit("Bewerbungen auswerten")
    wertetBewerbungenAusSus(this@bewerbungsprozessFlow)

    emit("Bewerbungsgespraeche vereinbaren")
    vereinbartBewerbungsgespraecheSus(this@bewerbungsprozessFlow)

    emit("Bewerbungsgespraeche fuehren")
    fuehrtBewerbungsgespraecheSus(this@bewerbungsprozessFlow)

    emit("Beste Kandidaten auswaehlen")
    waehltBesteKandidatenAusSus(this@bewerbungsprozessFlow)

    emit("Hintergrundpruefung einleiten")
    leitetPruefungEinSus(this@bewerbungsprozessFlow)

    emit("Arbeitsvertrag erstellen")
    erstelltArbeitsvertragSus(this@bewerbungsprozessFlow)

    emit("Einstellung abgeschlossen!")
}

// ─────────────────────────────────────────────────────────────────────────────
// Beispiel 1: collect – alle emittierten Werte empfangen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * collect ist ein terminaler Operator – er startet die Ausführung des Flows.
 * Ohne collect passiert im Cold Flow nichts.
 */
suspend fun FlowSachbearbeiter.collectBeispiel() {
    println("--- ${name}: collect Beispiel ---")
    val arbeitsanfang = beginntArbeit(this)

    bewerbungsprozessFlow().collect { schritt ->
        println("  ${name} → $schritt")
    }

    bewerbungAbgeschlossen(this, arbeitsanfang)
}

// ─────────────────────────────────────────────────────────────────────────────
// Beispiel 2: Intermediate Operators – map, filter, onEach
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Intermediate Operators transformieren den Flow, sind lazy und geben wieder
 * einen Flow zurück – die Ausführung startet erst durch einen terminalen Operator.
 *
 * map    → transformiert jeden Wert
 * filter → lässt nur Werte durch, die die Bedingung erfüllen
 * onEach → Seiteneffekt für jeden Wert, ohne ihn zu verändern
 */
suspend fun FlowSachbearbeiter.operatorsBeispiel() {
    println("--- ${name}: Operators Beispiel ---")
    val arbeitsanfang = beginntArbeit(this)

    bewerbungsprozessFlow()
        .onEach { schritt -> println("  ${name} bearbeitet: $schritt") }
        .map { schritt -> "[${name.uppercase()}] $schritt" }
        .filter { it.contains("Kandidaten") || it.contains("abgeschlossen") }
        .collect { meilenstein -> println("  *** Meilenstein: $meilenstein ***") }

    bewerbungAbgeschlossen(this, arbeitsanfang)
}

// ─────────────────────────────────────────────────────────────────────────────
// Beispiel 3: StateFlow – Hot Flow als State Container
// ─────────────────────────────────────────────────────────────────────────────

/**
 * StateFlow ist ein Hot Flow – er ist immer aktiv, auch ohne Collector.
 * Er hält immer genau einen Wert (mit Initialwert) und emittiert nur
 * bei tatsächlicher Änderung (equality-based conflation).
 *
 * Typischer Einsatz: UI-State, aktueller Status einer Komponente.
 *
 * Der Collector läuft in einem eigenen launch { } parallel zum Prozess.
 * Da StateFlow niemals completed, wird der Collector-Job am Ende explizit gecancelt.
 */
suspend fun FlowSachbearbeiter.stateFlowBeispiel() {
    println("--- ${name}: StateFlow Beispiel ---")
    val arbeitsanfang = beginntArbeit(this)

    val bewerbungsStatus = MutableStateFlow("Noch nicht gestartet")

    coroutineScope {
        // Collector laeuft parallel und reagiert auf jeden Status-Wechsel
        val collector = launch {
            bewerbungsStatus.collect { status ->
                println("  [Status] $status")
            }
        }

        // Prozess aktualisiert den State
        bewerbungsStatus.value = "Stelle ausgeschrieben"
        wartetAufBewerbungsEndeSus(this@stateFlowBeispiel)

        bewerbungsStatus.value = "Bewerbungen werden ausgewertet"
        wertetBewerbungenAusSus(this@stateFlowBeispiel)

        bewerbungsStatus.value = "Gespraeche werden gefuehrt"
        fuehrtBewerbungsgespraecheSus(this@stateFlowBeispiel)

        bewerbungsStatus.value = "Einstellung abgeschlossen!"

        // StateFlow completed nie → Collector explizit beenden
        collector.cancel()
    }

    bewerbungAbgeschlossen(this, arbeitsanfang)
}
