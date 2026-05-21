# DocShield — Pitanja i odgovori

Ovaj file bilježi pitanja koja se pojavljuju tijekom razvoja.
Na kraju svake sesije generira se skripta za intervju prep.

---

## Tjedan 10 — SpeedDial FAB + Scaffold

*(pitanja dolaze ovdje)*

---

## Za buduće poglavlje — Higher-Order Functions i Event Callback Pattern

**Što je to:**
Higher-order function je funkcija koja prima drugu funkciju kao parametar (ili je vraća).
U Kotlinu, tip `() -> Unit` znači "funkcija bez argumenata koja ne vraća ništa" (ekvivalent Java void).

**Kotlin naziv:** Higher-Order Functions / Function Types
**Compose/Android naziv:** Event Callback Pattern, Lambda Hoisting
**OOP naziv:** Observer Pattern (pojednostavljen)

**Zašto je važno u Composeu:**
- Ekrani ne smiju znati za NavigationController (Clean Architecture)
- ViewModel ne smije znati za navigaciju
- Navigacijska logika živi u MainActivity — proslijeđuje se prema dolje kao lambda

**Primjer iz projekta:**
```kotlin
// ScanScreen prima callback — ne zna što će se dogoditi
fun ScanScreen(onBack: () -> Unit) {
    onBack() // samo poziva kad treba
}

// MainActivity definira što se događa
ScanScreen(onBack = { navController.popBackStack() })
```

**Java analogija:** Interface s jednom metodom (SAM — Single Abstract Method), ali bez boilerplatea.

**Teme za dublje istraživanje:**
- Kotlin function types: `() -> Unit`, `(String) -> Boolean`, `(Int, Int) -> Int`
- `typealias` za složenije tipove funkcija
- Razlika između lambda i anonymous function u Kotlinu
- Zašto Compose koristi ovaj pattern umjesto direktnog NavController pristupa

---

## Intervju skripta

*(generira se na kraju sesije)*
