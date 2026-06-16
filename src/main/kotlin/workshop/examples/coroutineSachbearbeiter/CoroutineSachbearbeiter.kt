package org.example.workshop.examples.coroutineSachbearbeiter

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.example.workshop.examples.*

data class CoroutineSachbearbeiter(
    override val name: String,
    override val alter: Int
) : Sachbearbeiter

/**
 * Coroutine Sachbearbeiter
 * Kann Arbeitsschritte "parallel" abarbeiten mit Coroutines.
 * Statt Threads werden leichtgewichtige Coroutines verwendet.
 * Der Thread wird bei delay() nicht blockiert, sondern freigegeben für andere Aufgaben.
 * coroutineScope { } wartet automatisch auf alle gestarteten launch { } Blöcke.
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
}
