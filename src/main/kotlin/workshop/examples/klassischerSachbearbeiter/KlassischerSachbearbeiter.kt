package org.example.workshop.examples.klassischerSachbearbeiter

import org.example.workshop.examples.*

data class KlassischerSachbearbeiter(
    override val name: String,
    override val alter: Int
): Sachbearbeiter

/**
 * Klassischer Sachbearbeiter
 * Muss Arbeitsschritte sequenziell abarbeiten-> eine Aufgabe nach der anderen.
 * Es kann erst der nächste Schritt erfolgen, wenn der vorherige Schritt abgeschlossen ist.
 **/
fun KlassischerSachbearbeiter.arbeitet() {
    val arbeitsanfang = beginntArbeit(this)

    schreibtStelleAus(this)
    wartetAufBewerbungsEnde(this)
    wertetBewerbungenAus(this)
    vereinbartBewerbungsgespraeche(this)
    fuehrtBewerbungsgespraeche(this)
    waehltBesteKandidatenAus(this)
    leitetPruefungEin(this)
    erstelltArbeitsvertrag(this)

    bewerbungAbgeschlossen(this, arbeitsanfang)
}

