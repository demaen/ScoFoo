package com.scofoo.app

import java.io.Serializable
import java.time.LocalDate

/**
 * This is a *Model of Goals*
 *
 * Goals could be set to achieve them within a certain target zone
 *
 * @property choice should be 0 (meat), 1 (veggi) or 2 (vegan)
 * @property type should be 0 (min), 1 (max) or 2 (equal)
 * @property target that's the value that shouldn't or should be exceeded corresponding to the type
 * @property value that's the value actual value
 * @property start that's the date where the time frame starts (including this day)
 * @property end that's the date where the time frame ends (including this day)
 *
 */
class DMCGoal (val choice:Int, val type: Int, val target: Int, val start: LocalDate, val end: LocalDate, var achieved: Boolean? = null, var achievable: Boolean? = null, var value: Int? = null): Serializable