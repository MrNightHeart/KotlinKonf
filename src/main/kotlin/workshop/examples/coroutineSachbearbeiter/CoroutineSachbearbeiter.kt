package org.example.workshop.examples.coroutineSachbearbeiter

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.workshop.examples.*

data class CoroutineSachbearbeiter(
    override val name: String,
    override val alter: Int
) : Sachbearbeiter

/**
 * Coroutine Sachbearbeiter
 * Kann Arbeitsschritte "parallel" abarbeiten mit Coroutines.
 * Statt Threads werden leichtgewichtige Coroutines verwendet. (Skriptseite 22,23)
 * Der Thread wird bei delay() nicht blockiert, sondern freigegeben für andere Aufgaben. (Skriptseite 27)
 * Ausnahme runBlocking()-Funktion
 * coroutineScope { } ist suspendfunction, gruppiert coroutines und wartet automatisch auf alle gestarteten launch { } Blöcke. (bessere Alternative zu runBlocking)
 * launch {} startet coroutine -> fire and forget
 * async {} startet coroutine -> mit zu erwartendem Wert (Skript 64)
 *  withContext(){} wie coroutineScope nur mit ContextGlobalScope
 * GlobalScope böse (auch möglich wird aber von abgeraten) (Skript 147)
 **/
suspend fun CoroutineSachbearbeiter.arbeitet() {
    val arbeitsanfang = beginntArbeit(this)

    schreibtStelleAusSus(this)

    coroutineScope {
        launch { wartetAufBewerbungsEndeSus(this@arbeitet) }
        launch { wertetBewerbungenAusSus(this@arbeitet) }
        launch { vereinbartBewerbungsgespraecheSus(this@arbeitet) }
        launch { fuehrtBewerbungsgespraecheSus(this@arbeitet) }
    }

    waehltBesteKandidatenAusSus(this)

    coroutineScope {
        launch { leitetPruefungEinSus(this@arbeitet) }
        launch { erstelltArbeitsvertragSus(this@arbeitet) }
    }

    bewerbungAbgeschlossen(this, arbeitsanfang)

//_______________________________________________________________________________________________________

//    coroutineScope {
//        async {  } wäre ähnlich zu Promise
//    }

    //    withContext(Dispatchers.IO) {
//        ... coroutineScope aber legt fest, was für Threads genutz werden sollen (Skript 87)
//    }
}
