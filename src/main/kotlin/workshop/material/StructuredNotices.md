Threads
● Inside one thread instructions are executed sequentially
● Multiple threads can be executed concurrently
● Data confined to a single thread can be accessed safely without the
need for synchronization primitives like locks / mutexes
● Sharing state across threads requires care

Coroutine
● is a sequence of instructions
● Inside one coroutine instructions are executed sequentially
● Multiple coroutines can be executed concurrently
● Data confined to a single coroutine can be accessed safely without the
need for synchronization primitives like locks / mutexes
● Sharing state across coroutines requires care
● Coroutines run on top of threads
● Coroutines run on one thread at a time
● A toolkit for managing shared state and concurrent computations
● Structured concurrency
● Reactive-style abstractions with Flows


Wording
● Suspending function
A function that can pause
● Coroutine
An instance of a suspendable computation (with its lifecycle)
● Coroutine Scope
A mechanism for grouping multiple coroutines
● Coroutine Context
A bag of special values associated with a coroutine
● Structured Concurrency
Managing hierarchies of coroutines and handling errors predictably

Instead of blocking a thread, we suspend the coroutine 
    => thread is not blocked it continues with another task

______________________________________________________________________________________________________


Hier ist eine ausführlichere und thematisch gegliederte Zusammenfassung:

---

# KotlinConf 2026: Asynchronous Programming with Kotlin Coroutines
**Workshop-Skript | 365 Folien**
**Autoren:** Sebastian Aigner, Natasha Murashkina, Alejandro Serrano Mena (JetBrains)

Der Workshop ist als ganztägiges, hands-on Format konzipiert. Drei kleine Beispielprojekte dienen als roter Faden: *Articles* (Blog-API), *Kettle* (Wasserkocher mit WebSocket) und *Chat* (Echtzeit-Messaging). Das Skript kombiniert Theorie-Folien, Live-Demos und ca. 10 nummerierte Coding-Tasks.

---

## Themenkomplex 1: Was sind Coroutines und warum brauchen wir sie?

### 1.1 Das Grundproblem: Blocking vs. Suspending

Der Workshop beginnt mit der Frage, warum herkömmliche blocking API-Aufrufe problematisch sind. Ein blockierender HTTP-Request blockiert den gesamten Thread – vor allem fatal im UI-Thread, der ständig rendern muss. Coroutines lösen das, indem sie statt des Threads die *Coroutine* pausieren. Der Thread bleibt frei und kann andere Aufgaben übernehmen.

```kotlin
// Blocking:
val articles = myHTTPService.loadArticles() // Thread eingefroren!

// Mit Coroutines:
val articles = newHTTPService.loadArticles() // Thread frei, Coroutine suspendiert
```

### 1.2 Coroutines vs. Threads – Gemeinsamkeiten und Unterschiede

Coroutines sind ähnlich wie Threads: Beide führen Anweisungen sequenziell aus, können nebenläufig betrieben werden, und Shared State erfordert Synchronisation. Der entscheidende Unterschied: Statt Tausender Threads können Millionen Coroutines auf denselben Thread-Pool aufgeteilt werden – sie sind ein leichtgewichtiges Abstraktionslayer *über* Threads.

### 1.3 Coroutines vs. Alternativen

Der Workshop vergleicht Coroutines mit `CompletableFuture` (Java) und `RxJava`. Beide Alternativen erzwingen einen Callback- oder Chain-Stil, der für komplexere Logik schnell unübersichtlich wird. Coroutines ermöglichen Code, der *sequenziell aussieht* – ohne den Nachteil des Blockierens.

### 1.4 Kernvokabular

| Begriff | Bedeutung |
|---|---|
| `suspend fun` | Funktion, die pausieren kann |
| Coroutine | Eine Instanz einer suspendierbaren Berechnung |
| CoroutineScope | Mechanismus zum Gruppieren von Coroutines |
| CoroutineContext | Beutel mit Sonderwerten, die an eine Coroutine gebunden sind |
| Structured Concurrency | Verwaltung von Coroutine-Hierarchien inkl. Fehlerbehandlung |

Außerdem wird der Unterschied zwischen *Concurrency* (mehrere Aufgaben gleichzeitig verwalten) und *Parallelism* (tatsächlich gleichzeitig auf mehreren CPU-Kernen ausführen) klargestellt.

---

## Themenkomplex 2: Suspend Functions und Coroutine Builder

### 2.1 Die `suspend`-Funktion

`suspend` ist das Kern-Primitive von Kotlin Coroutines – direkt in den Compiler eingebaut. Eine suspend function kann ihre Ausführung anhalten, ohne den zugrundeliegenden Thread zu blockieren. Man kann sie aufrufen aus: einer anderen suspend function, einem `suspend fun main()`, oder einem Coroutine Builder.

### 2.2 Die vier wichtigsten Coroutine Builder

**`coroutineScope { }`** ist eine suspendierende Funktion, die einen `CoroutineScope` als Lambda-Receiver bereitstellt. Sie wartet auf alle gestarteten Kind-Coroutines und kann einen Wert zurückgeben. Dient der *concurrent decomposition of work* – „berechne mehrere Dinge parallel, warte auf alle, gib ein Ergebnis zurück."

**`launch { }`** startet eine neue Coroutine im „fire-and-forget"-Stil. Gibt ein `Job`-Objekt zurück, das für Cancellation genutzt werden kann. Eignet sich für Seiteneffekte, bei denen kein Rückgabewert erwartet wird.

**`async { }` / `await()`** startet eine Coroutine, die ein Ergebnis berechnet. Gibt ein `Deferred<T>` zurück (ein spezieller `Job`). Mit `.await()` wartet man auf das Ergebnis, ohne den Thread zu blockieren. Wichtiger Hinweis: `async` ohne zeitversetztes `await` ist sinnlos – dann lieber direkt `suspend` verwenden.

**`runBlocking { }`** ist eine Brückenfunktion, die den aktuellen Thread blockiert, bis alle Coroutines fertig sind. Baut intern eine Event Loop. Nur für Bridging-Szenarien gedacht (z. B. `main`-Funktion, Tests) – nicht als allgemeines Werkzeug und niemals innerhalb einer suspend function.

```kotlin
suspend fun loadArticlesConcurrently(service: BlogService): List<Article> =
    coroutineScope {
        val articles = service.getArticleList()
        articles.map { async { service.loadComments(it) } }.awaitAll()
    }
```

Der Workshop zeigt auch den Unterschied zwischen korrekten und fehlerhaften `async`-Verwendungen (z. B. sofortiges `await` direkt nach `async` entspricht sequenziellem Code).

---

## Themenkomplex 3: Dispatchers und Thread-Management

### 3.1 Was sind Dispatchers?

Ein Dispatcher legt fest, auf welchem Thread oder Thread-Pool eine Coroutine ausgeführt wird. Die Wahl hat Einfluss auf Performance, Parallelismus und Framework-spezifische Anforderungen.

### 3.2 Die verfügbaren Dispatchers

| Dispatcher | Zweck |
|---|---|
| `Dispatchers.Default` | CPU-intensive Arbeit, Thread-Pool mit #CPU-Kernen |
| `Dispatchers.IO` | Blocking I/O, bis zu 64 Threads |
| `Dispatchers.Main` | UI-Thread (framework-abhängig, z. B. Android/Compose) |
| `limitedParallelism(n)` | Begrenzt genutzte Threads aus einem Pool |

### 3.3 Dispatcher wechseln mit `withContext`

`withContext` ist ein spezialisierter, lexikalisch beschränkter Coroutine Builder, der den Kontext (typischerweise den Dispatcher) wechselt. Eine suspend function sollte für den Aufrufer „main-safe" sein – also nicht blockieren, egal von wo sie aufgerufen wird. Blockierende Aufrufe (z. B. `File.readBytes()`) sollten daher mit `withContext(Dispatchers.IO)` eingekapselt werden.

### 3.4 Shared Mutable State

Da Coroutines denselben Thread-Pool teilen, können Race Conditions entstehen. Das Skript demonstriert ein kaputtes Zähler-Beispiel und zeigt drei Lösungsansätze:
- **Atomics** (`AtomicInt`) für einfache Zähler
- **`Mutex`** zum Schützen kritischer Abschnitte (`mutex.withLock { }`)
- **`Semaphore`** zum Begrenzen gleichzeitiger Ausführungen

Wichtige Klarstellung: `limitedParallelism(n)` ist *kein* Ersatz für einen `Semaphore`. Er begrenzt nur die Anzahl genutzter Carrier-Threads – alle Coroutines starten trotzdem sofort. Ein `Semaphore` hingegen lässt Coroutines tatsächlich warten.

---

## Themenkomplex 4: Structured Concurrency

### 4.1 Das Konzept

Structured Concurrency führt Eltern-Kind-Beziehungen zwischen Coroutines ein. Dadurch werden drei wichtige Ziele erreicht: unnötige Arbeit vermeiden, Ressourcenlecks verhindern und Anwendungen reaktionsfähig halten.

### 4.2 Die Anatomie einer Coroutine

Jede Coroutine besitzt einen `CoroutineScope`, der einen `CoroutineContext` enthält, der wiederum ein `Job`-Objekt hält. Das `Job`-Objekt ist der Träger der Eltern-Kind-Beziehung (`Job.parent`, `Job.children`). `launch` gibt einen `Job` zurück, `async` gibt ein `Deferred<T>` zurück (Subtyp von `Job`).

### 4.3 Woher bekomme ich einen CoroutineScope?

- Vom Framework (Android: `viewModelScope`, Ktor: `application.launch`, `call.launch`)
- Von Coroutine Buildern (`coroutineScope {}`, `withContext {}`)
- Über `runBlocking` (nur für Bridging)
- Als Parameter akzeptieren (für Setup-Code)
- Via `CoroutineScope()` als Property einer Klasse (für Komponenten mit eigenem Lebenszyklus)

### 4.4 `coroutineScope` vs. `CoroutineScope()`

Diese Verwechslungsgefahr wird explizit adressiert:

| | `coroutineScope {}` | `CoroutineScope()` |
|---|---|---|
| Typ | Suspend function | Konstruktor / Fabrikfunktion |
| Zweck | Concurrent decomposition of work (lexikalisch begrenzt) | Coroutines an einen externen Lifecycle binden |
| Scope-Ende | Wenn alle Kinder fertig sind | Durch explizites `.cancel()` |

### 4.5 GlobalScope – ein Anti-Pattern

`GlobalScope` startet Coroutines ohne Eltern in der Anwendungshierarchie. Das Skript erklärt ausführlich, warum das problematisch ist: Cancellation funktioniert nicht, Ressourcenlecks entstehen, Lifecycle-Awareness fehlt. `GlobalScope` existiert nur aus historischen Gründen (hinzugekommen vor Structured Concurrency) und für den seltenen Fall globaler App-Lifetime-Coroutines. Fazit: „Resist the temptation, find a better scope."

Ephemeral parentless scopes via `CoroutineScope(EmptyCoroutineContext)` sind laut Skript „GlobalScope in a trenchcoat" – genauso problematisch.

### 4.6 CoroutineContext im Detail

Der `CoroutineContext` ist eine unveränderliche Menge von Elementen: `CoroutineDispatcher`, `Job`, `CoroutineName`, `CoroutineExceptionHandler` und ggf. custom Elemente aus Libraries. Kinder erben den Kontext des Elternteils; beim Start einer neuen Coroutine wird ein neues `Job`-Objekt erstellt, das Kind des Eltern-Jobs wird. Builder-Parameter können Kontextelemente überschreiben.

---

## Themenkomplex 5: Cancellation

### 5.1 Warum Cancellation wichtig ist

Cancellation verhindert unnötige Arbeit, gibt Ressourcen (Mutex, DB-Verbindungen) frei und ist Teil des Fehlerbehandlungs-Mechanismus. Die Hierarchie von Structured Concurrency stellt sicher, dass beim Abbruch eines Eltern-Jobs alle Kinder-Jobs automatisch abgebrochen werden.

### 5.2 Wie wird Cancellation ausgelöst?

```kotlin
val job = launch { doWork() }
job.cancel()              // oder:
job.cancelAndJoin()       // cancel + warten bis fertig

scope.cancel()            // bricht alle im Scope aus
withTimeoutOrNull(500) { … }  // automatischer Abbruch nach Timeout
```

### 5.3 Cancellation ist kooperativ

Eine Coroutine prüft nur an *Suspension Points*, ob sie abgebrochen wurde. Bibliotheksfunktionen aus `kotlinx.coroutines` (z. B. `delay`, `yield`) sind automatisch cancellable. Eigene CPU-intensive Loops hingegen nicht – dort muss man selbst kooperieren:

- **`isActive`**: Prüft, ob Cancellation angefordert wurde – erlaubt eigene Cleanup-Logik vor dem Beenden
- **`ensureActive()`**: Wirft sofort eine `CancellationException`, wenn Cancellation angefordert wurde
- **`yield()`**: Gibt Rechenzeit an andere Coroutines ab und prüft auf Cancellation; sinnvoll bei CPU-lastiger Arbeit, die den Thread-Pool erschöpfen könnte

### 5.4 CancellationException nicht verschlucken

`CancellationException` wird vom Coroutines-Mechanismus intern genutzt, um Abbrüche zu propagieren. Wird sie in einem `catch`-Block verschluckt, läuft die Coroutine weiter – ein häufiger Bug. Drei sichere Patterns:

1. `CancellationException` nicht fangen (einfachste Lösung)
2. Im `catch`-Block auf Typ prüfen und re-throwen
3. `ensureActive()` im `catch`-Block aufrufen

Besondere Vorsicht bei `runCatching { }`: Es fängt *alle* `Throwable`s – das Skript rät explizit davon ab.

### 5.5 Cleanup nach Cancellation

Mit einem `finally`-Block können Ressourcen freigegeben werden. Falls im Cleanup selbst suspendiert werden muss, ist `withContext(NonCancellable) { }` nötig – da eine cancelled Coroutine keine weiteren Suspension Points mehr passieren kann.

---

## Themenkomplex 6: Fehlerbehandlung

### 6.1 Fehler innerhalb einer Coroutine

Innerhalb einer einzelnen Coroutine funktioniert `try-catch` erwartungsgemäß – das ist der bevorzugte Weg. Wichtig: `try-catch` muss den tatsächlich ausführenden Code umschließen, nicht den Builder-Aufruf (der kehrt sofort zurück).

### 6.2 Fehlerausbreitung in der Hierarchie

Wirft eine Kind-Coroutine eine unbehandelte Exception, wird der Eltern-Job gecancelt – was wiederum alle Geschwister-Coroutines abbricht. Das ist kein Bug, sondern gewolltes Verhalten: Wenn eine Coroutine in einem kollaborativen System scheitert, können alle anderen ihre Arbeit oft nicht sinnvoll fortsetzen.

### 6.3 SupervisorJob als Fehlergrenze

Manchmal sollen Fehler isoliert werden. `SupervisorJob` verhindert, dass eine fehlschlagende Kind-Coroutine den Eltern-Job und die Geschwister beeinflusst. Quellen für einen Supervisor:
- Vom Framework (Ktor nutzt intern Supervisors für Request-Handler)
- `supervisorScope { }` (analog zu `coroutineScope`, aber mit Supervisor-Semantik)
- `CoroutineScope(SupervisorJob())` für Komponenten mit eigenem Lebenszyklus

Supervisors gehören an langlebige Stellen der Architektur (App-Level, Screen-Level) – sie sind kein Ersatz für reguläre `try-catch`-Behandlung.

### 6.4 CoroutineExceptionHandler

Ein letzter Ausweg für unbehandelte Exceptions aus `launch`-Coroutines, wenn der direkte Elternteil ein Supervisor ist. Er ist ein `CoroutineContext`-Element und wird an die Scope-Konfiguration übergeben. Er kann loggen oder Fehler reporten, aber *nicht* die Ausführung retten (dafür `try-catch` nutzen). Standardverhalten: auf JVM schreibt er nach `stderr`, auf Android/iOS stürzt die App ab.

Für `async`: Keine `CoroutineExceptionHandler`-Invokation. Die Exception wird im `Deferred` gespeichert und beim `await()`-Aufruf geworfen – dort muss sie mit `try-catch` behandelt werden.

---

## Themenkomplex 7: Flows

### 7.1 Motivation

Eine `suspend fun` gibt einen einzigen Wert zurück. `Flow<T>` ermöglicht, *mehrere Werte über die Zeit* asynchron zu liefern – für Progressive Loading, Event Streams oder Subscription-APIs.

### 7.2 Cold Flows

Cold Flows sind inert: Es passiert nichts, bis jemand `collect` aufruft. Für jeden Collector wird die Flow-Logik neu ausgeführt.

**`flow { emit(...) }`** ist der einfachste Builder für sequenzielle Flows. Das Lambda darf suspendieren, aber keine eigenen Coroutines starten. Flows können unendlich sein.

**`channelFlow { send(...) }`** ermöglicht Nebenläufigkeit innerhalb des Flows, da ein `ProducerScope` (ein spezialisierter `CoroutineScope`) bereitgestellt wird. Intern wird ein Channel genutzt, der Elemente puffert. Der Default-Buffer hält 64 Elemente; weitere `send`-Aufrufe suspendieren bis Platz frei wird. Faustregel: `flow` für sequenzielle Logik, `channelFlow` nur wenn Concurrency notwendig ist.

**Allgemeine Empfehlung:** Flow-erzeugende Funktionen sollten *nicht* `suspend` sein, da das Erstellen eines Flows schnell sein sollte. Die tatsächliche (suspendierende) Arbeit findet im Builder-Lambda statt.

### 7.3 Hot Flows

Hot Flows sind immer aktiv – auch ohne Collector. Sie vervollständigen niemals.

**`SharedFlow`** broadcastet Werte an mehrere Subscriber. Neue Subscriber erhalten nur Werte ab dem Zeitpunkt ihrer Subscription (optional mit `replay`-Buffer, der vergangene Werte für neue Subscriber bereithält). Gut für Events wie Chat-Nachrichten.

**`StateFlow`** ist ein spezialisierter SharedFlow, der immer genau einen Wert hält (mit Initialwert). Er emittiert nur bei tatsächlicher Änderung (equality-based conflation). Ideal als State Container – z. B. für UI-State in ViewModels. Operationen wie `update { }` und `updateAndGet { }` erlauben atomare Zustandsänderungen.

**Von Cold zu Hot:** Die Operatoren `shareIn()` und `stateIn()` konvertieren Cold Flows in Hot Flows. Mit `SharingStarted.WhileSubscribed()` startet die Ausführung erst, wenn der erste Subscriber da ist, und stoppt, wenn der letzte geht – effizient für Ressourcenmanagement.

**SharedFlow vs. StateFlow im Vergleich:**

| | SharedFlow | StateFlow |
|---|---|---|
| Zweck | Event Broadcasting | State Container |
| Emittiert wann? | Bei jedem `emit` | Nur bei Wertänderung |
| Initialwert | Nein | Ja (Pflicht) |
| Typischer Einsatz | Chat-Nachrichten, Einmalereignisse | UI-State, aktuelle Temperatur |

### 7.4 Flow Operators

Operators werden unterschieden in *intermediate* (geben wieder einen `Flow` zurück, lazy) und *terminal* (triggern die Ausführung).

**Intermediate Operators:**

| Operator | Funktion |
|---|---|
| `map`, `filter`, `onEach` | Klassische Transformationen |
| `onStart`, `onCompletion`, `onEmpty` | Einhaken in Flow-Phasen |
| `buffer` | Puffert Elemente für langsame Collectors |
| `debounce` | Filtert Werte, denen schnell neuere folgen (z. B. Sucheingabe) |
| `conflate` | Verwirft Zwischenwerte, hält Emitter nicht auf |
| `transform` | Wie `map`, kann aber 0 bis n Werte emittieren |
| `flowOn` | Wechselt den Dispatcher für alle vorgelagerten Operators |
| `catch` | Fängt Fehler im Upstream ab (nicht im Downstream) |
| `retry` | Wiederholt die Collection bei Fehler |
| `take`, `takeWhile` | Konvertiert unendliche in endliche Flows |

`flowOn` ist *context-preserving*: Es beeinflusst nur den Upstream und leckt nicht in den Downstream. Auf `SharedFlow` und `StateFlow` hat `flowOn` keine Wirkung (wird zur Compile-Zeit als Fehler markiert).

**Terminal Operators:** `collect`, `first`, `firstOrNull`, `toList`, `toSet`, u. a. Sie triggern die Ausführung der gesamten Pipeline.

---

## Themenkomplex 8: Testing

### 8.1 Suspend Functions testen

Suspend functions werden grundsätzlich wie normaler Code getestet. Der einzige Unterschied: `runTest` statt `runBlocking`. `runTest` nutzt einen speziellen Test-Dispatcher mit *virtueller Zeit* – `delay`-Aufrufe verbrauchen keine echte Zeit, sondern springen vor.

### 8.2 Flows testen mit Turbine

Die Bibliothek **Turbine** vereinfacht das Testen von Flows erheblich. Sie bietet `awaitItem()`, `awaitComplete()`, `awaitError()` und prüft, ob unerwartete Emissionen auftreten. Das Skript zeigt Unit- und Integrationstests für den Kettle-ViewModel (mit echtem und gefaktem Service).

---

## Übergreifende Empfehlungen des Workshops

- Bevorzuge `suspend` + `try-catch` über `async`/`CoroutineExceptionHandler`
- Vermeide `GlobalScope` – finde immer einen passenden Scope
- Suspend functions sollten *main-safe* sein (keine Blocking-Calls, außer in `withContext(IO)`)
- `CancellationException` niemals verschlucken
- Bei `SharedFlow` vs. `StateFlow`: Im Zweifel `StateFlow` – die meisten Probleme lassen sich als State modellieren
- Verwende `flow { }` für sequenzielle und `channelFlow { }` für nebenläufige Flows
- `limitedParallelism` ist kein Ersatz für `Semaphore`; `Semaphore` ist kein Ersatz für `Mutex`
- Teste mit `runTest` und Turbine

