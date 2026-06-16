package org.example.workshop.examples.threadSachbearbeiter

import org.example.workshop.examples.*

data class ThreadSachbearbeiter(
    override val name: String,
    override val alter: Int
): Sachbearbeiter

/**
 * Freier Sachbearbeiter
 * Kann Arbeitsschritte "parallel" abarbeiten.
 * Wird die derzeitige Aufgabe (Thread) grade nicht aktiv ausgeführt, kann eine andere Aufgabe bearbeitet werden.
 * Die derzeitige Aufgabe wird blockiert.
 **/
fun ThreadSachbearbeiter.arbeitet() {

    //deklariere Aufgaben, ohne sie zu starten
    val wartetAufBewerbungsEnde = Thread {
        wartetAufBewerbungsEnde(this)
    }

    val wertetBewerbungenAus = Thread {
        wertetBewerbungenAus(this)
    }

    val vereinbartBewerbungsgespraeche = Thread {
        vereinbartBewerbungsgespraeche(this)
    }

    val fuehrtBewerbungsgespraeche = Thread {
        fuehrtBewerbungsgespraeche(this)
    }

    val leitetPruefungEin = Thread {
        leitetPruefungEin(this)
    }

    val erstelltArbeitsvertrag = Thread {
        erstelltArbeitsvertrag(this)
    }



    // hier geht es los

    val arbeitsanfang = beginntArbeit(this)

    schreibtStelleAus(this)

    wartetAufBewerbungsEnde.start()
    wertetBewerbungenAus.start()
    vereinbartBewerbungsgespraeche.start()
    fuehrtBewerbungsgespraeche.start()

    wartetAufBewerbungsEnde.join()
    wertetBewerbungenAus.join()
    vereinbartBewerbungsgespraeche.join()
    fuehrtBewerbungsgespraeche.join()

    waehltBesteKandidatenAus(this)

    leitetPruefungEin.start()
    erstelltArbeitsvertrag.start()

    leitetPruefungEin.join()
    erstelltArbeitsvertrag.join()

    bewerbungAbgeschlossen(this, arbeitsanfang)

}