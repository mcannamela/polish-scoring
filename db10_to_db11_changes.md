# Changes: #

## throw ##
| id | long | ==> | | |
|:---|:-----|:----|:|:|
| throwNumber | int,nonull,uniquecombo | ==> | throwIdx |  |
| gameId | long,nonull,uniquecombo | ==> |  |  |
| playerId  | long,nonull | ==> | offensivePlayerId |  |
|  |  | ==> |  defensivePlayerId | long,nonull |
| timestamp | date,nonull | ==> |  |  |
| throwType | int,nonull | ==> |  |  |
| throwResult | int,nonull | ==> |  |  |
| isShort = false | boolean | ==> | deadType = 0 | int |
|  |  | ==> | isTipped = false | boolean |
| isGoaltend = false | boolean | ==> |  |  |
|  |  | ==> | isGrabbed = false | boolean |
| isDrinkHit = false | boolean | ==> |  |  |
| isOnFire = false | boolean | ==> |  |  |
|  |  | ==> | isLineFault = false | boolean |
|  |  | ==> | isOffDrinkDrop = false | boolean |
|  |  | ==> | isOffPoleKnock = false | boolean |
|  |  | ==> | isOffBottleKnock = false | boolean |
|  |  | ==> | isOffBreakErr = false | boolean |
| isDrinkDropped = false | boolean | ==> | isDefDrinkDrop = false |  |
|  |  | ==> | isDefPoleKnock = false | boolean |
|  |  | ==> | isDefBottleKnock = false | boolean |
|  |  | ==> | isDefBreakErr = false | boolean |
|  |  | ==> | offenseFireCount = 0 | int |
|  |  | ==> | defenseFireCount = 0 | int |
| initialOffensivePlayerScore = 0 | int | ==> | _deprecated_ | **note** |
| initialDefensivePlayerScore = 0 | int | ==> | _deprecated_ | **note** |

| ~~isOwnGoal = false~~ | boolean | ==> | _deleted_ | |
|:----------------------|:--------|:----|:----------|:|
| ~~ownGoalScore = 0~~ | int | ==> | _deleted_ |  |
| ~~isError = false~~ | boolean | ==> | _deleted_ |  |
| ~~errorScore = 0~~ | int | ==> | _deleted_ |  |
| ~~goaltendScore = 0~~ | int | ==> | _deleted_ |  |
|  ~~isTrap = false~~  | boolean | ==> | _deleted_ |  |
| ~~isFiredOn = false~~ | boolean | ==> | _deleted_ |  |
| ~~isBroken = false~~ | boolean | ==> | _deleted_ |  |

**throwTypes:** bottle, cup, pole, strike, high, right, low, left, +**short**, +**trap**, +**redeemedTrap**, +**not\_thrown**, +**fired\_on**

**throwResults:** catch, drop, stalwart, +**broken**, +**NA**

+**deadType: +notdead, +high, +right, +low, +left**

**note:** (are these just for speed?)<-probably we can take these out, but it means that the throw's state will be incomplete when you retrieve it from the db - mc
ok lets consider these deprecated but leave in for now. when we rewrite gip, we can see if it causes any issues to not use these. -p
might be a real pain not to have these actually. a battle for another day. -p

## ==> gameStats ##
| ==> | gameDuration | long |
|:----|:-------------|:-----|
| ==> | numThrows | long |
| ==> | strikeRate | long |
| ==> | highRate | long |
| ==> | ... | ... |

## session ##
| id | long | ==> | | |
|:---|:-----|:----|:|:|
| sessionName | string,nonull | ==> |  |  |
| sessionType | int,nonull | ==> |  |  |
| ~~startDate~~ | date,nonull | ==> | _deleted_ |  |
| ~~endDate~~ | date | ==> | _deleted_ |  |
| isTeam = false | boolean | ==> |  |  |
| isActive = true | boolean | ==> |  |  |

# Unchanged: #
## game ##
| id | long | ==> | | |
|:---|:-----|:----|:|:|
| firstPlayerId | long,nonull,uniquecombo | ==> |  |  |
| secondPlayerId | long,nonull,uniquecombo | ==> |  |  |
| firstPlayerOnTop | boolean,nonull | ==> |  |  |
| sessionId | long | ==> |  |  |
| venueId | long | ==> |  |  |
| datePlayed | date,nonull | ==> |  |  |
| firstPlayerScore | int | ==> |  |  |
| secondPlayerScore | int | ==> |  |  |
| isTeam | boolean | ==> |  |  |
| isComplete = false | boolean | ==> |  |  |
| isTracked = true | boolean | ==> |  |  |

## player ##
| id | long | ==> | | |
|:---|:-----|:----|:|:|
| firstName | string,nonull,uniquecombo | ==> |  |  |
| lastName | string,nonull,uniquecombo | ==> |  |  |
| nickName | string,nonull,uniquecombo | ==> |  |  |
| throwsRightHanded | boolean | ==> |  |  |
| throwsLeftHanded | boolean | ==> |  |  |
| prefersLeftSide | boolean | ==> |  |  |
| prefersRightSide | boolean | ==> |  |  |
| height\_cm | int | ==> |  |  |
| weight\_kg | int | ==> |  |  |
| imageBytes | byte[.md](.md) | ==> |  |  |
| isActive = true | boolean | ==> |  |  |

## playerStats ##
| playerId | long,unique | ==> | | |
|:---------|:------------|:----|:|:|
| nWins | int | ==> |  |  |
| nLosses | int | ==> |  |  |
| strikeRate | long | ==> |  |  |
| highRate | long | ==> |  |  |
| ... | ... | ==> |  |  |

## team ##
| id | long | ==> | | |
|:---|:-----|:----|:|:|
| teamName | string,nonull | ==> |  |  |
| firstPlayerId | long,nonull,uniquecombo | ==> |  |  |
| secondPlayerId | long,nonull,uniquecombo | ==> |  |  |
| imageBytes | byte[.md](.md) | ==> |  |  |
| isActive = true | boolean | ==> |  |  |

## teamStats ##
| teamId | long,unique | ==> | | |
|:-------|:------------|:----|:|:|
| nWins | int | ==> |  |  |
| nLosses | int | ==> |  |  |
| strikeRate | long | ==> |  |  |
| highRate | long | ==> |  |  |
| ... | ... | ==> |  |  |

## badge ##
| id | long | ==> | | |
|:---|:-----|:----|:|:|
| playerId | long | ==> |  |  |
| isTeam | boolean | ==> |  |  |
| sessionId | long | ==> |  |  |
| badgeType | int,nonull | ==> |  |  |

## sessionMembers ##
| sessionId | long,nonull,uniquecombo | ==> | | |
|:----------|:------------------------|:----|:|:|
| playerId | long,nonull,uniquecombo | ==> |  |  |
| playerSeed | int,nonull | ==> |  |  |
| playerRank | int,nonull | ==> |  |  |

## venue ##
| id | long | ==> | | |
|:---|:-----|:----|:|:|
| venueName | string,nonull,uniquecombo | ==> |  |  |
| scoreKeptFromTop = true | boolean | ==> |  |  |
| longitude | long | ==> |  |  |
| latitude | long | ==> |  |  |
| zipCode | long | ==> |  |  |
| isActive = true | boolean | ==> |  |  |