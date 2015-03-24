HRLL = high, right, low, or left
PCB = pole, cup, or bottle
trap(+) = trap or redeemed trap

# Overview #

| **TYPES** | | **RESULTS** | | **MODS** | **MODS+** |
|:----------|:|:------------|:|:---------|:----------|
| HRLL |  | NA |  | dead (HRLL) | onFire (off. only) |
| PCB |  | drop |  | drinkHit | line fault (og) |
| strike |  | catch |  | tipped | drinkDrop (og/err) |
| trap(+) |  | stalwart |  | goaltend | PCB knock (og/err) |
| short |  | broken |  | grabbed |  bottleBreak (og/err) |
| firedOn |  |  |  |  |  |


Comments:
  * MODS+ can stack on all other mods, throwTypes (except firedOn) and results without restriction. Only exception is onFire, which restricts result to NA or broken.
  * firedOn must be kept separate under current rules because a dummy throw has to be created for the defender to allow the on fire player to keep throwing)
  * tipped cannot stack with goaltend, drinkHit. only drop and catch allowed
  * drinkHit cannot stack with tipped or dead.

---


# Legal outcomes #

### HRLL ###
  * MODS: (dead or drinkhit), grabbed
  * RESULTS: drop or catch

### PCB ###
  * MODS: tipped, dead, goaltend
  * RESULTS: drop, catch, stalwart, broken

### Strike ###
  * MODS: (dead or drinkHit), grabbed
  * RESULTS: drop or catch

### Trap (+Redeemed), Short ###
  * MODS: dead, grabbed
  * RESULTS: NA

### firedOn ###
  * MODS: _none_
  * RESULTS: NA
  * this is a dummy throw for current ruleset