package org.example.workshop.examples

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

interface Sachbearbeiter {
    val name: String
    val alter: Int
}

fun beginntArbeit(sachbearbeiter: Sachbearbeiter): Long {
    println("${sachbearbeiter.name} startet mit der Arbeit")
    return System.currentTimeMillis()
}

fun bewerbungAbgeschlossen(sachbearbeiter: Sachbearbeiter, arbeitsanfang: Long) {
    println("${sachbearbeiter.name} hat ${System.currentTimeMillis() - arbeitsanfang} Tage benoetigt um den Mitarbeiter einzustellen.")

}

fun schreibtStelleAus(sachbearbeiter: Sachbearbeiter) {
    println("${sachbearbeiter.name} schreibt die Stelle aus.")
    Thread.sleep(2)
}

fun wartetAufBewerbungsEnde(sachbearbeiter: Sachbearbeiter) {
    println("${sachbearbeiter.name} wartet auf Bewerbungsende.")
    Thread.sleep(30)
}

fun wertetBewerbungenAus(sachbearbeiter: Sachbearbeiter) {
    println("${sachbearbeiter.name} wertet die Bewerbungen aus.")
    Thread.sleep(15)
}

fun vereinbartBewerbungsgespraeche(sachbearbeiter: Sachbearbeiter) {
    println("${sachbearbeiter.name} vereinbart Bewerbungsgespraeche.")
    Thread.sleep(8)
}

fun fuehrtBewerbungsgespraeche(sachbearbeiter: Sachbearbeiter) {
    println("${sachbearbeiter.name} fuehrt Bewerbungsgespraeche.")
    Thread.sleep(40)
}

fun waehltBesteKandidatenAus(sachbearbeiter: Sachbearbeiter) {
    println("${sachbearbeiter.name} waehlt Beste Kandidaten aus.")
    Thread.sleep(1)
}

fun leitetPruefungEin(sachbearbeiter: Sachbearbeiter) {
    println("${sachbearbeiter.name} leitet Pruefung ein.")
    Thread.sleep(180)
}

fun erstelltArbeitsvertrag(sachbearbeiter: Sachbearbeiter) {
    println("${sachbearbeiter.name} erstellt Arbeitsvertrag.")
    Thread.sleep(10)
}

suspend fun schreibtStelleAusSus(sachbearbeiter: Sachbearbeiter) {
    println("${sachbearbeiter.name} schreibt die Stelle aus.")
    delay(2.milliseconds)
}

suspend fun wartetAufBewerbungsEndeSus(sachbearbeiter: Sachbearbeiter) {
    println("${sachbearbeiter.name} wartet auf Bewerbungsende.")
    delay(30.milliseconds)
}

suspend fun wertetBewerbungenAusSus(sachbearbeiter: Sachbearbeiter) {
    println("${sachbearbeiter.name} wertet die Bewerbungen aus.")
    delay(15.milliseconds)
}

suspend fun vereinbartBewerbungsgespraecheSus(sachbearbeiter: Sachbearbeiter) {
    println("${sachbearbeiter.name} vereinbart Bewerbungsgespraeche.")
    delay(8.milliseconds)
}

suspend fun fuehrtBewerbungsgespraecheSus(sachbearbeiter: Sachbearbeiter) {
    println("${sachbearbeiter.name} fuehrt Bewerbungsgespraeche.")
    delay(40.milliseconds)
}

suspend fun leitetPruefungEinSus(sachbearbeiter: Sachbearbeiter) {
    println("${sachbearbeiter.name} leitet Pruefung ein.")
    delay(180.milliseconds)
}

suspend fun erstelltArbeitsvertragSus(sachbearbeiter: Sachbearbeiter) {
    println("${sachbearbeiter.name} erstellt Arbeitsvertrag.")
    delay(10.milliseconds)
}