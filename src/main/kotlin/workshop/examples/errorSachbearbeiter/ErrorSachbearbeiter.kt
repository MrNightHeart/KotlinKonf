package org.example.workshop.examples.errorSachbearbeiter

import kotlinx.coroutines.*
import org.example.workshop.examples.*

data class ErrorSachbearbeiter(
    override val name: String,
    override val alter: Int
) : Sachbearbeiter

// ─────────────────────────────────────────────────────────────────────────────
// Beispiel 1: try-catch (bevorzugter Weg)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * try-catch innerhalb der suspend function – der bevorzugte Weg.
 *
 * Wichtig: try-catch muss den tatsächlich ausführenden Code umschließen,
 * NICHT den Coroutine-Builder-Aufruf (der kehrt sofort zurück!).
 *
 *   // Richtig – fängt die Exception:
 *   launch {
 *       try { fehlerhafteAufgabe() }
 *       catch (e: Exception) { ... }
 *   }
 *
 *   // Falsch – fängt NICHT die Exception (Builder kehrt sofort zurück):
 *   try { launch { fehlerhafteAufgabe() } }
 *   catch (e: Exception) { ... }
 */
suspend fun ErrorSachbearbeiter.tryCatchBeispiel() {
    val arbeitsanfang = beginntArbeit(this)

    schreibtStelleAusSus(this)
    wartetAufBewerbungsEndeSus(this)
    wertetBewerbungenAusSus(this)
    vereinbartBewerbungsgespraecheSus(this)
    fuehrtBewerbungsgespraecheSus(this)
    waehltBesteKandidatenAusSus(this)

    try {
        leitetPruefungEinSusMitFehler(this)
    } catch (e: Exception) {
        println("$name: Pruefung fehlgeschlagen – ${e.message}")
        println("$name: Wechselt zu Ersatzkandidaten.")
    }

    erstelltArbeitsvertragSus(this)
    bewerbungAbgeschlossen(this, arbeitsanfang)
}

// ─────────────────────────────────────────────────────────────────────────────
// Beispiel 2: Fehlerausbreitung mit coroutineScope
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Fehlerausbreitung in der Coroutine-Hierarchie: (Skript 116)
 * Ein Kind wirft eine unbehandelte Exception
 *   → Eltern-Job wird gecancelt
 *   → alle Geschwister-Coroutines werden abgebrochen
 *   → Exception propagiert nach oben zum Aufrufer
 *
 *   coroutineScope {
 *       launch { leitetPruefungEinSusMitFehler(...) }  ← wirft Exception
 *       launch { erstelltArbeitsvertragSus(...) }       ← wird abgebrochen!
 *   }                                                   ← Exception landet hier
 *
 *   Es können Scopes auch verdrahtet werden (Skript 143) coroutineScope != CoroutineScop
 * Cancelation Skript 163, kann nur stattfinden, wenn grade suspended wird, das kann man aber unterstützen Skript 184
 */
suspend fun ErrorSachbearbeiter.fehlerAusbreitungBeispiel() {
    val arbeitsanfang = beginntArbeit(this)

    schreibtStelleAusSus(this)
    wartetAufBewerbungsEndeSus(this)
    wertetBewerbungenAusSus(this)
    vereinbartBewerbungsgespraecheSus(this)
    fuehrtBewerbungsgespraecheSus(this)
    waehltBesteKandidatenAusSus(this)

    try {
        coroutineScope {
            launch { leitetPruefungEinSusMitFehler(this@fehlerAusbreitungBeispiel) }  // wirft!
            launch { erstelltArbeitsvertragSus(this@fehlerAusbreitungBeispiel) }       // abgebrochen
        }
    } catch (e: Exception) {
        println("$name: Einstellung abgebrochen – ${e.message}")
    }

    bewerbungAbgeschlossen(this, arbeitsanfang)
}

// ─────────────────────────────────────────────────────────────────────────────
// Beispiel 3: supervisorScope als Fehlergrenze
// ─────────────────────────────────────────────────────────────────────────────

/**
 * supervisorScope: Kinder schlagen unabhängig voneinander fehl.
 * Eine fehlschlagende Kind-Coroutine beeinflusst NICHT den Eltern-Job
 * und NICHT die Geschwister-Coroutines.
 *
 * Wichtig: Der Fehler muss lokal im launch { } mit try-catch behandelt werden.
 * Supervisors gehören an langlebige Stellen der Architektur –
 * sie sind KEIN Ersatz für reguläres try-catch.
 *
 *   supervisorScope {
 *       launch {
 *           try { leitetPruefungEinSusMitFehler(...) }  ← lokal behandelt
 *           catch (e: Exception) { ... }
 *       }
 *       launch { erstelltArbeitsvertragSus(...) }        ← läuft weiter!
 *   }
 */
suspend fun ErrorSachbearbeiter.supervisorScopeBeispiel() {
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
                leitetPruefungEinSusMitFehler(this@supervisorScopeBeispiel)
            } catch (e: Exception) {
                println("$name: Pruefung fehlgeschlagen – ${e.message}")
            }
        }
        launch {
            erstelltArbeitsvertragSus(this@supervisorScopeBeispiel)  // laeuft weiter!
        }
    }

    bewerbungAbgeschlossen(this, arbeitsanfang)
}

// ─────────────────────────────────────────────────────────────────────────────
// Beispiel 4: CoroutineExceptionHandler – letzter Ausweg
// ─────────────────────────────────────────────────────────────────────────────

/**
 * CoroutineExceptionHandler: Globaler Handler für unbehandelte Exceptions aus launch.
 * Wird nur aufgerufen, wenn der direkte Elternteil ein Supervisor ist.
 *
 * Er kann loggen oder reporten – aber NICHT die Ausführung retten.
 * Dafür ist try-catch zuständig.
 *
 * Für async/await gilt: Die Exception wird im Deferred gespeichert
 * und erst beim await()-Aufruf geworfen → dort mit try-catch behandeln.
 *
 *   supervisorScope {
 *       launch(handler) {
 *           leitetPruefungEinSusMitFehler(...)  ← unbehandelt → handler wird aufgerufen
 *       }
 *   }
 */
suspend fun ErrorSachbearbeiter.exceptionHandlerBeispiel() {
    val arbeitsanfang = beginntArbeit(this)

    schreibtStelleAusSus(this)
    wartetAufBewerbungsEndeSus(this)
    wertetBewerbungenAusSus(this)
    vereinbartBewerbungsgespraecheSus(this)
    fuehrtBewerbungsgespraecheSus(this)
    waehltBesteKandidatenAusSus(this)

    val handler = CoroutineExceptionHandler { _, exception ->
        println("$name: [CoroutineExceptionHandler] ${exception.message}")
    }

    supervisorScope {
        launch(handler) {
            leitetPruefungEinSusMitFehler(this@exceptionHandlerBeispiel)  // unbehandelt!
        }
        launch {
            erstelltArbeitsvertragSus(this@exceptionHandlerBeispiel)  // laeuft weiter
        }
    }

    bewerbungAbgeschlossen(this, arbeitsanfang)
}
