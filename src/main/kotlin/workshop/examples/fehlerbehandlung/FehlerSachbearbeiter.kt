package org.example.workshop.examples.fehlerbehandlung

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.example.workshop.examples.*

data class FehlerSachbearbeiter(
    override val name: String,
    override val alter: Int
) : Sachbearbeiter

// ─────────────────────────────────────────────────────────────────────────────
// Beispiel 1: coroutineScope  →  Fehler bricht alle Geschwister ab
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Structured Concurrency mit coroutineScope:
 * Schlägt eine Kind-Coroutine fehl, werden alle anderen sofort abgebrochen
 * und die Exception wird an den Aufrufer weitergegeben.
 *
 *   coroutineScope {
 *       launch { leitetPruefungEinSusMitFehler(...) }   ← wirft Exception
 *       launch { erstelltArbeitsvertragSus(...) }        ← wird abgebrochen!
 *   }
 */
suspend fun FehlerSachbearbeiter.arbeitetOhneSupervision() {
    val arbeitsanfang = beginntArbeit(this)

    schreibtStelleAusSus(this)
    wartetAufBewerbungsEndeSus(this)
    wertetBewerbungenAusSus(this)
    vereinbartBewerbungsgespraecheSus(this)
    fuehrtBewerbungsgespraecheSus(this)
    waehltBesteKandidatenAusSus(this)

    try {
        coroutineScope {
            launch { leitetPruefungEinSusMitFehler(this@arbeitetOhneSupervision) }
            launch { erstelltArbeitsvertragSus(this@arbeitetOhneSupervision) }  // wird abgebrochen!
        }
    } catch (e: Exception) {
        println("${name}: Einstellung abgebrochen – ${e.message}")
    }

    bewerbungAbgeschlossen(this, arbeitsanfang)
}

// ─────────────────────────────────────────────────────────────────────────────
// Beispiel 2: supervisorScope  →  Fehler ist isoliert, Geschwister laufen weiter
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Structured Concurrency mit supervisorScope:
 * Jede Kind-Coroutine schlägt unabhängig fehl.
 * Der Fehler muss lokal mit try/catch behandelt werden.
 *
 *   supervisorScope {
 *       launch {
 *           try { leitetPruefungEinSusMitFehler(...) }   ← Fehler lokal abgefangen
 *           catch (e: Exception) { println("Fehler!") }
 *       }
 *       launch { erstelltArbeitsvertragSus(...) }         ← läuft weiter!
 *   }
 */
suspend fun FehlerSachbearbeiter.arbeitetMitSupervision() {
    val arbeitsanfang = beginntArbeit(this)

    schreibtStelleAusSus(this)
    wartetAufBewerbungsEndeSus(this)
    wertetBewerbungenAusSus(this)
    vereinbartBewerbungsgespraecheSus(this)
    fuehrtBewerbungsgespraecheSus(this)
    waehltBesteKandidatenAusSus(this)

    supervisorScope {
        launch {
            try {
                leitetPruefungEinSusMitFehler(this@arbeitetMitSupervision)
            } catch (e: Exception) {
                println("${name}: Pruefung fehlgeschlagen – ${e.message}")
            }
        }
        launch {
            erstelltArbeitsvertragSus(this@arbeitetMitSupervision)  // laeuft weiter!
        }
    }

    bewerbungAbgeschlossen(this, arbeitsanfang)
}
