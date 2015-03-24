# Todo #
  * ~~figure out best way to track drawables in db fields~~
  * ~~push throw changes to db11~~
  * ~~migration code~~

# Changes: #

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
|  |  | ==> | isTeam | boolean |
|  |  | ==> | isComplete = false | boolean |
|  |  | ==> | isTracked = true | boolean |

## player ##
| id | long | ==> | | |
|:---|:-----|:----|:|:|
| firstName | string,nonull,uniquecombo | ==> |  |  |
| lastName | string,nonull,uniquecombo | ==> |  |  |
| nickName | string,nonull,uniquecombo | ==> |  |  |
| throwsRightHanded | boolean | ==> |  |  |
| throwsLeftHanded | boolean | ==> |  |  |
|  |  | ==> | prefersLeftSide | boolean |
|  |  | ==> | prefersRightSide | boolean |
| height\_cm | int | ==> |  |  |
| weight\_kg | int | ==> |  |  |
| ~~nGames~~ | int | ==> | _deleted_ |  |
| ~~nWins~~ | int | ==> | _deleted_ |  |
| ~~nLosses~~ | int | ==> | _deleted_ |  |
| imageBytes | byte[.md](.md) | ==> |  |  |
| isActive = true | boolean | ==> |  |  |

## ==> playerStats ##
| ==> | playerId | long,unique |
|:----|:---------|:------------|
| ==> | nWins | int |
| ==> | nLosses | int |
| ==> | strikeRate | long |
| ==> | highRate | long |
| ==> | ... | ... |

## ==> team ##
| ==> | id | long |
|:----|:---|:-----|
| ==> | teamName | string,nonull |
| ==> | firstPlayerId | long,nonull,uniquecombo |
| ==> | secondPlayerId | long,nonull,uniquecombo |
| ==> | imageBytes | byte[.md](.md) |
| ==> | isActive = true | boolean |

## ==> teamStats ##
| ==> | teamId | long,unique |
|:----|:-------|:------------|
| ==> | nWins | int |
| ==> | nLosses | int |
| ==> | strikeRate | long |
| ==> | highRate | long |
| ==> | ... | ... |

## ==> badge ##
| ==> | id | long |
|:----|:---|:-----|
| ==> | playerId | long |
| ==> | isTeam | boolean |
| ==> | sessionId | long |
| ==> | badgeType | int,nonull |

## session ##
| id | long | ==> | | |
|:---|:-----|:----|:|:|
| sessionName | string,nonull | ==> |  |  |
| isLeague |  | ==> | sessionType | int,nonull,**note1** |
| startDate | date,nonull | ==> |  |  |
| endDate | date | ==> |  |  |
|  |  | ==> | isTeam = false | boolean |
|  |  | ==> | isActive = true | boolean |

**note1:** 0=open, 1=League, 2=Ladder, ..., 10=SingleElimTourney, 11=DoubleElimTourney, ...

## ==> sessionMembers ##
| ==> | sessionId | long,nonull,uniquecombo |
|:----|:----------|:------------------------|
| ==> | playerId | long,nonull,uniquecombo |
| ==> | playerSeed | int,nonull |
| ==> | playerRank | int,nonull |

## venue ##
| id | long | ==> | | |
|:---|:-----|:----|:|:|
| name | string,nonull,uniquecombo | ==> | venueName |  |
| scoreKeptFromTop = true | boolean | ==> |  |  |
|  |  | ==> | longitude | long |
|  |  | ==> | latitude | long |
|  |  | ==> | zipCode | long |
|  |  | ==> | isActive = true | boolean |

# Unchanged: #

## throw ##
| id | long | ==> | | |
|:---|:-----|:----|:|:|
| throwNumber | int,nonull,uniquecombo | ==> |  |  |
| gameId | long,nonull,uniquecombo | ==> |  |  |
| playerId  | long,nonull | ==> |  |  |
| timestamp | date,nonull | ==> |  |  |
| throwType | int,nonull | ==> |  |  |
| throwResult | int,nonull | ==> |  |  |
| isOwnGoal = false | boolean | ==> |  |  |
| ownGoalScore = 0 | int | ==> |  |  |
| isError = false | boolean | ==> |  |  |
| errorScore = 0 | int | ==> |  |  |
| isGoaltend = false | boolean | ==> |  |  |
| goaltendScore = 0 | int | ==> |  |  |
| isDrinkHit = false | boolean | ==> |  |  |
| isDrinkDropped = false | boolean | ==> |  |  |
| isTrap = false  | boolean | ==> |  |  |
| isOnFire = false | boolean | ==> |  |  |
| isFiredOn = false | boolean | ==> |  |  |
| isShort = false | boolean | ==> |  |  |
| isBroken = false | boolean | ==> |  |  |
| initialOffensivePlayerScore = 0 | int | ==> |  |  |
| initialDefensivePlayerScore = 0 | int | ==> |  |  |