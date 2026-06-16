package org.example.workshop.examples.klassischerSachbearbeiter

import kotlinx.coroutines.delay
import org.example.workshop.examples.Sachbearbeiter
import org.example.workshop.examples.beginntArbeit
import org.example.workshop.examples.bewerbungAbgeschlossen
import org.example.workshop.examples.erstelltArbeitsvertrag
import org.example.workshop.examples.fuehrtBewerbungsgespraeche
import org.example.workshop.examples.leitetPruefungEin
import org.example.workshop.examples.schreibtStelleAus
import org.example.workshop.examples.vereinbartBewerbungsgespraeche
import org.example.workshop.examples.waehltBesteKandidatenAus
import org.example.workshop.examples.wartetAufBewerbungsEnde
import org.example.workshop.examples.wertetBewerbungenAus

data class KlassischerSachbearbeiter(
    override val name: String,
    override val alter: Int
): Sachbearbeiter

/**
 * Klassischer Sachbearbeiter
 * Muss Arbeitsschritte sequentiell abarbeiten-> eine Aufgabe nach der anderen.
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

