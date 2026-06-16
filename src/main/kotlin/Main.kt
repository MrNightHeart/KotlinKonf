package org.example

import org.example.workshop.examples.freierSachbearbeiter.FreierSachbearbeiter
import org.example.workshop.examples.freierSachbearbeiter.arbeitet
import org.example.workshop.examples.klassischerSachbearbeiter.KlassischerSachbearbeiter
import org.example.workshop.examples.klassischerSachbearbeiter.arbeitet


suspend fun main() {
    // Sequentiell arbeiten
    KlassischerSachbearbeiter(name = "Helmut", alter = 30).arbeitet()

    // Mit Threads arbeiten
    FreierSachbearbeiter(name = "Peter", alter = 60).arbeitet()

    // Mit Coroutines arbeiten
}