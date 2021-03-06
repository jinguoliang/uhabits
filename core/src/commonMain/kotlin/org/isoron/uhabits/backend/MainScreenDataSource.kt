/*
 * Copyright (C) 2016-2019 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.backend

import org.isoron.platform.concurrency.*
import org.isoron.platform.time.*
import org.isoron.uhabits.models.*

class MainScreenDataSource(val habits: MutableMap<Int, Habit>,
                           val checkmarks: MutableMap<Habit, CheckmarkList>,
                           val taskRunner: TaskRunner) {

    private val today = LocalDate(2019, 3, 30) /* TODO */

    data class Data(val habits: List<Habit>,
                    val currentScore: Map<Habit, Double>,
                    val checkmarkValues: Map<Habit, List<Int>>)

    val observable = Observable<Listener>()

    interface Listener {
        fun onDataChanged(newData: Data)
    }

    fun requestData() {
        taskRunner.runInBackground {
            val filtered = habits.values.filter { h -> !h.isArchived }
            val currentScores = filtered.associate { it to 0.0 /* TODO */}
            val recentCheckmarks = filtered.associate {
                val allValues = checkmarks[it]!!.getValuesUntil(today)
                if (allValues.size <= 7) it to allValues
                else it to allValues.subList(0, 7)
            }
            val data = Data(filtered, currentScores, recentCheckmarks)
            taskRunner.runInForeground {
                observable.notifyListeners { listener ->
                    listener.onDataChanged(data)
                }
            }
        }
    }
}